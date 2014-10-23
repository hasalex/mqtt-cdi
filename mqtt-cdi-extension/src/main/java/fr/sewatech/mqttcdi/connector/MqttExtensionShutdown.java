package fr.sewatech.mqttcdi.connector;

/**
 * @author Alexis Hassler
 */
public class MqttExtensionShutdown {

    final String[] topics;

    MqttExtensionShutdown(String[] topics) {
        this.topics = topics;
    }

}
