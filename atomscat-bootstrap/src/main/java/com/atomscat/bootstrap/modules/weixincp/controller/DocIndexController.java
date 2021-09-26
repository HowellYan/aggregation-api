package com.atomscat.bootstrap.modules.weixincp.controller;

import com.atomscat.bootstrap.modules.weixincp.service.DocIndexService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/weixin/cp")
public class DocIndexController {
    @Autowired
    private DocIndexService docIndexService;

    @RequestMapping(value = "/getDocIndexByJson", method = RequestMethod.GET)
    public String getDocIndexByJson() {
        docIndexService.getDocIndexByJson();
        return "ok";
    }
}
