package com.rentalpro.service;

import com.rentalpro.model.dto.response.AIBenchmarkResponse;

import java.util.UUID;

public interface RentAnalyzerService {

    AIBenchmarkResponse calculateFairRent(UUID propertyId);
}