/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.ba.uipath;

import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidAuth;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom WIH that manages robots via UiPath Orchestrator
 * 
 * @author jbride
 */
public class UiPathOrchestratorWIH extends AbstractLogOrThrowWorkItemHandler {

	private static Logger logger = LoggerFactory.getLogger(UiPathOrchestratorWIH.class);

        private String uipathClientId;
        private String uipathUserKey;

	public UiPathOrchestratorWIH(String uipathClientId, String uipathUserKey) {
            this.uipathClientId = uipathClientId;
            this.uipathUserKey = uipathUserKey;
	}

        public void executeWorkItem(WorkItem workItem, WorkItemManager wim) {
        }

        public void abortWorkItem(WorkItem wi, WorkItemManager wim) {
        }
}
