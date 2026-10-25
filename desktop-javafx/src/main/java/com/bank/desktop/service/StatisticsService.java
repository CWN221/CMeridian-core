package com.bank.desktop.service;

import lombok.extern.slf4j.Slf4j;

import java.util.Random;

/**
 * Service for fetching dashboard statistics.
 * Currently returns mock data - replace with real API calls later.
 */
@Slf4j
public class StatisticsService {

    private final Random random = new Random();

    public long getCustomerCount() {
        log.debug("Fetching customer count");
        // TODO: Replace with real API call:
        // return apiClient.get("/api/customers/count/active");
        return 1234;
    }

    public long getAccountCount() {
        log.debug("Fetching account count");
        return 2456;
    }

    public long getTransactionCount() {
        log.debug("Fetching transaction count");
        return 8901;
    }

    public long getLoanCount() {
        log.debug("Fetching loan count");
        return 567;
    }

    public double getCustomerChange() {
        return 12.5;
    }

    public double getAccountChange() {
        return 8.3;
    }

    public double getTransactionChange() {
        return 15.7;
    }

    public double getLoanChange() {
        return 5.2;
    }
}