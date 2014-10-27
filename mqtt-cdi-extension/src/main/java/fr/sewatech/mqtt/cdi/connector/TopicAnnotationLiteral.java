package fr.sewatech.mqtt.cdi.connector;

import fr.sewatech.mqtt.cdi.api.MqttTopic;
import org.fusesource.mqtt.client.QoS;

import javax.enterprise.util.AnnotationLiteral;

class TopicAnnotationLiteral extends AnnotationLiteral<MqttTopic> implements MqttTopic {
    private String value;

    TopicAnnotationLiteral(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public QoS qos() {
        return QoS.AT_MOST_ONCE;
    }
}
