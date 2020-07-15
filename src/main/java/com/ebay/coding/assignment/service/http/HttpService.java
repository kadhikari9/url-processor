package com.ebay.coding.assignment.service.http;

import java.util.Map;

/**
 * @author kadhikari
 * Utility Class to perform Http Call
 */
public interface HttpService {

    /**
     * Perform Http
     * @param url Url of endpoint
     * @param params Http query Params
     * @return response of http call
     */
    String doGet(String url, Map<String, String> params);
}
