package fr.sewatech.mqtt.cdi.connector;

import fr.sewatech.mqtt.cdi.api.MqttConnection;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.QoS;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MqttConnectionImpl implements MqttConnection {

    private static final Logger logger = Logger.getLogger(MqttConnectionImpl.class.getName());

    BlockingConnection blockingConnection;
    private MqttConnectionFactoryImpl connectionFactory;

    MqttConnectionImpl(BlockingConnection blockingConnection, MqttConnectionFactoryImpl mqttConnectionFactory) {
        this.blockingConnection = blockingConnection;
        connectionFactory = mqttConnectionFactory;
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
        connectionFactory.close(this);
    }
}
