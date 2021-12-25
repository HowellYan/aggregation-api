package com.atomscat.bootstrap.modules.keruyun.controller;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/keruyun")
public class KeRuYunController {

    @RequestMapping(value = "/getOne", method = RequestMethod.GET)
    public String getOne() {
        String url = "https://open.keruyun.com/docs/zh/v8eXEXQBzPVmqdQu11w7.html";
        Document document = null;
        try {
            document = Jsoup.connect(url).timeout(60000).get();
            Elements elements = document.getElementsByTag("code");
            for (Element element : elements) {
                log.info(element.text());
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return "ok";
    }
}
