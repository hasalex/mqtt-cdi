package fr.sewatech.mqtt.cdi.connector;

import fr.sewatech.mqtt.cdi.api.MqttConnection;
import fr.sewatech.mqtt.cdi.api.MqttOutBoundTopic;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.QoS;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MqttConnectionImpl implements MqttConnection {

    private static final Logger logger = Logger.getLogger(MqttConnectionImpl.class.getName());

    BlockingConnection blockingConnection;
    private MqttConnectionPools connectionProvider;
    final MqttOutBoundTopic topic;

    MqttConnectionImpl(BlockingConnection blockingConnection, MqttConnectionPools mqttConnectionFactory, MqttOutBoundTopic topic) {
        this.blockingConnection = blockingConnection;
        connectionProvider = mqttConnectionFactory;
        this.topic = topic;
    }

    public void publish(String message) {
        if (topic == null) {
            publish("swt/Default", message, QoS.AT_MOST_ONCE);
        } else {
            publish(topic.value(), message, topic.qos());
        }
    }

    public void publish(String topicName, String message, QoS qos) {
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Trying to publish message " + message + " on topic " + topicName);
            }
            if (blockingConnection == null) {
                throw new RuntimeException("Connection closed");
            }
            blockingConnection.publish(topicName, message.getBytes(), qos, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void close() {
        connectionProvider.close(this);
    }
}
