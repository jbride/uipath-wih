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

import java.io.IOException;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.RequiredParameterValidator;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidAuth;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.json.JSONObject;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom WIH that manages robots via UiPath Orchestrator
 * 
 * @author jbride
 */
@Wid(widfile="UiPathWorkItem.wid", name="UiPathWorkItem",
        displayName="UiPathWorkItem",
        defaultHandler="mvel: new com.redhat.ba.uipath.UiPathOrchestratorWIH()",
        documentation = "uipath-workitem/index.html",
        category = "uipath-workitem",
        icon = "uipath.png",
        parameters={
            @WidParameter(name=UiPathOrchestratorWIH.ACTION, type="new StringDataType()", runtimeType="String", required = true),
            @WidParameter(name=UiPathRobotLifecycle.UIPATH_PROCESS_KEY, type="new StringDataType()", runtimeType="String", required = false)
        },
        results={
            @WidResult(name="result")
        },
        mavenDepends={
        },
        serviceInfo = @WidService(category = "uipath-workitem", description = "${description}",
                keywords = "",
                action = @WidAction(title = "UiPathWorkItem"),
                authinfo = @WidAuth(required = false, params = {},
                    paramsdescription = {})
        )
)
public class UiPathOrchestratorWIH extends AbstractLogOrThrowWorkItemHandler {

    public static final String ACTION = "ACTION";
    private static Logger logger = LoggerFactory.getLogger(UiPathOrchestratorWIH.class);

	public UiPathOrchestratorWIH() {
	}

        public void executeWorkItem(WorkItem workItem, WorkItemManager wim) {
            try{
                Object actionObj = workItem.getParameter(ACTION);
                if(actionObj != null && StringUtils.isEmpty((String)actionObj)){
                    String action = (String)actionObj;
                    if(action.equals(UiPathRobotLifecycle.ACTION_GET_RELEASES)) {
                        Map<String, JSONObject> releases = UiPathRobotLifecycle.getReleases();
                        logger.info("executeWorkItem() number of releases = "+releases.size());
                    } else if(action.equals(UiPathRobotLifecycle.ACTION_START_JOB)){
                        Object keyObj = workItem.getParameter(UiPathRobotLifecycle.UIPATH_PROCESS_KEY);
                        if(keyObj != null && StringUtils.isEmpty((String)keyObj)) {
                            String processReleaseKey = (String)keyObj;
                            int jobsCount = 1;
                            StringBuilder robotIdBuilder = new StringBuilder();
                            UiPathRobotLifecycle.startJob(processReleaseKey, UiPathRobotLifecycle.UIPATH_STRATEGY_SPECIFIC, robotIdBuilder.toString(), jobsCount, UiPathRobotLifecycle.UIPATH_SOURCE_MANUAL);
                            logger.info("executeWorkItem() just started UiPath job with id = "+processReleaseKey);
                        }else {
                                throw new RuntimeException("executeWorkItem() must pass a String parameter of: "+UiPathRobotLifecycle.UIPATH_PROCESS_KEY);
                        }
                    }else {
                        throw new RuntimeException("executeWorkItem() Value of ACTION parameter is invalid: "+ACTION+" .\n\tValid values as follows: "+UiPathRobotLifecycle.ACTION_VALID_VALUES);
                    }
                }else {
                    throw new RuntimeException("executeWorkItem() must pass a String parameter of: "+ACTION+" with a value from the following list: "+UiPathRobotLifecycle.ACTION_VALID_VALUES);
                }
            }catch(IOException | UiPathCommunicationException | UiPathBusinessException x){
                x.printStackTrace();
                throw new RuntimeException(x);
            }
        }

        public void abortWorkItem(WorkItem wi, WorkItemManager wim) {
        }
}
