package com.atomscat.bootstrap.modules.weixincp.service;

import org.springframework.scheduling.annotation.Async;

public interface DocFetchService {
    @Async
    void getDocFetchByDocID();

    String getOpenAPI();

    void update(Long id, Integer result);

    void getDocFetchJsonByDocID(Long docID);
}
