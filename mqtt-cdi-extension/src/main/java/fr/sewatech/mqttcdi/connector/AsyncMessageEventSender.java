package fr.sewatech.mqttcdi.connector;

import fr.sewatech.mqttcdi.api.MqttMessage;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
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
@ApplicationScoped
class AsyncMessageEventSender {

    private ExecutorService executorService;

    @Inject
    private Event<MqttMessage> mqttMessageEvent;

    public AsyncMessageEventSender() {
        System.out.println("NEW AsyncMessageEventSender");
    }

    @PostConstruct
    void init() {
        try {
            executorService = InitialContext.doLookup("java:comp/DefaultManagedExecutorService");
        } catch (NamingException e) {
            executorService = new ThreadPoolExecutor(16, 16, 10, TimeUnit.MINUTES, new LinkedBlockingDeque<Runnable>());
        } catch (IllegalArgumentException e) {
            System.out.println(e);
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
