package fr.sewatech.mqtt.cdi.api;

import fr.sewatech.mqtt.cdi.connector.MqttConnectionImpl;

/**
 * @author Alexis Hassler
 */
public interface MqttConnectionFactory {

    MqttConnectionImpl getConnection();

}
