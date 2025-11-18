package com.system.batch.kill_batch_system;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SpringBootApplication
public class KillBatchSystemApplication {

	public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(KillBatchSystemApplication.class, args)));
	}

    @Bean
    public CommandLineRunner runner(JobLauncher jobLauncher,  @Qualifier("postBlockBatchJob") Job postBlockBatchJob) {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime start = LocalDateTime.parse("2025-11-15 03:48:47", dateTimeFormatter);
        LocalDateTime end = LocalDateTime.parse("2025-11-18 03:38:50", dateTimeFormatter);

        return args -> {
            JobParameters params = new JobParametersBuilder()
                    .addLocalDateTime("startDateTime", start)
                    .addLocalDateTime("endDateTime", end)
                    .addLong("timestamp", System.currentTimeMillis()) // 유니크하게 만들기
                    .toJobParameters();

            jobLauncher.run(postBlockBatchJob, params);
        };
    }

    // 다중 파일 읽기
//    @Bean
//    public CommandLineRunner runner(JobLauncher jobLauncher, Job systemFailureJob) {
//        return args -> {
//            JobParameters params = new JobParametersBuilder()
//                    .addString("inputFilePath", "critical-failures.csv,normal-failures.csv")
//                    .addLong("timestamp", System.currentTimeMillis()) // 유니크하게 만들기
//                    .toJobParameters();
//
//            jobLauncher.run(systemFailureJob, params);
//        };
//    }

    // 단일 파일 읽기
//    @Bean
//    public CommandLineRunner runner(JobLauncher jobLauncher, Job systemLogJob) {
//        return args -> {
//            JobParameters params = new JobParametersBuilder()
//                    .addString("inputFile", "system-events.log")
//                    .addLong("timestamp", System.currentTimeMillis()) // 유니크하게 만들기
//                    .toJobParameters();
//
//            jobLauncher.run(systemLogJob, params);
//        };
//    }

}
