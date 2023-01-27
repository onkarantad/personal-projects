package com.sapiens.ssi.auditing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.apache.hc.core5.http.HttpHeaders.USER_AGENT;

@Log4j2
public class RestAPI {
    public static String postRequest(String requestURL, String jsonBody) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(requestURL);
        StringEntity entity = new StringEntity(jsonBody);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        httpPost.addHeader("User-Agent", USER_AGENT);

        String result = null;
        try {
            CloseableHttpResponse response = client.execute(httpPost);
            HttpEntity entity2 = response.getEntity();
            result = EntityUtils.toString(entity2);
            if (response.getCode() != 200) {
                log.error("error in response : "+result+" | payload: "+jsonBody);
                throw new HttpException("response: "+result+" | payload: "+jsonBody);
            }
        } catch (IOException | HttpException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getRequest(String requestURL, String jsonBody) {
        HttpGet httpGet = new HttpGet(requestURL);
        CloseableHttpClient client = HttpClients.createDefault();

        StringEntity entity = new StringEntity(jsonBody);
        httpGet.setEntity(entity);
        httpGet.setHeader("Accept", "application/json");
        httpGet.setHeader("Content-type", "application/json");
        httpGet.addHeader("User-Agent", USER_AGENT);

        String result = null;
        try {
            CloseableHttpResponse response = client.execute(httpGet);
            HttpEntity entity2 = response.getEntity();
            result = EntityUtils.toString(entity2);
            if (response.getCode() != 200) {
                log.error("error in response : "+result+" | payload: "+jsonBody);
                throw new HttpException("response: "+result+" | payload: "+jsonBody);
            }
        } catch (IOException | HttpException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static List<Map<String, Object>> getResObj(String requestURL, String payLoad) {
        String jsonObject = postRequest(requestURL, payLoad);
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> resObj = null;
        try {
            resObj = objectMapper.readValue(jsonObject,
                    new TypeReference<List<Map<String, Object>>>() {
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return resObj;

    }


}
