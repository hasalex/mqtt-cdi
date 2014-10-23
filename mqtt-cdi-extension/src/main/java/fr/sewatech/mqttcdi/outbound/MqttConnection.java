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
package fr.sewatech.mqttcdi.outbound;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.QoS;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MqttConnection {

    private static final Logger logger = Logger.getLogger(MqttConnection.class.getName());

    BlockingConnection blockingConnection;
    private MqttConnectionFactory connectionFactory;

    MqttConnection(BlockingConnection blockingConnection, MqttConnectionFactory mqttConnectionFactory) {
        this.blockingConnection = blockingConnection;
        connectionFactory = mqttConnectionFactory;
    }

    public void publish(String topicName, String message, QoS qos) {
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Trying to publish message " + message + " on topic " + topicName);
            }
            if (blockingConnection == null) {
                throw new RuntimeException("Connection closed");
            }
            blockingConnection.publish(topicName, message.getBytes(), qos, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void close() {
        connectionFactory.close(this);
    }
}
