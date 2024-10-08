package beBig.job;

import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class AgeUpdateJob extends AbstractBatchJob {

    @Autowired
    public AgeUpdateJob(@Qualifier("ageUpdateJob") Job ageUpdateJob) {
        super(ageUpdateJob);
    }
}
