package fr.sewatech.mqttcdi.connector;

import fr.sewatech.mqttcdi.api.MqttMessage;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Alexis Hassler
 */
class AsyncMessageEventSender {

    private ExecutorService executorService;

    @Inject
    private Event<MqttMessage> mqttMessageEvent;

    @PostConstruct
    void init() {
        try {
            executorService = InitialContext.doLookup("java:comp/DefaultManagedExecutorService");
        } catch (NamingException e) {
            executorService = new ThreadPoolExecutor(16, 16, 10, TimeUnit.MINUTES, new LinkedBlockingDeque<Runnable>());
        }
    }

    void send(MqttMessage message) {
        executorService.execute(new EventRunnable(message));
    }

    private class EventRunnable implements Runnable {
        private MqttMessage message;

        public EventRunnable(MqttMessage message) {
            this.message = message;
        }

        public void run() {
            mqttMessageEvent
                    .select(new TopicAnnotationLiteral(message.getTopic()))
                    .fire(message);
        }
    }

}
