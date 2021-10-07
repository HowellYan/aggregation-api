package com.atomscat.bootstrap.modules.dingding.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.atomscat.bootstrap.modules.dingding.dao.mapper.DocMetaMapper;
import com.atomscat.bootstrap.modules.dingding.entity.DocMeta;
import com.atomscat.bootstrap.modules.dingding.service.DocService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Howell
 * @date 2021/10/7 11:15
 */
@Slf4j
@Service
public class DocServiceImpl implements DocService {
    @Autowired
    private DocMetaMapper docMetaMapper;

    private static final Integer[] ids = {2453864, 2879797};

    @Override
    public String getDataHtml() {
        OpenAPI openAPI = new OpenAPI();
        List<Tag> tagList = new ArrayList<>();
        Paths paths = new Paths();

        QueryWrapper<DocMeta> queryWrapper = new QueryWrapper<>();
        List<DocMeta> list = docMetaMapper.selectList(queryWrapper);
        for (DocMeta docMeta : list) {
            if (ids.length <= 0) {
                getApi(docMeta, tagList, paths);
            } else {
                for (Integer id : ids) {
                    if (docMeta.getId() - id == 0) {
                        getApi(docMeta, tagList, paths);
                    }
                }

            }
        }
        openAPI.setPaths(paths);
        openAPI.setInfo(new Info().title("dingtalk").version("1.0.0").description(""));
        tagList = tagList.stream().distinct().collect(Collectors.toList());
        openAPI.setTags(tagList);
        log.info("OpenAPI json: {}", JSONObject.toJSONString(openAPI));
        return JSONObject.toJSONString(openAPI);
    }

    public void getApi(DocMeta docMeta, List<Tag> tagList, Paths paths) {
        try {
            Document doc = Jsoup.parse(docMeta.getHtml());
            if (doc != null) {
                setPaths(docMeta, tagList, paths, doc);
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }


    public void setPaths(DocMeta docMeta, List<Tag> tagList, Paths paths, Document doc) {
        // 请求接口地址
        String apiUrl = getApiUrl(doc);
        if (apiUrl == null) {
            return;
        }
        // 接口详情对象
        Operation operation = new Operation();


        PathItem path = new PathItem();
        // 请求接口方式
        PathItem.HttpMethod httpMethod = getHttpMethod(doc);
        if (httpMethod == null) {
            return;
        }
        path.operation(httpMethod, operation);
        paths.addPathItem(apiUrl, path);
    }

    /**
     * 接口请求地址
     */
    private String getApiUrl(Document doc) {
        String apiUrl = null;
        for (Element section : doc.getElementsByTag("section")) {
            if (section.text().contains("请求方式") && section.text().contains("请求地址")) {
                for (Element code : section.getElementsByTag("code")) {
                    apiUrl = code.text();
                }
            }
        }
        if (apiUrl != null) {
            for (Element section : doc.getElementsByTag("section")) {
                if (section.text().contains("请求方法")) {
                    for (Element code : section.getElementsByTag("code")) {

                        apiUrl = code.text();

                    }
                }
            }
        }
        return apiUrl;
    }

    /**
     * 请求接口方式
     */
    private PathItem.HttpMethod getHttpMethod(Document doc) {
        PathItem.HttpMethod method = null;
        for (Element element : doc.getElementsByTag("section")) {
            if (element.text().contains("PUT")) {
                method = PathItem.HttpMethod.PUT;
            } else if (element.text().contains("GET")) {
                method = PathItem.HttpMethod.GET;
            } else if (element.text().contains("HEAD")) {
                method = PathItem.HttpMethod.HEAD;
            } else if (element.text().contains("POST")) {
                method = PathItem.HttpMethod.POST;
            } else if (element.text().contains("DELETE")) {
                method = PathItem.HttpMethod.DELETE;
            } else if (element.text().contains("PATCH")) {
                method = PathItem.HttpMethod.PATCH;
            } else if (element.text().contains("OPTIONS")) {
                method = PathItem.HttpMethod.OPTIONS;
            } else if (element.text().contains("PUT")) {
                method = PathItem.HttpMethod.PUT;
            }
        }
        return method;
    }


}
