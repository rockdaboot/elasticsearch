/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the "Elastic License
 * 2.0", the "GNU Affero General Public License v3.0 only", and the "Server Side
 * Public License v 1"; you may not use this file except in compliance with, at
 * your election, the "Elastic License 2.0", the "GNU Affero General Public
 * License v3.0 only", or the "Server Side Public License, v 1".
 */

package org.elasticsearch.action.admin.cluster.storedscripts;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.master.TransportMasterNodeReadAction;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.project.ProjectResolver;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.util.concurrent.EsExecutors;
import org.elasticsearch.injection.guice.Inject;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

public class TransportGetStoredScriptAction extends TransportMasterNodeReadAction<GetStoredScriptRequest, GetStoredScriptResponse> {

    private final ProjectResolver projectResolver;

    @Inject
    public TransportGetStoredScriptAction(
        TransportService transportService,
        ClusterService clusterService,
        ThreadPool threadPool,
        ActionFilters actionFilters,
        ProjectResolver projectResolver
    ) {
        super(
            GetStoredScriptAction.NAME,
            transportService,
            clusterService,
            threadPool,
            actionFilters,
            GetStoredScriptRequest::new,
            GetStoredScriptResponse::new,
            EsExecutors.DIRECT_EXECUTOR_SERVICE
        );
        this.projectResolver = projectResolver;
    }

    @Override
    protected void masterOperation(
        Task task,
        GetStoredScriptRequest request,
        ClusterState state,
        ActionListener<GetStoredScriptResponse> listener
    ) throws Exception {
        listener.onResponse(new GetStoredScriptResponse(request.id(), ScriptService.getStoredScript(state, request)));
    }

    @Override
    protected ClusterBlockException checkBlock(GetStoredScriptRequest request, ClusterState state) {
        return state.blocks().globalBlockedException(projectResolver.getProjectId(), ClusterBlockLevel.METADATA_READ);
    }

}
