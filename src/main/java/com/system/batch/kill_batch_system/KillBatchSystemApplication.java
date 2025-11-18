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

@SpringBootApplication
public class KillBatchSystemApplication {

	public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(KillBatchSystemApplication.class, args)));
	}

    @Bean
    public CommandLineRunner runner(JobLauncher jobLauncher,  @Qualifier("orderRecoveryJob") Job orderRecoveryJob) {
        return args -> {
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis()) // 유니크하게 만들기
                    .toJobParameters();

            jobLauncher.run(orderRecoveryJob, params);
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
