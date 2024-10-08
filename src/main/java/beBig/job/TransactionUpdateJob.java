package beBig.job;

import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TransactionUpdateJob extends AbstractBatchJob {

    @Autowired
    public TransactionUpdateJob(@Qualifier("transactionUpdateJob") Job transactionUpdateJob) {
        super(transactionUpdateJob);
    }
}
