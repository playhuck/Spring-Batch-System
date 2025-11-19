package com.system.batch.kill_batch_system.rdb;

import com.system.batch.kill_batch_system.rdb.entity.Post;
import com.system.batch.kill_batch_system.rdb.entity.Report;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class PostBlockBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job postBlockBatchJob(Step postBlockStep) {
        return new JobBuilder("postBlockBatchJob", jobRepository)
                .start(postBlockStep)
                .build();
    }

    @Bean
    public Step postBlockStep(PostBlockProcessor postBlockProcessor) {
        return new StepBuilder("postBlockStep", jobRepository)
                .<Post, Post>chunk(5, transactionManager)
                .reader(postBlockReader())
                .processor(postBlockProcessor)
                .writer(postBlockWriter())
                .build();
    }

    @Bean
    @StepScope
    public JpaCursorItemReader<Post> postBlockReader(
    ) {
        return new JpaCursorItemReaderBuilder<Post>()
                .name("postBlockReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("""
                        SELECT p FROM Post p JOIN FETCH p.reports r
                        WHERE r.reportedAt >= :startDateTime AND r.reportedAt < :endDateTime
                        """)
                .build();
    }

    @Bean
    public JpaItemWriter<Post> postBlockWriter() {
        return new JpaItemWriterBuilder<Post>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(false) // 사실 기본값이 false이다.
                .build();
    }

    @Component
    public static class PostBlockProcessor implements ItemProcessor<Post, Post> {

        @Override
        public Post process(Post post) {
            // 각 신고의 신뢰도를 기반으로 차단 점수 계산
            double blockScore = calculateBlockScore(post.getReports());

            // 차단 점수가 기준치를 넘으면 처형 결정
            if (blockScore >= 7.0) {
                post.setBlockedAt(LocalDateTime.now());
                return post;
            }

            return null;  // 무죄 방면
        }

        private double calculateBlockScore(List<Report> reports) {
            // 각 신고들의 정보를 시그니처에 포함시켜 마치 사용하는 것처럼 보이지만...
            for (Report report : reports) {
                analyzeReportType(report.getReportType());            // 신고 유형 분석
                checkReporterTrust(report.getReporterLevel());        // 신고자 신뢰도 확인
                validateEvidence(report.getEvidenceData());           // 증거 데이터 검증
                calculateTimeValidity(report.getReportedAt());        // 시간 가중치 계산
            }

            // 실제로는 그냥 랜덤 값을 반환
            return Math.random() * 10;  // 0~10 사이의 랜덤 값
        }

        // 아래는 실제로는 아무것도 하지 않는 메서드들
        private void analyzeReportType(String reportType) {
            // 신고 유형 분석하는 척
        }

        private void checkReporterTrust(int reporterLevel) {
            // 신고자 신뢰도 확인하는 척
        }

        private void validateEvidence(String evidenceData) {
            // 증거 검증하는 척
        }

        private void calculateTimeValidity(LocalDateTime reportedAt) {
            // 시간 가중치 계산하는 척
        }
    }

}
