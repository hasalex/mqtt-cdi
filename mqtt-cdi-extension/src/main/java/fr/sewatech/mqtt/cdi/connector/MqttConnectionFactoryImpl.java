package fr.sewatech.mqtt.cdi.connector;

import fr.sewatech.mqtt.cdi.api.MqttConnectionFactory;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

@ApplicationScoped
public class MqttConnectionFactoryImpl implements MqttConnectionFactory {

    private static final Logger logger = Logger.getLogger(MqttConnectionFactoryImpl.class.getName());

    private ConcurrentLinkedQueue<BlockingConnection> pool = new ConcurrentLinkedQueue<>();

    public MqttConnectionImpl getConnection() {
        try {
            BlockingConnection blockingConnection = pool.poll();
            if (blockingConnection == null) {
                logger.fine("Creating a new connection");
                blockingConnection = createConnection();
            } else {
                logger.fine("Existing connection available, no need to create a new one");
            }

            return new MqttConnectionImpl(blockingConnection, this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void close(MqttConnectionImpl connection) {
        pool.offer(connection.blockingConnection);
    }

    @PreDestroy
    private void shutdown() {
        logger.fine("Shutting down connection factory");
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
