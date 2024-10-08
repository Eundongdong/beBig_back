//package beBig.controller;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.JobParametersBuilder;
//import org.springframework.batch.core.launch.JobLauncher;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//public class JobController {
//
//    @Autowired
//    private JobLauncher jobLauncher;
//
//    @Autowired
//    private Job job;
//
//    @GetMapping("/run-batch")
//    public String runBatchJob() throws Exception {
//        jobLauncher.run(job, new JobParametersBuilder()
//                .addLong("time", System.currentTimeMillis())
//                .toJobParameters());
//        return "Batch job has been invoked!";
//    }
//}
//
