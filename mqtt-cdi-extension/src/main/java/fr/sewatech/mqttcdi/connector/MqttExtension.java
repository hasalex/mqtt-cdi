package fr.sewatech.mqttcdi.connector;

import fr.sewatech.mqttcdi.api.MqttMessage;
import fr.sewatech.mqttcdi.api.MqttTopic;
import org.fusesource.mqtt.client.Topic;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

/**
 * @author Alexis Hassler
 */
public class MqttExtension implements Extension {

    private static final Logger logger = Logger.getLogger(MqttExtension.class.getName());

    private Set<Topic> topicSet = new HashSet<>();
    private MqttMessageReceiver receiver;

    void registerTopic(@Observes ProcessObserverMethod<MqttMessage, ?> observerMethod) {
        logger.fine("ProcessObserverMethod");
        Set<Annotation> qualifiers = observerMethod.getObserverMethod().getObservedQualifiers();
        for (Annotation qualifier : qualifiers) {
            if (qualifier instanceof MqttTopic) {
                MqttTopic topic = (MqttTopic) qualifier;
                topicSet.add(new Topic(topic.value(), topic.qos()));
            }
        }
    }

    void afterDeploymentValidation(@Observes AfterDeploymentValidation afterDeploymentValidation, BeanManager beanManager) {
        logger.fine("AfterDeploymentValidation ...");

        Topic[] topics = topicSet.toArray(new Topic[topicSet.size()]);
        receiver = new MqttMessageReceiver(topics, beanManager);
        newThread(receiver).start();
    }

    void shutdown(@Observes BeforeShutdown beforeShutdown) {
        logger.fine("Before shutdown ...");
        receiver.shutdown();
    }

    private Thread newThread(MqttMessageReceiver runnable) {
        ThreadFactory threadFactory;
        try {
            threadFactory = InitialContext.doLookup("java:comp/DefaultManagedThreadFactory");
        } catch (NamingException e) {
            threadFactory = Executors.defaultThreadFactory();
        }
        return threadFactory.newThread(runnable);
    }

}
