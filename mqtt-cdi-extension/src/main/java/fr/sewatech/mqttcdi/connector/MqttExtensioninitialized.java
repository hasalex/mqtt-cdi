package fr.sewatech.mqttcdi.connector;

import org.fusesource.mqtt.client.Topic;

/**
 * @author Alexis Hassler
 */
class MqttExtensionInitialized {

    final String host = "tcp://localhost:1883";

    final Topic[] topics;

    MqttExtensionInitialized(Topic[] topics) {
        this.topics = topics;
    }

}
