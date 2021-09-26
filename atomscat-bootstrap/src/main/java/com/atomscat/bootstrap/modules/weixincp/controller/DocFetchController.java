package com.atomscat.bootstrap.modules.weixincp.controller;

import com.atomscat.bootstrap.modules.weixincp.service.DocFetchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/weixin/cp")
public class DocFetchController {

    @Autowired
    private DocFetchService docFetchService;

    @RequestMapping(value = "/getDocFetch", method = RequestMethod.GET)
    public String getDocFetch() {
        docFetchService.getDocFetchByDocID();
        return "ok";
    }

    @RequestMapping(value = "/getOpenAPI", method = RequestMethod.GET)
    public String getOpenAPI() {
        return docFetchService.getOpenAPI();
    }

}
