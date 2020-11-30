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

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.charset.StandardCharsets;

public class UiPathRobotLifecycle {

    public static final String UIPATH_PUBLIC_ORCHESTRATOR_URL = "UIPATH_PUBLIC_ORCHESTRATOR_URL";
    public static final String UIPATH_ACCOUNT_LOGICAL_NAME = "UIPATH_ACCOUNT_LOGICAL_NAME";
    public static final String UIPATH_TENANT_NAME = "UIPATH_TENANT_NAME";
    public static final String UIPATH_ORG_UNIT_ID = "UIPATH_ORG_UNIT_ID";
    public static final String UIPATH_PROCESS_KEY = "PROCESS_KEY";
    public static final String UIPATH_RELEASE_DESCRIPTION = "Description";
    public static final String UIPATH_ROBOT_ID = "Id";
    public static final String UIPATH_STRATEGY_SPECIFIC = "Specific";
    public static final String UIPATH_STRATEGY_MODERN_JOBS_COUNT = "ModernJobsCount";
    public static final String UIPATH_SOURCE_MANUAL = "Manual";

    public static final String ACTION_GET_RELEASES="GET_RELEASES";
    public static final String ACTION_START_JOB = "START_JOB";
    public static final String ACTION_VALID_VALUES = ACTION_GET_RELEASES+","+ACTION_START_JOB;

    public static final String VALUE = "value";
    public static final String KEY = "Key";

    private static Logger logger = LoggerFactory.getLogger(UiPathRobotLifecycle.class.getName());

    private static String orchestratorUrl = "https://cloud.uipath.com/";
    private static String accountLogicalName;
    private static String tenantName;
    private static String orgUnitId;

    static {
        if(StringUtils.isNotEmpty(System.getenv(UIPATH_PUBLIC_ORCHESTRATOR_URL)))
            orchestratorUrl = System.getenv(UIPATH_PUBLIC_ORCHESTRATOR_URL);

        accountLogicalName = System.getenv(UIPATH_ACCOUNT_LOGICAL_NAME);
        if(StringUtils.isEmpty(accountLogicalName))
            throw new RuntimeException("Need to provide env var: "+ UIPATH_ACCOUNT_LOGICAL_NAME);

        tenantName = System.getenv(UIPATH_TENANT_NAME);
        if(StringUtils.isEmpty(tenantName))
            throw new RuntimeException("Need to provide env var: "+ UIPATH_TENANT_NAME);

        orgUnitId = System.getenv(UIPATH_ORG_UNIT_ID);
        if(StringUtils.isEmpty(orgUnitId))
            throw new RuntimeException("Need to provide env var: "+ UIPATH_ORG_UNIT_ID);
    }

    /**
     * 
     * @return JSONObject that is structured as per the following example:  https://gist.github.com/jbride/1b9b6e03756dfcdf0c63c2b675f910ba
     * @throws UiPathCommunicationException
     * @throws IOException
     */
    public static Map<String, JSONObject> getReleases() throws UiPathCommunicationException, IOException {

        StringBuilder releasesUrl = new StringBuilder(orchestratorUrl + accountLogicalName+"/"+tenantName+"/odata/Releases");
        Map<String, JSONObject> releases = new HashMap<String, JSONObject>();
        HttpGet httpGet = new HttpGet(releasesUrl.toString());
        httpGet.setHeader("Content-Type", "application/json");
        String token = UiPathTokenLifecycle.getUiPathToken();
        httpGet.setHeader("Authorization", "Bearer " + token);
        HttpClient httpclient = HttpClientBuilder.create().build();
        HttpResponse getResponse = httpclient.execute(httpGet);
        final int getStatusCode = getResponse.getStatusLine().getStatusCode();
        final String responseBody = EntityUtils.toString(getResponse.getEntity());
        httpGet.releaseConnection();
        logger.info("getReleases() statusCode = "+getStatusCode+" : responseBody = "+responseBody);
        JSONObject jsonResponse = new JSONObject(responseBody);
        JSONArray values = jsonResponse.getJSONArray(VALUE);
        if(values.length() > 0) {
            for(int i=0; i < values.length(); i++) {
                JSONObject value = values.getJSONObject(i);
                String key = StringUtils.strip(value.getString(KEY), "\"");
                logger.info("getReleases() adding release to map with key = "+key);
                releases.put(key, value);
            }
        }

        return releases;
    }

