package com.atomscat.bootstrap.modules.dingding.controller;

import com.atomscat.bootstrap.modules.dingding.service.DocMetaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Howell
 * @date 2021/10/3 16:33
 */
@Slf4j
@RestController
@RequestMapping("/api/dingding")
public class AppMetaController {

    @Autowired
    private DocMetaService docMetaService;

    @RequestMapping(value = "/getAppMeta", method = RequestMethod.GET)
    public String getAppMeta() {
        docMetaService.getAppMeta();
        docMetaService.getChatgroupMeta();
        docMetaService.getHardwareAccessMeta();
        docMetaService.getConnectorMeta();
        docMetaService.getRobotsMeta();
        docMetaService.getDashboardMeta();
        docMetaService.getMobileAppGuideMeta();
        return "ok";
    }

    @RequestMapping(value = "/getHtml", method = RequestMethod.GET)
    public String getHtml() {
        docMetaService.getHtml();
        return "ok";
    }


}
