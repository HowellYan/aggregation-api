package com.atomscat.bootstrap.modules.weixincp.service;

import com.atomscat.bootstrap.modules.weixincp.entity.DocFetch;

import java.util.List;

public interface OpenAPIService {
    String build(List<DocFetch> docFetchList);
}
