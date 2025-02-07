package com.chutneytesting.task.kafka;

import static com.chutneytesting.task.kafka.KafkaClientFactoryHelper.resolveBootStrapServerConfig;
import static java.util.Collections.unmodifiableMap;
import static org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;

import com.chutneytesting.task.spi.injectable.Target;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

public class KafkaConsumerFactoryFactory {

    public ConsumerFactory<String, String> create(Target target, String group, Map<String, String> config) {

        Map<String, Object> consumerConfig = new HashMap<>();
        consumerConfig.put(BOOTSTRAP_SERVERS_CONFIG, resolveBootStrapServerConfig(target));
        consumerConfig.put(GROUP_ID_CONFIG, group);
        consumerConfig.putAll(config);

        return new DefaultKafkaConsumerFactory<>(
            unmodifiableMap(consumerConfig),
            new StringDeserializer(),
            new StringDeserializer());
    }
}
