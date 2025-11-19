package com.system.batch.kill_batch_system.mongo;

import jakarta.persistence.Id;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.batch.item.data.MongoPagingItemReader;
import org.springframework.batch.item.data.builder.MongoItemWriterBuilder;
import org.springframework.batch.item.data.builder.MongoPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
public class HackerPatternDetectionJob2 {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final MongoTemplate mongoTemplate;

    public HackerPatternDetectionJob2(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            MongoTemplate mongoTemplate
    ) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.mongoTemplate = mongoTemplate;
    }

    @Bean
    public Job detectHackerPatternJob2(Step detectHackerPatternStep) {
        return new JobBuilder("detectHackerPatternJob2", jobRepository)
                .start(detectHackerPatternStep)
                .build();
    }

    @Bean
    public Step detectHackerPatternStep(
            MongoPagingItemReader<SecurityLog> securityLogReader,
            ItemProcessor<SecurityLog, SecurityLog> hackerPatternProcessor,
            MongoItemWriter<SecurityLog> securityLogWriter
    ) {
        return new StepBuilder("detectHackerPatternStep", jobRepository)
                .<SecurityLog, SecurityLog>chunk(10, transactionManager)
                .reader(securityLogReader)
                .processor(hackerPatternProcessor)
                .writer(securityLogWriter)
                .build();
    }

    @Bean
    @StepScope
    public MongoPagingItemReader<SecurityLog> securityLogReader(
            @Value("#{jobParameters['searchDate']}") LocalDate searchDate
    ) {
        Query query = new Query()
                .addCriteria(Criteria.where("label").is("PENDING_ANALYSIS"))
                .addCriteria(Criteria.where("timestamp")
                        .gte(Date.from(searchDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))
                        .lt(Date.from(searchDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant())))
                .with(Sort.by(Sort.Direction.ASC, "timestamp"));

        return new MongoPagingItemReaderBuilder<SecurityLog>()
                .name("securityLogReader")
                .template(mongoTemplate)
                .collection("security_logs")
                .query(query)
                .sorts(Map.of("timestamp", Sort.Direction.ASC))  // 빌더의 버그 때문에 필요
                .targetType(SecurityLog.class)
                .pageSize(10)
                .build();
    }

    @Bean
    public ItemProcessor<SecurityLog, SecurityLog> hackerPatternProcessor() {
        return log -> {
            String detectedPattern = analyzeAttackPattern(log);
            log.setLabel(detectedPattern);
            return log;
        };
    }

    //    @Bean
//    public MongoItemWriter<SecurityLog> securityLogWriter() {
//        return new MongoItemWriterBuilder<SecurityLog>()
//                .template(mongoTemplate)
//                .collection("security_logs")
//                .mode(MongoItemWriter.Mode.UPSERT)  // 기존 문서 수정
//                .build();
//    }
    @Bean
    public MongoItemWriter<SecurityLog> securityLogWriter() {
        return new MongoItemWriterBuilder<SecurityLog>()
                .template(mongoTemplate)
                .collection("security_logs")
                .primaryKeys(List.of("attackerId", "timestamp"))
                .mode(MongoItemWriter.Mode.UPSERT)
                .build();
    }

    private String analyzeAttackPattern(SecurityLog securityLog) {
        String command = securityLog.getCommand().toLowerCase();

        // Lateral Movement 탐지
        if (command.contains("ssh") || command.contains("telnet")) {
            return "Lateral_Movement";
        }

        // Privilege Escalation 탐지
        if (command.contains("sudo") || command.contains("su ")) {
            return "Privilege_Escalation";
        }

        // Defense Evasion 탐지
        if (command.contains("history -c") || command.contains("rm /var/log") || command.contains("killall rsyslog")) {
            return "Defense_Evasion";
        }

        return "UNKNOWN";
    }

    @Document
    @Data
    public static class SecurityLog {
        @Id
        private String id;
        private String attackerId;
        private String command;
        private LocalDateTime timestamp;
        private String label;
    }
}
