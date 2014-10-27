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
package fr.sewatech.mqtt.cdi.example;

import fr.sewatech.mqtt.cdi.api.MqttMessage;
import fr.sewatech.mqtt.cdi.api.MqttTopic;
import fr.sewatech.mqtt.cdi.connector.MqttConnectionFactoryImpl;
import fr.sewatech.mqtt.cdi.connector.MqttConnectionImpl;
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
    private MqttConnectionFactoryImpl connectionFactory;

    private AtomicInteger count = new AtomicInteger();

    public void onQuestion(@Observes @MqttTopic("swt/Question") MqttMessage message) {
        logger.fine("Received : " + count.incrementAndGet());
        System.out.println("Message received " + message.asText() + " in " + this.getClass().getName() + " on Topic " + message.getTopic());
        answer("Answer " + message.asText());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Sleeping thread interrupted", e);
        }
        logger.fine("Done : " + count.decrementAndGet());
    }

    public void onQuestionBis(@Observes @MqttTopic("swt/QuestionBis") MqttMessage message) {
        System.out.println("Message received " + message.asText() + " in " + this.getClass().getName() + " on Topic " + message.getTopic());
    }

    public void onQuestionOtherBroker(@Observes @MqttTopic(value = "swt/Question", url = "tcp://docker:2883") MqttMessage message) {
        System.out.println("Message received " + message.asText() + " in " + this.getClass().getName() + " on Topic " + message.getTopic());
    }

    private void answer(String message) {
        if (connectionFactory == null) {
            logger.warning(this.getClass().getName() + " is trying to answer but has no connection factory");
            return;
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(this.getClass().getName() + " will answer " + message);
        }
        try {
            MqttConnectionImpl connection = connectionFactory.getConnection();
            connection.publish("swt/Default", message, QoS.AT_MOST_ONCE);
            connection.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "ARRRRGH : " + e.getMessage(), e);
        }
    }
}
