package fr.sewatech.mqttcdi.api;

import org.fusesource.mqtt.client.QoS;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Alexis Hassler
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface MqttTopic {

    String value();

    @Nonbinding
    QoS qos() default QoS.AT_MOST_ONCE;
}
