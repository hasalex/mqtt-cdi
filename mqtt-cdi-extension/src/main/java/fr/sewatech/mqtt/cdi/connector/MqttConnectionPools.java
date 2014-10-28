package fr.sewatech.mqtt.cdi.connector;

import fr.sewatech.mqtt.cdi.api.MqttOutBoundTopic;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

@ApplicationScoped
class MqttConnectionPools {

    private static final Logger logger = Logger.getLogger(MqttConnectionPools.class.getName());

    private ConcurrentLinkedQueue<BlockingConnection> defaultPool = new ConcurrentLinkedQueue<>();
    private ConcurrentHashMap<MqttOutBoundTopic, ConcurrentLinkedQueue<BlockingConnection>> pools = new ConcurrentHashMap<>();

    MqttConnectionImpl getConnection(MqttOutBoundTopic topic) {
        try {
            BlockingConnection blockingConnection = getPool(topic).poll();
            if (blockingConnection == null) {
                String url = topic == null ? "tcp://localhost:1883" : topic.url();
                logger.fine("Creating a new connection to " + url);
                blockingConnection = createConnection(url);
            } else {
                logger.fine("Existing connection available, no need to create a new one");
            }

            return new MqttConnectionImpl(blockingConnection, this, topic);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void close(MqttConnectionImpl connection) {
        ConcurrentLinkedQueue<BlockingConnection> pool = getPool(connection.topic);
        if (pool != null) {
            pool.offer(connection.blockingConnection);
        }
    }

    @PreDestroy
    private void shutdown() {
        logger.fine("Shutting down connection factory");
        for (ConcurrentLinkedQueue<BlockingConnection> pool : pools.values()) {
            for (BlockingConnection connection : pool) {
                try {
                    connection.disconnect();
                } catch (Exception e) {
                    logger.warning("Problem while disconnecting MQTT connection : " + e);
                }
            }
        }
    }

    private ConcurrentLinkedQueue<BlockingConnection> getPool(MqttOutBoundTopic topic) {
        ConcurrentLinkedQueue<BlockingConnection> pool;
        if (topic == null) {
            pool = defaultPool;
        } else {
            pool = pools.get(topic);
            if (pool == null) {
                pool = new ConcurrentLinkedQueue<>();
            }
        }
        return pool;
    }

    private BlockingConnection createConnection(String url) throws Exception {
        BlockingConnection blockingConnection;
        MQTT mqtt = new MQTT();
        mqtt.setHost(url);
        mqtt.setUserName("");
        mqtt.setPassword("");
        blockingConnection = mqtt.blockingConnection();
        blockingConnection.connect();
        return blockingConnection;
    }
}
