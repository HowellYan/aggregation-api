package com.atomscat.bootstrap.modules.weixincp.service;

import org.springframework.scheduling.annotation.Async;

public interface DocFetchService {
    @Async
    void getDocFetchByDocID();
}
