package com.ebay.coding.assignment.file;

import com.ebay.coding.assignment.service.http.SimpleHttpService;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class HttpServiceTest {

    @Test
    public void testHttp() {
        String resp = SimpleHttpService.INSTANCE.doGet("http://127.0.0.1:8080/e?a=582009481", Collections.emptyMap());
        Assert.assertNotNull(resp);
        Assert.assertTrue(resp.isEmpty());
    }
}
