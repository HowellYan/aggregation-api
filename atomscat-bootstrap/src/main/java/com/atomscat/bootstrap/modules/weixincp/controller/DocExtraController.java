package com.atomscat.bootstrap.modules.weixincp.controller;

import com.atomscat.bootstrap.modules.weixincp.service.DocExtraService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/weixin/cp")
public class DocExtraController {
    @Autowired
    private DocExtraService docExtraService;

    @RequestMapping(value = "/getDocExtra", method = RequestMethod.GET)
    public String getDocExtra() {
        docExtraService.getDocExtraByDocID();
        return "ok";
    }
}
