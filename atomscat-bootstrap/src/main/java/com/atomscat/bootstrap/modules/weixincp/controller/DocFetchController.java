package com.atomscat.bootstrap.modules.weixincp.controller;

import com.atomscat.bootstrap.modules.weixincp.service.DocFetchService;
import com.atomscat.bootstrap.modules.weixincp.service.TreeRootService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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

    @RequestMapping(value = "/getOpenAPI", method = RequestMethod.GET)
    public String getOpenAPI() {
        return docFetchService.getOpenAPI();
    }

}