    /**
     * 
     * @param robotNameFilter
     * @return
     * @throws UiPathCommunicationException
     * @throws UiPathBusinessException
     * @throws IOException
     */
    public static Map<String, JSONObject> getRobots(String robotNameFilter) throws UiPathCommunicationException, UiPathBusinessException, IOException {
        StringBuilder robotsUrl = new StringBuilder(orchestratorUrl + accountLogicalName+"/"+tenantName+"/odata/Robots");
        Map<String, JSONObject> robots = new HashMap<String, JSONObject>();
        HttpGet httpGet = new HttpGet(robotsUrl.toString());
        httpGet.setHeader("Content-Type", "application/json");
        String token = UiPathTokenLifecycle.getUiPathToken();
        httpGet.setHeader("Authorization", "Bearer " + token);
        httpGet.setHeader("X-UIPATH-OrganizationUnitId", orgUnitId);
        HttpClient httpclient = HttpClientBuilder.create().build();
        HttpResponse getResponse = httpclient.execute(httpGet);
        final int statusCode = getResponse.getStatusLine().getStatusCode();
        final String responseBody = EntityUtils.toString(getResponse.getEntity());
        httpGet.releaseConnection();
        logger.info("getRobots() statusCode = "+statusCode+" : responseBody = "+responseBody);
        if(statusCode == 200) {
            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONArray values = jsonResponse.getJSONArray(VALUE);
            if(values.length() > 0) {
                for(int i=0; i < values.length(); i++) {
                    JSONObject value = values.getJSONObject(i);
                    String key = StringUtils.strip(value.getString(UIPATH_ROBOT_ID), "\"");
                    logger.info("getRobots() adding robot to map with key = "+key);
                    robots.put(key, value);
                }
            }
            return robots;
        }else {
            StringBuilder sBuilder = new StringBuilder();
            sBuilder.append("getRobots() response statusCode = "+statusCode);
            sBuilder.append("\n\tresponseBody = "+responseBody);
            sBuilder.append("\n\taccountLogicalName = "+accountLogicalName);
            sBuilder.append("\n\ttenantName = "+tenantName);
            throw new UiPathBusinessException(sBuilder.toString());
        }
    }
    

    /**
     * 
     * @param processReleaseKey
     * @param strategy                                     : Specific or ModernJobsCount
     * @param commaDelimitedRobotIds
     * @param jobsCount
     * @param source                                       : Manual
     * @throws UiPathCommunicationException
     * @throws UiPathBusinessException
     * @throws IOException
     */
    public static void startJob(String processReleaseKey, String strategy, String commaDelimitedRobotIds, int jobsCount, String source) throws UiPathCommunicationException, UiPathBusinessException, IOException {
        StringBuilder startJobUrl = new StringBuilder(orchestratorUrl + accountLogicalName+"/"+tenantName+"/odata/Jobs/UiPath.Server.Configuration.OData.StartJobs");
        HttpPost httpPost = new HttpPost(startJobUrl.toString());

        String token = UiPathTokenLifecycle.getUiPathToken();
        httpPost.setHeader("Authorization", "Bearer " + token);
        httpPost.setHeader("X-UIPATH-OrganizationUnitId", orgUnitId);
        HttpClient httpclient = HttpClientBuilder.create().build();

        httpPost.setHeader("Content-Type", "application/json");
        StringBuilder startJobEntity = new StringBuilder();
        startJobEntity.append("{\"startInfo\":");
        startJobEntity.append("{\"ReleaseKey\":\""+processReleaseKey+"\",");
        startJobEntity.append("\"Strategy\":\""+strategy+"\",");
        startJobEntity.append("\"RobotIds\":["+commaDelimitedRobotIds+"],");
        startJobEntity.append("\"JobsCount\":"+jobsCount+",");
        //startJobEntity.append("\"JobPriority\":\"Normal\",");
        startJobEntity.append("\"Source\":\""+source+"\"");
        startJobEntity.append("}}");
        httpPost.setEntity(new StringEntity(startJobEntity.toString()));

        HttpResponse getResponse = httpclient.execute(httpPost);
        final int statusCode = getResponse.getStatusLine().getStatusCode();
        final String responseBody = EntityUtils.toString(getResponse.getEntity());
        httpPost.releaseConnection();
        logger.info("startJob() statusCode = "+statusCode+" : responseBody = "+responseBody);

        if(statusCode != 200) {
            StringBuilder sBuilder = new StringBuilder();
            sBuilder.append("startJob() response statusCode = "+statusCode);
            sBuilder.append("\n\tresponseBody = "+responseBody);
            sBuilder.append("\n\tprocessReleaseKey = "+processReleaseKey);
            sBuilder.append("\n\taccountLogicalName = "+accountLogicalName);
            sBuilder.append("\n\ttenantName = "+tenantName);
            sBuilder.append("\n\tstrategy = "+strategy);
            sBuilder.append("\n\tcommaDelimitedRobotIds = "+commaDelimitedRobotIds);
            sBuilder.append("\n\tjobsCount = "+jobsCount);
            throw new UiPathBusinessException(sBuilder.toString());
        }

    }
    
}
