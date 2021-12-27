package com.atomscat.bootstrap.modules.feishu.controller;

import com.atomscat.bootstrap.modules.feishu.service.DirectoryListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Howell
 * @date 2021/11/21 20:27
 */
@Slf4j
@RestController
@RequestMapping("/api/feishu")
public class DirectoryListController {
    @Autowired
    private DirectoryListService directoryListService;

    @RequestMapping(value = "/getDirectoryList", method = RequestMethod.GET)
    public String getDirectoryList() {
        try {
            directoryListService.getDirectoryList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "ok";
    }

    @RequestMapping(value = "/getDocumentDetail", method = RequestMethod.GET)
    public String getDocumentDetail() {
        try {
            directoryListService.getDocumentDetail();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "ok";
    }

    @RequestMapping(value = "/getOpenApiByJson", method = RequestMethod.GET)
    public String getOpenApiByJson() {
        try {
            directoryListService.getOpenApiByJson();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "ok";
    }

    @RequestMapping(value = "/getOpenApi", method = RequestMethod.GET)
    public String getOpenApi() {
        try {
            directoryListService.getOpenApi();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "ok";
    }



}
