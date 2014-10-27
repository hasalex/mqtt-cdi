package fr.sewatech.mqtt.cdi.connector;

import fr.sewatech.mqtt.cdi.api.MqttMessage;
import fr.sewatech.mqtt.cdi.api.MqttTopic;
import org.fusesource.mqtt.client.Topic;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

/**
 * @author Alexis Hassler
 */
public class MqttExtension implements Extension {

    private static final Logger logger = Logger.getLogger(MqttExtension.class.getName());

    private Map<String, Set<Topic>> topicMap = new HashMap<>();
    private List<MqttMessageReceiver> receivers = new ArrayList<>();

    void registerTopic(@Observes ProcessObserverMethod<MqttMessage, ?> observerMethod) {
        logger.fine("ProcessObserverMethod");
        Set<Annotation> qualifiers = observerMethod.getObserverMethod().getObservedQualifiers();
        for (Annotation qualifier : qualifiers) {
            if (qualifier instanceof MqttTopic) {
                MqttTopic topic = (MqttTopic) qualifier;
                Set<Topic> topics = topicMap.get(topic.url());
                if (topics == null) {
                    topics = new HashSet<>();
                    topicMap.put(topic.url(), topics);
                }
                topics.add(new Topic(topic.value(), topic.qos()));
            }
        }
    }

    void afterDeploymentValidation(@Observes AfterDeploymentValidation afterDeploymentValidation, BeanManager beanManager) {
        logger.fine("AfterDeploymentValidation ...");

        for (Map.Entry<String, Set<Topic>> entry : topicMap.entrySet()) {
            Set<Topic> topicSet = entry.getValue();
            Topic[] topics = topicSet.toArray(new Topic[topicSet.size()]);
            MqttMessageReceiver receiver = new MqttMessageReceiver(entry.getKey(), topics, beanManager);
            receivers.add(receiver);
            newThread(receiver).start();
        }
    }

    void shutdown(@Observes BeforeShutdown beforeShutdown) {
        logger.fine("Before shutdown ...");
        for (MqttMessageReceiver receiver : receivers) {
            receiver.shutdown();
        }
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
