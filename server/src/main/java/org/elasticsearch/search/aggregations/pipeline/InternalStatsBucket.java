/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the "Elastic License
 * 2.0", the "GNU Affero General Public License v3.0 only", and the "Server Side
 * Public License v 1"; you may not use this file except in compliance with, at
 * your election, the "Elastic License 2.0", the "GNU Affero General Public
 * License v3.0 only", or the "Server Side Public License, v 1".
 */

package org.elasticsearch.search.aggregations.pipeline;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.search.DocValueFormat;
import org.elasticsearch.search.aggregations.AggregationReduceContext;
import org.elasticsearch.search.aggregations.AggregatorReducer;
import org.elasticsearch.search.aggregations.metrics.InternalStats;

import java.io.IOException;
import java.util.Map;

public class InternalStatsBucket extends InternalStats implements StatsBucket {
    public InternalStatsBucket(
        String name,
        long count,
        double sum,
        double min,
        double max,
        DocValueFormat formatter,
        Map<String, Object> metadata
    ) {
        super(name, count, sum, min, max, formatter, metadata);
    }

    /**
     * Read from a stream.
     */
    public InternalStatsBucket(StreamInput in) throws IOException {
        super(in);
    }

    @Override
    public String getWriteableName() {
        return StatsBucketPipelineAggregationBuilder.NAME;
    }

    @Override
    protected AggregatorReducer getLeaderReducer(AggregationReduceContext reduceContext, int size) {
        throw new UnsupportedOperationException("Not supported");
    }

}
