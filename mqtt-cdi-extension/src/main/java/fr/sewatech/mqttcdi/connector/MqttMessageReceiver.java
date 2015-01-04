package fr.sewatech.mqttcdi.connector;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.Topic;

import fr.sewatech.mqttcdi.api.MqttMessage;

/**
* @author Alexis Hassler
*/
class MqttMessageReceiver implements Runnable {

    private static final Logger logger = Logger.getLogger(MqttMessageReceiver.class.getName());

    private Topic[] topics;
    private Map<Pattern, String> wildcardTopics;
    private BeanManager beanManager;
    private FutureConnection connection;

    MqttMessageReceiver(Topic[] topics, BeanManager beanManager) {
        this.topics = topics;
        this.wildcardTopics = extractWildcardTopics(topics);
        this.beanManager = beanManager;       
    }

    public void run() {
        try {
            connection = connect("tcp://localhost:1883");
            connection.subscribe(topics);
            logger.fine("... connected");

            ExecutorService executorService;
            try {
                executorService = InitialContext.doLookup("java:comp/DefaultManagedExecutorService");
            } catch (NamingException e) {
                executorService = new ThreadPoolExecutor(16, 16, 10, TimeUnit.MINUTES, new LinkedBlockingDeque<Runnable>());
            }

            while (true) {
                Message message = connection.receive().await();
                executorService.execute(new MessageEventAsyncSender(new MqttMessage(message.getTopic(), message.getPayload())));

                message.ack();

                if (!connection.isConnected()) {
                    logger.fine("End of receiver");
                    return;
                }
            }
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Receiver thread interrupted", e);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Receiver problem", e);
        }
    }


    void shutdown() {
        if (connection == null) {
            logger.fine("No connection to close");
        } else {
            logger.fine("About to disconnect receiver");
            String[] topicNames = new String[topics.length];
            int i = 0;
            for (Topic topic : topics) {
                topicNames[i++] = topic.name().toString();
            }

            try {
                connection.unsubscribe(topicNames).await();
                logger.fine("Topics unsubscribed");
                connection.disconnect().await();
                logger.fine("MQTT disconnected");
            } catch (Exception e) {
                logger.log(Level.WARNING, "Problem while disconnecting", e);
            }
        }

    }


    private class MessageEventAsyncSender implements Runnable {
        private MqttMessage message;

        public MessageEventAsyncSender(MqttMessage message) {
            this.message = message;
        }


        public void run() {
        	beanManager.fireEvent(message, new TopicAnnotationLiteral(message.getTopic()));
        	
        	for (Pattern wildcardTopic : wildcardTopics.keySet()) {
				if (wildcardTopic.matcher(message.getTopic()).matches()) {
					beanManager.fireEvent(message, new TopicAnnotationLiteral(wildcardTopics.get(wildcardTopic)));
				}
			}
        }
    }

    private FutureConnection connect(String host) throws Exception {
        MQTT mqtt = new MQTT();
        mqtt.setHost(host);

        connection = mqtt.futureConnection();
        connection.connect().await();
        return connection;
    }
    
    private Map<Pattern, String> extractWildcardTopics(Topic[] topics) {
		Map<Pattern, String> wildcardTopics = new HashMap<>();
        String partPattern = "[a-zA-Z0-9\\_\\-\\%\\~\\:\\(\\)]+";
        
        for (Topic topic : topics) {
        	String topicName = topic.name().toString();
			if (topicName.contains("#") || topicName.contains("+")) {
				String patternString = "^"+topicName.replace("/", "\\/")+"$";
				patternString = patternString.replace("+", partPattern);
				patternString = patternString.replace("#", "("+partPattern+"\\/*)*");
				Pattern pattern = Pattern.compile(patternString);
				wildcardTopics.put(pattern, topicName);
			}
		}
        
		return wildcardTopics;
	}

}
