package com.system.batch.kill_batch_system.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.redis.RedisItemReader;
import org.springframework.batch.item.redis.builder.RedisItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class HackerAttackAggregationJob {
    private final RedisConnectionFactory redisConnectionFactory;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job aggregateHackerAttackJob(
            Step aggregateAttackStep,
            Step reportAttacktStep,
            AttackCounter attackCounter
    ) {
        return new JobBuilder("aggregateHackerAttackJob", jobRepository)
                .start(aggregateAttackStep)
                .next(reportAttacktStep)
                .listener(attackCounter)
                .build();
    }

    @Bean
    public Step aggregateAttackStep(
            RedisItemReader<String, AttackModels.AttackLog> attackLogReader,
            AttackCounterItemWriter attackCounterItemWriter
    ) {
        return new StepBuilder("aggregateAttackStep", jobRepository)
                .<AttackModels.AttackLog, AttackModels.AttackLog>chunk(10, transactionManager)
                .reader(attackLogReader)
                .processor(item -> {
                    log.info("{}", item);
                    return item;
                })
                .writer(attackCounterItemWriter)
                .build();
    }

    @Bean
    public Step reportAttacktStep(AttackCounter attackCounter) {
        return new StepBuilder("reportAttacktStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("{}", attackCounter.generateAnalysis());
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public RedisItemReader<String, AttackModels.AttackLog> attackLogReader() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        RedisTemplate<String, AttackModels.AttackLog> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(objectMapper, AttackModels.AttackLog.class));
        template.afterPropertiesSet();

        return new RedisItemReaderBuilder<String, AttackModels.AttackLog>()
                .redisTemplate(template)
                .scanOptions(ScanOptions
                        .scanOptions()
                        .match("attack:*")  // attack: 으로 시작하는 키만 스캔
                        .count(10)
                        .build())
                .build();
    }
//
//    @Bean
//    public RedisItemWriter<String, AttackModels.AttackLog> deleteAttackLogWriter() {
//        return new RedisItemWriterBuilder<String, AttackModels.AttackLog>()
//                .redisTemplate(redisTemplate)
//                .itemKeyMapper(attackLog -> "attack:" + attackLog.getId())
//                .delete(true)
//                .build();
//    }

    @Component
    @RequiredArgsConstructor
    public static class AttackCounterItemWriter implements ItemWriter<AttackModels.AttackLog> {
        private final AttackCounter attackCounter;

        @Override
        public void write(Chunk<? extends AttackModels.AttackLog> chunk) {
            for (AttackModels.AttackLog attackLog : chunk) {
                attackCounter.record(attackLog);
            }
        }
    }
}
