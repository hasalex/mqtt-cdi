package fr.sewatech.mqttcdi.connector;

import fr.sewatech.mqttcdi.api.MqttMessage;
import org.fusesource.mqtt.client.Future;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.net.URISyntaxException;

/**
 * @author Alexis Hassler
 */
@ApplicationScoped
class MqttReceiver {

    @Inject
    private AsyncMessageEventSender asyncMessageEventSender;

    private FutureConnection connection;

    private void connect(@Observes MqttExtensionInitialized init) {
        System.out.println("About to connect...");

        try {
            FutureConnection connection = connect(init.host);

            connection.subscribe(init.topics);
            while (true) {
                Future<Message> futureMessage = connection.receive();
                Message message = futureMessage.await();
                message.ack();
                asyncMessageEventSender.send(new MqttMessage(message.getTopic(), message.getPayload()));
            }
        } catch (InterruptedException e) {
            System.out.println(e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void disconnect(@Observes MqttExtensionShutdown shutdown) {
        System.out.println("disconnect");
        if (connection != null) {
            try {
                connection.unsubscribe(shutdown.topics);
                connection.disconnect().await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private FutureConnection connect(String host) throws URISyntaxException {
        MQTT mqtt = new MQTT();
        mqtt.setHost(host);

        connection = mqtt.futureConnection();
        connection.connect();
        return connection;
    }

}
