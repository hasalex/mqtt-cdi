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

import fr.sewatech.mqttcdi.connector.MqttExtension;
import fr.sewatech.mqttcdi.connector.MqttExtensionShutdown;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

@ApplicationScoped
public class MqttConnectionFactory {

    private static final Logger logger = Logger.getLogger(MqttConnectionFactory.class.getName());

    private ConcurrentLinkedQueue<BlockingConnection> pool = new ConcurrentLinkedQueue<>();

    public MqttConnection getConnection() {
        try {
            BlockingConnection blockingConnection = pool.poll();
            if (blockingConnection == null) {
                blockingConnection = createConnection();
            }

            MqttConnection mqttConnection = new MqttConnection(blockingConnection, this);
            return mqttConnection;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void close(MqttConnection connection) {
        pool.offer(connection.blockingConnection);
    }

    void shutdown(@Observes MqttExtensionShutdown shutdown) {
        for (BlockingConnection connection : pool) {
            try {
                connection.disconnect();
            } catch (Exception e) {
                logger.warning("Problem while disconnecting MQTT connection : " + e);
            }
        }
    }

    private BlockingConnection createConnection() throws Exception {
        BlockingConnection blockingConnection;MQTT mqtt = new MQTT();
        mqtt.setHost("tcp://localhost:1883");
        mqtt.setUserName("");
        mqtt.setPassword("");
        blockingConnection = mqtt.blockingConnection();
        blockingConnection.connect();
        return blockingConnection;
    }
}
