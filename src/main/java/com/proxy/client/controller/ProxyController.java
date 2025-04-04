package com.proxy.client.controller;

import com.proxy.client.service.ProxyClient;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/")
class ProxyController {
    private final ProxyClient proxyClient;

    public ProxyController(ProxyClient proxyClient) {
        this.proxyClient = proxyClient;
    }

    @GetMapping("/**")
    public String handleGetRequest(@RequestParam String url) throws IOException {
        return proxyClient.forwardRequest("GET", url, "");
    }

}
