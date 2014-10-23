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

        ThreadFactory threadFactory;
        try {
            threadFactory = InitialContext.doLookup("java:comp/DefaultManagedThreadFactory");
        } catch (NamingException e) {
            threadFactory = Executors.defaultThreadFactory();
        }

        Topic[] topics = topicSet.toArray(new Topic[topicSet.size()]);
        threadFactory.newThread(new EventRunnable(new MqttExtensionInitialized(topics), beanManager)).start();
    }

    void shutdown(@Observes BeforeShutdown beforeShutdown, BeanManager beanManager) {
        logger.fine("BeforeShutdown...");
        String[] topics = new String[topicSet.size()];
        int i = 0;
        for (Topic topic : topicSet) {
            topics[i++] = topic.name().toString();
        }
        beanManager.fireEvent(new MqttExtensionShutdown(topics));
    }

    private class EventRunnable implements Runnable {
        private MqttExtensionInitialized event;
        private BeanManager beanManager;

        public EventRunnable(MqttExtensionInitialized event, BeanManager beanManager) {
            this.event = event;
            this.beanManager = beanManager;
        }

        public void run() {
            logger.fine("Run...");
            beanManager.fireEvent(event);
        }
    }
}
