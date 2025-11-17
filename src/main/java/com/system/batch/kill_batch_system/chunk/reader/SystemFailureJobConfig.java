package com.system.batch.kill_batch_system.chunk.reader;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;

/**
 *  - echo -e "에러ID,발생시각,심각도,프로세스ID,에러메시지\nERR001,2024-01-19 10:15:23,CRITICAL,1234,SYSTEM_CRASH\nERR002,2024-01-19 10:15:25,FATAL,1235,MEMORY_OVERFLOW" > system-failures.csv
 *  - ./gradlew bootRun --args='--spring.batch.job.name=systemFailureJob inputFile=/kill_batch_system/chunk/system-failures.csv'
 */
@Slf4j
@Configuration
public class SystemFailureJobConfig {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job systemFailureJob(Step systemFailureStep) {
        log.info("starting SystemFailureJob");
        return new JobBuilder("systemFailureJob", jobRepository)
                .start(systemFailureStep)
                .build();
    }

    @Bean
    public Step systemFailureStep(
            MultiResourceItemReader<SystemFailure> multiSystemFailureItemReader,
            SystemFailureStdoutItemWriter systemFailureStdoutItemWriter
    ) {
        return new StepBuilder("systemFailureStep", jobRepository)
                .<SystemFailure, SystemFailure>chunk(10, transactionManager)
                .reader(multiSystemFailureItemReader)
                .writer(systemFailureStdoutItemWriter)
                .build();
    }

    @Bean
    @StepScope
    public MultiResourceItemReader<SystemFailure> multiSystemFailureItemReader(
            @Value("#{jobParameters['inputFilePath']}") String inputFilePath) {

        Resource[] resources = Arrays.stream(inputFilePath.split(","))
                .map(String::trim)
                .peek(file -> log.info("파일 추가: {}", file))
                .map(FileSystemResource::new)
                .toArray(Resource[]::new);

        MultiResourceItemReader<SystemFailure> reader = new MultiResourceItemReader<>();
        reader.setName("multiSystemFailureItemReader");
        reader.setResources(resources);
        reader.setStrict(false);  // 파일 없어도 에러 안 남
        reader.setDelegate(systemFailureFileReader());

        return reader;
    }

    @Bean
    public FlatFileItemReader<SystemFailure> systemFailureFileReader() {
        return new FlatFileItemReaderBuilder<SystemFailure>()
                .name("systemFailureFileReader")
                .delimited()
                .delimiter(",")
                .names("errorId", "errorDateTime", "severity", "processId", "errorMessage")
                .targetType(SystemFailure.class)
                .linesToSkip(1)
                .build();
    }

    @Bean
    public SystemFailureStdoutItemWriter systemFailureStdoutItemWriter() {
        return new SystemFailureStdoutItemWriter();
    }

    public static class SystemFailureStdoutItemWriter implements ItemWriter<SystemFailure> {
        @Override
        public void write(Chunk<? extends SystemFailure> chunk) throws Exception {
            log.info("SystemFailureStdoutItemWriter");
            for (SystemFailure failure : chunk) {
                log.info("Processing system failure: {}", failure);
            }
        }
    }

    @Data
    public static class SystemFailure {
        private String errorId;
        private String errorDateTime;
        private String severity;
        private Integer processId;
        private String errorMessage;
    }

    public static class ErrorFieldSetMapper implements FieldSetMapper<SystemLog> {
        @Override
        public SystemLog mapFieldSet(FieldSet fs) throws BindException {
            ErrorLog errorLog = new ErrorLog();
            errorLog.setType(fs.readString("type"));
            errorLog.setApplication(fs.readString("application"));
            errorLog.setErrorType(fs.readString("errorType"));
            errorLog.setTimestamp(fs.readString("timestamp"));
            errorLog.setMessage(fs.readString("message"));
            errorLog.setResourceUsage(fs.readString("resourceUsage"));
            errorLog.setLogPath(fs.readString("logPath"));
            return errorLog;
        }
    }

    @Data
    public static class SystemLog {
        private String type;
        private String timestamp;
    }

    @Data
    @ToString(callSuper = true)
    public static class ErrorLog extends SystemLog {
        private String application;
        private String errorType;
        private String message;
        private String resourceUsage;
        private String logPath;
    }

}
