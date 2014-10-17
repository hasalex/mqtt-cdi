package fr.sewatech.mqttcdi.connector;

/**
 * @author Alexis Hassler
 */
class MqttExtensionShutdown {

    final String[] topics;

    MqttExtensionShutdown(String[] topics) {
        this.topics = topics;
    }

}
