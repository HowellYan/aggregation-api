package com.atomscat.bootstrap.modules.weixincp.controller;

import com.atomscat.bootstrap.modules.weixincp.service.DocFetchService;
import com.atomscat.bootstrap.modules.weixincp.service.TreeRootService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/weixin/cp")
@RequiredArgsConstructor
public class DocFetchController {

    private final DocFetchService docFetchService;

    private final TreeRootService treeRootService;

    @RequestMapping(value = "/getDocFetch", method = RequestMethod.GET)
    public String getDocFetch() {
//        docFetchService.getDocFetchByDocID();
        treeRootService.getDocFetch(PageRequest.of(1, 1));
        return "ok";
    }

    @RequestMapping(value = "/getDocFetchById", method = RequestMethod.GET)
    public String getDocFetch(@Param("docId") Long docId) {
        treeRootService.getDocFetch(docId);
        return "ok";
    }


    @RequestMapping(value = "/getOpenAPI", method = RequestMethod.GET)
    public String getOpenAPI() {
        return docFetchService.getOpenAPI();
    }

}
//https://developer.work.weixin.qq.com/docFetch/fetchCnt?id=15371&lang=zh_CN&ajax=1&f=json&random=42234
//https://developer.work.weixin.qq.com/docFetch/fetchCnt?id=15951&lang=zh_CN&ajax=1&f=json&random=00963
//https://developer.work.weixin.qq.com/docFetch/fetchCnt?id=25575&lang=zh_CN&ajax=1&f=json&random=11907
//https://developer.work.weixin.qq.com/docFetch/fetchCnt?id=40720&lang=zh_CN&ajax=1&f=json&random=71470
//https://developer.work.weixin.qq.com/docFetch/fetchCnt?id=40722&lang=zh_CN&ajax=1&f=json&random=94499
//https://developer.work.weixin.qq.com/docFetch/fetchCnt?id=21268&lang=zh_CN&ajax=1&f=json&random=81252
//https://developer.work.weixin.qq.com/docFetch/fetchCnt?id=23834&lang=zh_CN&ajax=1&f=json&random=29373




