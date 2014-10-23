package fr.sewatech.mqttcdi.connector;

import fr.sewatech.mqttcdi.api.MqttMessage;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alexis Hassler
 */
@ApplicationScoped
class MqttReceiver {

    private static final Logger logger = Logger.getLogger(MqttReceiver.class.getName());

    @Inject
    private AsyncMessageEventSender asyncMessageEventSender;

    private FutureConnection connection;

    private void connect(@Observes MqttExtensionInitialized init) {
        logger.fine("About to connect...");

        try {
            FutureConnection connection = connect(init.host);
            logger.fine("Connected ? " + connection.isConnected());
            connection.subscribe(init.topics);
            while (true) {
                Message message = connection.receive().await();
                asyncMessageEventSender.send(new MqttMessage(message.getTopic(), message.getPayload()));
                message.ack();

                if (!connection.isConnected()) {
                    return;
                }
            }
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Receiver thread interrupted", e);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Receiver problem", e);
        }
    }

    private void disconnect(@Observes MqttExtensionShutdown event) {
        logger.fine("About to disconnect");
        if (connection != null) {
            try {
                connection.unsubscribe(event.topics);
                connection.disconnect().await();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Problem while disconnnecting", e);
            }
        }
    }

    private FutureConnection connect(String host) throws Exception {
        MQTT mqtt = new MQTT();
        mqtt.setHost(host);

        connection = mqtt.futureConnection();
        connection.connect();
        return connection;
    }

}
