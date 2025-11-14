package com.system.batch.kill_batch_system;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
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
    public CommandLineRunner runner(JobLauncher jobLauncher, Job logAnalysisJob) {
        return args -> {
            JobParameters params = new JobParametersBuilder()
                    .addString("inputFile", "log.txt")
                    .addLong("timestamp", System.currentTimeMillis()) // 유니크하게 만들기
                    .toJobParameters();

            jobLauncher.run(logAnalysisJob, params);
        };
    }

}
