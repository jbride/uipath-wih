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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONArray;
import org.json.JSONException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UiPathTokenLifecycle {

    public static final String UIPATH_TOKEN_URL = "UIPATH_TOKEN_URL";
    public static final String UIPATH_CLIENT_ID = "UIPATH_CLIENT_ID";
    public static final String UIPATH_USER_KEY = "UIPATH_USER_KEY";
    public static final String UIPATH_SECONDS_TO_REFRESH_TOKEN = "UIPATH_SECONDS_TO_REFRESH_TOKEN";

    private static final String GRANT_TYPE = "refresh_token";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String EXPIRES_IN = "expires_in";
    private static final SimpleDateFormat sdfObj = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

    /* Cache UiPath Token
     *   Purpose:
     *      Allow for UiPath API rate limit ( which is applied for an individual access token across the entire UiPath Orchestrator API )
     *   Contents of cache:
     *      uiPathTokenArray[0] = String token
     *      uiPathTokenArray[1] = Calendar object indicating token expiration
    */
    private static Object[] uiPathTokenArray = new Object[2];
    private static Object syncObj = new Object();
    private static String uipathTokenUrl;
    private static String clientId;
    private static String clientSecret;
    private static int secondsToRefresh;

    private static Logger logger = LoggerFactory.getLogger(UiPathTokenLifecycle.class.getName());

    static {
        uipathTokenUrl = System.getEnv(UiPathTokenLifecycle.UIPATH_TOKEN_URL, "https://account.uipath.com/oauth/token");

        secondsToRefresh = Integer.parseInt(System.getEnv(UIPATH_SECONDS_TO_REFRESH_TOKEN, "10"));

        clientId = System.getEnv(UiPathTokenLifecycle.UIPATH_CLIENT_ID);
        if(StringUtils.isEmpty(clientId))
            throw new RuntimeException("Need to provide env var: "+UiPathTokenLifecycle.UIPATH_CLIENT_ID);
        
        clientSecret = System.getEnv(UiPathTokenLifecycle.UIPATH_USER_KEY);
        if(StringUtils.isEmpty(clientSecret))
            throw new RuntimeException("Need to provide env var: "+UiPathTokenLifecycle.UIPATH_USER_KEY);
    }

    public static String getUiPathToken() throws UiPathCommunicationException {

        String response = null;

        if(uiPathTokenArray[0] != null && (Integer)uiPathTokenArray[1] > secondsToRefresh)
            return (String)uiPathTokenArray[0];
        
        try{
            synchronized(syncObj) {
                if(uiPathTokenArray[0] != null && (Integer)uiPathTokenArray[1] > secondsToRefresh)
                    return (String)uiPathTokenArray[0];

                HttpClient httpclient = HttpClientBuilder.create().build();
        
                HttpPost post = new HttpPost(uipathTokenUrl);
                // set up name value pairs
                List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                nvps.add(new BasicNameValuePair("grant_type", GRANT_TYPE));
                nvps.add(new BasicNameValuePair("client_id", clientId));
                nvps.add(new BasicNameValuePair("refresh_token", clientSecret));
        
                // set HTTP request message body
                post.setEntity(new UrlEncodedFormEntity(nvps));
        
                // send POST message
                HttpResponse httpResponse = httpclient.execute(post);
        
                // process response
                response = EntityUtils.toString(httpResponse.getEntity());
                post.releaseConnection();
                
                JSONObject jsonResponse = new JSONObject(response);
                String token = jsonResponse.getString(ACCESS_TOKEN);
                int expiresInSeconds = jsonResponse.getInt(EXPIRES_IN);
                    
                uiPathTokenArray[0] = token;
                uiPathTokenArray[1] = new Integer(expiresInSeconds);
        
                Calendar tokenExpDate = Calendar.getInstance();
                tokenExpDate.add(Calendar.SECOND, expiresInSeconds);
                StringBuilder sBuilder = new StringBuilder("getUiPathToken() just retrieved the following UiPath token:\n\t");
                sBuilder.append("token = "+token+"\n\t");
                sBuilder.append("expires at = "+sdfObj.format(tokenExpDate.getTime()));
                logger.info(sBuilder.toString());
                return (String)uiPathTokenArray[0];
            }

        }catch(Exception x){
            handleUiPathException(x, response);
            throw new UiPathCommunicationException("UiPath Authentication Problem. Check values of "+UIPATH_CLIENT_ID+ " and "+UIPATH_USER_KEY);
        }
    }

    public static void handleUiPathException(Exception x, String uiPathResponse) {
        logger.error("handleUiPathException() UiPath Response = "+uiPathResponse);
        x.printStackTrace();
    }
}
