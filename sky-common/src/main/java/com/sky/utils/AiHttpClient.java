package com.sky.utils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.util.Map;
/**
 ** AI 专用 HTTP 客户端（轻量），用于发送 JSON body 并可设置 headers
 * * 与现有 HttpClientUtil
 * */
public class AiHttpClient {
    private static final int TIMEOUT_MSEC = 15_000;
    public static String postJsonWithHeaders(String url, String jsonBody, Map<String, String> headers) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost(url);
            RequestConfig config = RequestConfig.custom() .setConnectTimeout(TIMEOUT_MSEC) .setConnectionRequestTimeout(TIMEOUT_MSEC) .setSocketTimeout(TIMEOUT_MSEC) .build();
            httpPost.setConfig(config); StringEntity entity = new StringEntity(jsonBody, "UTF-8");
            entity.setContentType("application/json"); httpPost.setEntity(entity);
            if (headers != null) {
                for (Map.Entry<String, String> e : headers.entrySet()) {
                    httpPost.setHeader(e.getKey(), e.getValue()); }
            }
            response = httpClient.execute(httpPost);
            int status = response.getStatusLine().getStatusCode();
            String resp = EntityUtils.toString(response.getEntity(), "UTF-8");
            if (status >= 200 && status < 300) {
                return resp;
            } else {
                throw new IOException("AiHttpClient.postJsonWithHeaders error status=" + status + " body=" + resp);
            }
        } finally {
            if (response != null) try {
                response.close();
            }
            catch (IOException ignored) {

            } try {
                httpClient.close();
            } catch (IOException ignored) {

            }
        }
    }
}