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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SpringBootApplication
public class KillBatchSystemApplication {

	public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(KillBatchSystemApplication.class, args)));
	}

    @Bean
    public CommandLineRunner runner(JobLauncher jobLauncher,  @Qualifier("aggregateHackerAttackJob") Job aggregateHackerAttackJob) {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter dateTimeFormatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime start = LocalDateTime.parse("2025-11-15 03:48:47", dateTimeFormatter);
        LocalDateTime end = LocalDateTime.parse("2025-11-18 03:38:50", dateTimeFormatter);

//        LocalDateTime time =  LocalDateTime.parse("2025-08-03", dateTimeFormatter2);
        LocalDate time2 =  LocalDate.parse("2025-08-03", dateTimeFormatter2);

        return args -> {
            JobParameters params = new JobParametersBuilder()
//                    .addLocalDateTime("startDateTime", start)
//                    .addLocalDateTime("endDateTime", end)
//                    .addLocalDate("searchDate", LocalDate.now())
//                    .addLong("timestamp", System.currentTimeMillis()) // 유니크하게 만들기
                    .toJobParameters();

            jobLauncher.run(aggregateHackerAttackJob, params);
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
