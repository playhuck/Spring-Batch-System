//package com.system.batch.kill_batch_system.jobparameters;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.batch.core.JobParameters;
//import org.springframework.batch.core.StepContribution;
//import org.springframework.batch.core.scope.context.ChunkContext;
//import org.springframework.batch.core.step.tasklet.Tasklet;
//import org.springframework.batch.repeat.RepeatStatus;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//public class JobParametersExecution implements Tasklet {
//    @Override
//    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
//        JobParameters jobParameters = chunkContext.getStepContext()
//                .getStepExecution()
//                .getJobParameters();
//
//        String targetSystem = jobParameters.getString("system.target");
//        long destructionLevel = jobParameters.getLong("system.destruction.level");
//
//        log.info("타겟 시스템: {}", targetSystem);
//        log.info("파괴 레벨: {}", destructionLevel);
//
//        return RepeatStatus.FINISHED;
//    }
//}
