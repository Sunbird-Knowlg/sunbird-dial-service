package org.sunbird.utils;

import org.sunbird.commons.AppConfig;
import org.sunbird.commons.exception.ClientException;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.sunbird.telemetry.TelemetryManager;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class KafkaUtils {

	private final static String BOOTSTRAP_SERVERS = AppConfig.getString("kafka.urls", "localhost:9092");
	private static Producer<Long, String> producer;
	private static Consumer<Long, String> consumer;

	static {
		loadProducerProperties();
		loadConsumerProperties();
	}

	private static void loadProducerProperties() {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
		props.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaClientProducer");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		producer = new KafkaProducer<Long, String>(props);
	}

	private static void loadConsumerProperties() {
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
		props.put(ConsumerConfig.CLIENT_ID_CONFIG, "KafkaClientConsumer");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		consumer = new KafkaConsumer<>(props);
	}

	protected static Producer<Long, String> getProducer() {
		return producer;
	}

	protected static Consumer<Long, String> getConsumer() {
		return consumer;
	}

	public static void send(String event, String topic) throws Exception {
		boolean isTopicCheckReq = AppConfig.getBoolean("kafka.topic.send_enable", false);
		if (!isTopicCheckReq)
			return;
		if(validate(topic)) {
			final Producer<Long, String> producer = getProducer();
			ProducerRecord<Long, String> record = new ProducerRecord<Long, String>(topic, event);
			producer.send(record);
			TelemetryManager.info("Event Sent Successfully to the topic : " + topic + " | Event: " + event);
		}else {
			TelemetryManager.error("Topic with name: " + topic + " does not exists.");
			throw new ClientException("TOPIC_NOT_EXISTS", "Topic with name : " + topic + " does not exists.");
		}
	}
	public static boolean validate(String topic) throws Exception{
		Consumer<Long, String> consumer = getConsumer();
		Map<String, List<PartitionInfo>> topics = consumer.listTopics();
		return topics.keySet().contains(topic);
	}
}
