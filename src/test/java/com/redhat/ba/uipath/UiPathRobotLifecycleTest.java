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

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class UiPathRobotLifecycleTest {

    private static Logger log = LoggerFactory.getLogger("UiPathRobotLifecycleTest");


    @Before
    public void prepare() throws Exception {

    }


    @Test
    //@Ignore
    public void testGetRobots() {
        
        try{
            String robotNameFilter = null;
            Map<String, JSONObject> robots = UiPathRobotLifecycle.getRobots(robotNameFilter);
            Assert.assertTrue(robots.size() > 0);
            for(String robotKey : robots.keySet()) {
                JSONObject uipathRobotObj = robots.get(robotKey);
                log.info("robotKey = "+robotKey+ " : robot details = "+uipathRobotObj.toString());
            }
        }catch(Exception x){
            x.printStackTrace();
        }
    }

    @Test
    @Ignore
    public void testGetReleases() {
        
        try{
            Map<String, JSONObject> releases = UiPathRobotLifecycle.getReleases();
            Assert.assertTrue(releases.size() > 0);
            for(String processKey : releases.keySet()) {
                JSONObject uipathReleaseObj = releases.get(processKey);
                log.info("processKey = "+processKey+ " : process = "+uipathReleaseObj.getString(UiPathRobotLifecycle.UIPATH_RELEASE_DESCRIPTION));
            }
        }catch(Exception x){
            x.printStackTrace();
        }
    }


    @Test
    @Ignore
    public void testStartAllJobs() {
        try {
            int jobsCount = 1;
            String robotNameFilter = null;
            StringBuilder robotIdBuilder = new StringBuilder();
            /*
            Map<String, JSONObject> robots = UiPathRobotLifecycle.getRobots(robotNameFilter);
            int t = 0;
            for(String robotId : robots.keySet()) {
                if(t > 0)
                    robotIdBuilder.append(",");
                robotIdBuilder.append(robotId);
                t++;
            }
            */

            Map<String, JSONObject> releases = UiPathRobotLifecycle.getReleases();
            for(String processKey : releases.keySet()) {
                log.info("starting process with key = "+processKey);
                UiPathRobotLifecycle.startJob(processKey, UiPathRobotLifecycle.UIPATH_STRATEGY_SPECIFIC, robotIdBuilder.toString(), jobsCount, UiPathRobotLifecycle.UIPATH_SOURCE_MANUAL);
            }
        }catch(UiPathBusinessException x){
            x.printStackTrace();
        }catch(Exception y){
            y.printStackTrace();
        }
    }
}
