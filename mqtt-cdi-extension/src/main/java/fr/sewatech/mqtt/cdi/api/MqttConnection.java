package fr.sewatech.mqtt.cdi.api;

import org.fusesource.mqtt.client.QoS;

/**
 * @author Alexis Hassler
 */
public interface MqttConnection {

    void publish(String topicName, String message, QoS qos);

    void close();

}
