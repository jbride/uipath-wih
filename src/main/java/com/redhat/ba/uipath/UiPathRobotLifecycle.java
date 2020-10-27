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

    public static final String VALUE = "value";
    public static final String KEY = "Key";

    private static Logger logger = LoggerFactory.getLogger(UiPathRobotLifecycle.class.getName());

    private static String orchestratorUrl;
    private static String accountLogicalName;
    private static String tenantName;
    private static String orgUnitId;

    static {
        orchestratorUrl = System.getProperty(UIPATH_PUBLIC_ORCHESTRATOR_URL, "https://cloud.uipath.com/");

        accountLogicalName = System.getProperty(UIPATH_ACCOUNT_LOGICAL_NAME);
        if(StringUtils.isEmpty(accountLogicalName))
            throw new RuntimeException("Need to provide system property: "+ UIPATH_ACCOUNT_LOGICAL_NAME);

        tenantName = System.getProperty(UIPATH_TENANT_NAME);
        if(StringUtils.isEmpty(tenantName))
            throw new RuntimeException("Need to provide system property: "+ UIPATH_TENANT_NAME);

        orgUnitId = System.getProperty(UIPATH_ORG_UNIT_ID);
        if(StringUtils.isEmpty(orgUnitId))
            throw new RuntimeException("Need to provide system property: "+ UIPATH_ORG_UNIT_ID);
    }

    public static Map<String, JSONObject> getReleases() throws UiPathCommunicationException {

        StringBuilder releasesUrl = new StringBuilder(orchestratorUrl + accountLogicalName+"/"+tenantName+"/odata/Releases");
        Map<String, JSONObject> releases = new HashMap<String, JSONObject>();
        try {
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

        }catch(IOException x){
            logger.error("getReleases() exception invoking the following Url: "+releasesUrl.toString());
            x.printStackTrace();
        }

        return releases;
    }

    public static void startJob(String processReleaseKey) {
        StringBuilder startJobUrl = new StringBuilder(orchestratorUrl + accountLogicalName+"/"+tenantName+"/odata/Jobs/UiPath.Server.Configuration.OData.StartJobs");
        try {
            HttpPost httpPost = new HttpPost(startJobUrl.toString());

            String token = UiPathTokenLifecycle.getUiPathToken();
            httpPost.setHeader("Authorization", "Bearer " + token);
            httpPost.setHeader("X-UIPATH-OrganizationUnitId", orgUnitId);
            HttpClient httpclient = HttpClientBuilder.create().build();

            httpPost.setHeader("Content-Type", "application/json");
            StringBuilder startJobEntity = new StringBuilder();
            startJobEntity.append("{\"startInfo\":");
            startJobEntity.append("{\"ReleaseKey\":\""+processReleaseKey+"\",");
            startJobEntity.append("\"RobotIds\":[],");
            startJobEntity.append("\"JobsCount\":1,");
            startJobEntity.append("\"JobPriority\":\"Normal\",");
            startJobEntity.append("\"Strategy\":\"ModernJobsCount\"");
            startJobEntity.append("}}");
            httpPost.setEntity(new StringEntity(startJobEntity.toString()));

            HttpResponse getResponse = httpclient.execute(httpPost);
            final int getStatusCode = getResponse.getStatusLine().getStatusCode();
            final String responseBody = EntityUtils.toString(getResponse.getEntity());
            httpPost.releaseConnection();
            logger.info("getReleases() statusCode = "+getStatusCode+" : responseBody = "+responseBody);

        }catch(Exception x) {

        }

    }
    
}
