package fr.sewatech.mqttcdi.connector;

import fr.sewatech.mqttcdi.api.MqttMessage;
import fr.sewatech.mqttcdi.api.MqttTopic;
import org.fusesource.mqtt.client.Topic;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessObserverMethod;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Alexis Hassler
 */
@ApplicationScoped
public class MqttExtension implements Extension {

    private Set<Topic> topics = new HashSet<>();

    void registerTopic(@Observes ProcessObserverMethod<MqttMessage, ?> observerMethod) {
        System.out.println("ProcessObserverMethod");
        Set<Annotation> qualifiers = observerMethod.getObserverMethod().getObservedQualifiers();
        for (Annotation qualifier : qualifiers) {
            if (qualifier instanceof MqttTopic) {
                MqttTopic topic = (MqttTopic) qualifier;
                topics.add(new Topic(topic.value(), topic.qos()));
            }
        }
    }

    void end(@Observes AfterDeploymentValidation afterDeploymentValidation, BeanManager beanManager) {
        System.out.println("AfterDeploymentValidation");
        beanManager.fireEvent(new MqttExtensionInitialized(topics.toArray(new Topic[topics.size()])));
    }
}
