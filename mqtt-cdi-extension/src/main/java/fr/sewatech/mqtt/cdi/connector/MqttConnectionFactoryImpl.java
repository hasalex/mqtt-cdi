package fr.sewatech.mqtt.cdi.connector;

import fr.sewatech.mqtt.cdi.api.MqttConnectionFactory;
import fr.sewatech.mqtt.cdi.api.MqttOutBoundTopic;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

/**
 * @author Alexis Hassler
 */
@Dependent
public class MqttConnectionFactoryImpl implements MqttConnectionFactory{

    @Inject
    private MqttConnectionPools connectionPools;

    @Inject
    private InjectionPoint injectionPoint;

    private MqttOutBoundTopic annotation;

    @PostConstruct
    private void init() {
        annotation = injectionPoint.getAnnotated().getAnnotation(MqttOutBoundTopic.class);
    }

    @Override
    public MqttConnectionImpl getConnection() {
        return connectionPools.getConnection(annotation);
    }
}
