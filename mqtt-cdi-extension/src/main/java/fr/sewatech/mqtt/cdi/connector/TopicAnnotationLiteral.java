package fr.sewatech.mqtt.cdi.connector;

import fr.sewatech.mqtt.cdi.api.MqttInboundTopic;
import org.fusesource.mqtt.client.QoS;

import javax.enterprise.util.AnnotationLiteral;

class TopicAnnotationLiteral extends AnnotationLiteral<MqttInboundTopic> implements MqttInboundTopic {
    private String value;
    private String url;

    TopicAnnotationLiteral(String url, String value) {
        this.url = url;
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public QoS qos() {
        return QoS.AT_MOST_ONCE;
    }
}
