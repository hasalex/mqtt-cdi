package fr.sewatech.mqttcdi.connector;

import org.fusesource.mqtt.client.Topic;

/**
 * @author Alexis Hassler
 */
class MqttExtensioninitialized {

    final String host = "tcp://localhost:1883";

    final Topic[] topics;

    MqttExtensioninitialized(Topic[] topics) {
        this.topics = topics;
    }

}
