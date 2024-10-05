package beBig.service;

import org.springframework.stereotype.Service;

import java.io.IOException;

public interface DepositApiService {
    void fetchAndSaveDepositData() throws IOException;
}

