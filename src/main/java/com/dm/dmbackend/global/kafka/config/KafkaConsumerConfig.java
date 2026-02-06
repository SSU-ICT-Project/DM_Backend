package com.dm.dmbackend.global.kafka.config;

import com.dm.dmbackend.global.exception.ServiceException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {
    @Bean
    DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaTemplate<Object, Object> kt) {
        // 기본 규칙: 실패한 레코드의 원본 토픽명에 ".DLT"를 붙여 발행
        return new DeadLetterPublishingRecoverer(kt);
    }

    @Bean
    DefaultErrorHandler kafkaDefaultErrorHandler(DeadLetterPublishingRecoverer dlpr) {
        var backoff = new org.springframework.util.backoff.FixedBackOff(1000L, 3L); // 1초 간격, 최대 3회 재시도
        var handler = new DefaultErrorHandler(dlpr, backoff);
        // 업무상 재시도 무의미한 예외는 DLT로 빠르게 이동
        handler.addNotRetryableExceptions(ServiceException.class);
        handler.addNotRetryableExceptions(IllegalArgumentException.class);
        return handler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            DefaultErrorHandler errorHandler
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        // 필요시 ack 모드, 동시성, 배치 소비 등 추가 설정
        return factory;
    }
}
