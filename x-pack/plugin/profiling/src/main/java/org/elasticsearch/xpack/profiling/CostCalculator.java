/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.xpack.profiling;

import java.util.Map;

final class CostCalculator {
    private static final double DEFAULT_SAMPLING_FREQUENCY = 20.0d;
    private static final double SECONDS_PER_HOUR = 60 * 60;
    private static final double SECONDS_PER_YEAR = SECONDS_PER_HOUR * 24 * 365.0d; // unit: seconds
    private static final double DEFAULT_COST_USD_PER_CORE_HOUR = 0.0425d; // unit: USD / (core * hour)
    private static final double DEFAULT_AWS_COST_FACTOR = 1.0d;
    private final InstanceTypeService instanceTypeService;
    private final Map<String, HostMetadata> hostMetadata;
    private final double samplingDurationInSeconds;
    private final double awsCostFactor;
    private final double customCostPerCoreHour;
    private final boolean explain;
    private final Details details = new Details();

    CostCalculator(
        InstanceTypeService instanceTypeService,
        Map<String, HostMetadata> hostMetadata,
        double samplingDurationInSeconds,
        Double awsCostFactor,
        Double customCostPerCoreHour,
        boolean explain
    ) {
        this.instanceTypeService = instanceTypeService;
        this.hostMetadata = hostMetadata;
        this.samplingDurationInSeconds = samplingDurationInSeconds > 0 ? samplingDurationInSeconds : 1.0d; // avoid division by zero
        this.awsCostFactor = awsCostFactor == null ? DEFAULT_AWS_COST_FACTOR : awsCostFactor;
        this.customCostPerCoreHour = customCostPerCoreHour == null ? DEFAULT_COST_USD_PER_CORE_HOUR : customCostPerCoreHour;
        this.explain = explain;
    }

    public double annualCostsUSD(String hostID, long samples) {
        double annualCoreHours = annualCoreHours(samplingDurationInSeconds, samples, DEFAULT_SAMPLING_FREQUENCY);
        details.duration = samplingDurationInSeconds;
        details.samples = samples;
        details.samplingFrequency = DEFAULT_SAMPLING_FREQUENCY;
        details.annualCoreHours = annualCoreHours;

        HostMetadata host = hostMetadata.get(hostID);
        if (host == null) {
            details.customCostPerCoreHour = customCostPerCoreHour;
            return annualCoreHours * customCostPerCoreHour;
        }
        details.instanceType = host.instanceType;

        double providerCostFactor = host.instanceType.provider.equals("aws") ? awsCostFactor : 1.0d;
        details.providerCostFactor = providerCostFactor;

        CostEntry costs = instanceTypeService.getCosts(host.instanceType);
        if (costs == null) {
            details.customCostPerCoreHour = customCostPerCoreHour;
            return annualCoreHours * customCostPerCoreHour * providerCostFactor;
        }
        details.costFactor = costs.costFactor;

        return annualCoreHours * costs.costFactor * providerCostFactor;
    }

    public static double annualCoreHours(double duration, double samples, double samplingFrequency) {
        // samplingFrequency will a variable value when we start supporting probabilistic profiling (soon).
        return (SECONDS_PER_YEAR / duration * samples / samplingFrequency) / SECONDS_PER_HOUR; // unit: core * hour
    }

    public Details getDetails() {
        if (explain == false) {
            return null;
        }
        return details;
    }

    public static class Details {
        double duration;
        double samples;
        double samplingFrequency;
        double annualCoreHours;
        double customCostPerCoreHour;
        double providerCostFactor;
        double costFactor;
        InstanceType instanceType;
    }
}
