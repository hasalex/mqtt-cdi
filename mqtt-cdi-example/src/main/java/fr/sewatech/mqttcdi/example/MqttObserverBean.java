/**
 * Copyright 2014 Sewatech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.sewatech.mqttcdi.example;

import fr.sewatech.mqttcdi.api.MqttMessage;
import fr.sewatech.mqttcdi.api.MqttTopic;
import fr.sewatech.mqttcdi.outbound.MqttConnection;
import fr.sewatech.mqttcdi.outbound.MqttConnectionFactory;
import org.fusesource.mqtt.client.QoS;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alexis Hassler
 */
@ApplicationScoped
public class MqttObserverBean {

    private static final Logger logger = Logger.getLogger(MqttObserverBean.class.getName());

    @Inject
    private MqttConnectionFactory connectionFactory;

    private AtomicInteger count = new AtomicInteger();

    public void onQuestion(@Observes @MqttTopic(value = "swt/Question", qos = QoS.EXACTLY_ONCE) MqttMessage message) {
        System.out.println("Received : " + count.incrementAndGet());
        answer("Answer " + count.get());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Done : " + count.decrementAndGet());
    }

    public void onQuestionBis(@Observes @MqttTopic("swt/QuestionBis") MqttMessage message) {
        System.out.println("Message received (@TopicListener) " + new String(message.getPayload()) + " in " + this.getClass().getName() + " on Topic " + message.getTopic());
    }

    private void answer(String message) {
        if (connectionFactory == null) {
            logger.fine(this.getClass().getName() + " is trying to answer but has no connection factory");
            return;
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(this.getClass().getName() + " will answer " + message);
        }
        try {
            MqttConnection connection = connectionFactory.getConnection();
            connection.publish("swt/Default", message, QoS.AT_MOST_ONCE);
            connection.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
