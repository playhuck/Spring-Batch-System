//package com.system.batch.kill_batch_system.jobparameters;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.configuration.annotation.StepScope;
//import org.springframework.batch.core.job.builder.JobBuilder;
//import org.springframework.batch.core.repository.JobRepository;
//import org.springframework.batch.core.step.builder.StepBuilder;
//import org.springframework.batch.core.step.tasklet.Tasklet;
//import org.springframework.batch.repeat.RepeatStatus;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.transaction.PlatformTransactionManager;
//
///*
//   - implementation 'org.springframework.boot:spring-boot-starter-json' 추가 필요
//   - ./gradlew bootRun --args="--spring.batch.job.name=terminatorJob infiltrationTargets='{\"value\":\"판교서버실,안산데이터센터\",\"type\":\"java.lang.String\"}'"
//   - JSON을 JobParameters로 쓸 때는 \ escape 처리 해줘야 한다.
// */
//@Slf4j
//@Configuration
//public class JobParametersJson {
//
//    @Bean
//    public Job terminatorJsonJob(
//            JobRepository jobRepository,
//            Step questDifficultyStep
//    ) {
//        return new JobBuilder("terminatorJsonJob", jobRepository)
//                .start(questDifficultyStep)
//                .build();
//    }
//
//    @Bean
//    public Step terminatorJsonStep(
//            JobRepository jobRepository,
//            PlatformTransactionManager platformTransactionManager,
//            Tasklet terminationTasklet
//    ) {
//
//        return new StepBuilder("terminatorJsonStep", jobRepository)
//                .tasklet(terminationTasklet, platformTransactionManager)
//                .build();
//    }
//
//    @Bean
//    @StepScope
//    public Tasklet terminatorTasklet(
//            @Value("#{jobParameters['infiltrationTargets']}") String infiltrationTargets
//    ) {
//        return (contribution, chunkContext) -> {
//            String[] targets = infiltrationTargets.split(",");
//
//            log.info("⚡ 침투 작전 개시");
//            log.info("첫 번째 타겟: {} 침투 시작", targets[0]);
//            log.info("마지막 타겟: {} 에서 집결", targets[1]);
//            log.info("🎯 임무 전달 완료");
//
//            return RepeatStatus.FINISHED;
//        };
//    }
//
//}
