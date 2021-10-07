package com.atomscat.bootstrap.modules.dingding.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.atomscat.bootstrap.modules.dingding.dao.mapper.DocMetaMapper;
import com.atomscat.bootstrap.modules.dingding.entity.DocMeta;
import com.atomscat.bootstrap.modules.dingding.service.DocMetaService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Howell
 * @date 2021/10/4 12:47
 */
@Slf4j
@Service
public class DocMetaServiceImpl implements DocMetaService {
    @Autowired
    private DocMetaMapper docMetaMapper;

    @Override
    public void getAppMeta() {
        String url = "https://icms-document.oss-cn-beijing.aliyuncs.com/zh-CN/dingtalk/app/meta.json";
        getResp(url, "app");
    }

    @Override
    public void getChatgroupMeta() {
        String url = "https://icms-document.oss-cn-beijing.aliyuncs.com/zh-CN/dingtalk/chatgroup/meta.json";
        getResp(url, "chatgroup");
    }

    @Override
    public void getRobotsMeta() {
        String url = "https://icms-document.oss-cn-beijing.aliyuncs.com/zh-CN/dingtalk/robots/meta.json";
        getResp(url, "robots");
    }

    @Override
    public void getConnectorMeta() {
        String url = "https://icms-document.oss-cn-beijing.aliyuncs.com/zh-CN/dingtalk/connector/meta.json";
        getResp(url, "connector");
    }

    @Override
    public void getDashboardMeta() {
        String url = "https://icms-document.oss-cn-beijing.aliyuncs.com/zh-CN/dingtalk/dashboard/meta.json";
        getResp(url, "dashboard");
    }

    @Override
    public void getMobileAppGuideMeta() {
        String url = "https://icms-document.oss-cn-beijing.aliyuncs.com/zh-CN/dingtalk/mobile-app-guide/meta.json";
        getResp(url, "mobile-app-guide");
    }

    @Override
    public void getHardwareAccessMeta() {
        String url = "https://icms-document.oss-cn-beijing.aliyuncs.com/zh-CN/dingtalk/hardware-access/meta.json";
        getResp(url, "hardware-access");
    }

    /**
     * 获取json
     *
     * @param url
     * @param docType
     */
    private void getResp(String url, String docType) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            String res = "";
            try (Response response = client.newCall(request).execute()) {
                res = response.body().string();
            }
            JSONObject jsonObject = JSONObject.parseObject(res);
            log.info("{}", jsonObject.size());
            getItem(jsonObject, docType);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    private void getItem(JSONObject jsonObject, String docType) {
        if (jsonObject != null) {
            if (jsonObject.getJSONArray("topics") != null) {
                JSONArray jsonArray = jsonObject.getJSONArray("topics");
                for (Object obj : jsonArray) {
                    getItem(JSON.parseObject(obj.toString()), docType);
                }
            } else if (jsonObject.getJSONArray("children") != null) {
                JSONArray jsonArray = jsonObject.getJSONArray("children");
                for (Object obj : jsonArray) {
                    getItem(JSON.parseObject(obj.toString()), docType);
                }
            }
            save(jsonObject, docType);
        }
    }

    /**
     * 保存到数据库
     * @param jsonObject
     * @param docType
     */
    public void save(JSONObject jsonObject, String docType) {
        try {
            DocMeta docMeta = jsonObject.toJavaObject(DocMeta.class);
            docMeta.setDocType(docType);
            QueryWrapper<DocMeta> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(DocMeta::getId, docMeta.getId());
            if (docMetaMapper.selectCount(queryWrapper) > 0) {
                docMetaMapper.updateById(docMeta);
            } else {
                docMetaMapper.insert(docMeta);
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * 循环获取接口
     */
    @Override
    public void getHtml() {
        QueryWrapper<DocMeta> queryWrapper = new QueryWrapper<>();
        List<DocMeta> list = docMetaMapper.selectList(queryWrapper);
        for (DocMeta docMeta : list) {
            getHtmlResp(docMeta);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                log.error("{}", e);
            }
        }
    }

    /**
     * 获取运程的html
     * @param docMeta
     */
    private void getHtmlResp(DocMeta docMeta) {
        try {
            String url = "https://icms-document.oss-cn-beijing.aliyuncs.com" + "" +
                    "/zh-CN/dingtalk/" + docMeta.getDocType() + "/topics/" + docMeta.getSlug() + ".html";
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            String res = "";
            try (Response response = client.newCall(request).execute()) {
                res = response.body().string();
            }
            docMeta.setHtml(res);
            docMetaMapper.updateById(docMeta);
        } catch (Exception e) {
            log.error("", e);
        }
    }
}
