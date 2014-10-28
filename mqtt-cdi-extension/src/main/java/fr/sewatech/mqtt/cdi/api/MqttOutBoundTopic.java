package fr.sewatech.mqtt.cdi.api;

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
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface MqttOutBoundTopic {

    String value();

    String url() default "tcp://localhost:1883";

    QoS qos() default QoS.AT_MOST_ONCE;
}
