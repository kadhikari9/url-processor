package com.ebay.coding.assignment.service.http;

import com.ebay.coding.assignment.util.PropertyUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public enum SimpleHttpService implements HttpService {
    INSTANCE;

    private static final Logger logger = LoggerFactory.getLogger(SimpleHttpService.class);
    private CloseableHttpClient httpClient;

    SimpleHttpService() {
        init();
    }

    private void init() {
        String connectTimeout = PropertyUtil.INSTANCE.getProperty("http.connect.timeout", "1000");
        String maxConnecions = PropertyUtil.INSTANCE.getProperty("http.max.connections", "100");

        RequestConfig config = RequestConfig.custom().setConnectTimeout(Integer.parseInt(connectTimeout)).build();
        PoolingHttpClientConnectionManager connManager = new
                PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(Integer.parseInt(maxConnecions));

        httpClient = HttpClientBuilder.create().setConnectionManager(connManager).setDefaultRequestConfig(config).build();
    }

    @Override
    public String doGet(String url, Map<String, String> params) {
        try (CloseableHttpClient closeableHttpClient = httpClient) {
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = closeableHttpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 200) {
                InputStream io = response.getEntity().getContent();

                BufferedReader reader = new BufferedReader(new InputStreamReader(io));
                StringBuilder respStr = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    respStr.append(line);
                }
                return respStr.toString();
            }

        } catch (Exception ex) {
            logger.error("Error calling http url:{}", url);
        }

        return null;
    }
}
