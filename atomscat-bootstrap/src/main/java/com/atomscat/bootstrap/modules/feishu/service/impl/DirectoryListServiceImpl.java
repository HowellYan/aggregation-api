package com.atomscat.bootstrap.modules.feishu.service.impl;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.atomscat.bootstrap.modules.feishu.dao.mapper.DirectoryListMapper;
import com.atomscat.bootstrap.modules.feishu.entity.DirectoryList;
import com.atomscat.bootstrap.modules.feishu.service.DirectoryListService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Howell
 * @date 2021/11/21 20:28
 */
@Slf4j
@Service
public class DirectoryListServiceImpl implements DirectoryListService {

    private final String url = "https://open.feishu.cn/api/tools/docment/directory_list";

    private final String detailUrl = "https://open.feishu.cn/api/tools/document/detail?fullPath=";

    @Autowired
    private DirectoryListMapper directoryListMapper;

    /**
     * 獲取全部接口文檔列表
     * @throws Exception
     */
    @Override
    public void getDirectoryList() throws Exception {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        String res = "";
        try (Response response = client.newCall(request).execute()) {
            res = response.body().string();
            // 保存
            JSONObject json = JSONObject.parseObject(res);
            explainRes(json.getJSONObject("data"));
        }
    }

    private void explainRes(JSONObject json) {
        JSONArray jsonArray = json.getJSONArray("items");
        jsonArray.forEach(o -> {
            JSONObject itemJson = JSONObject.parseObject(o.toString());
            saveOrUpdate(itemJson);
            explainRes(itemJson);
        });
    }

    private void saveOrUpdate(JSONObject itemJson) {
        DirectoryList directoryList = JSONObject.parseObject(itemJson.toJSONString(), DirectoryList.class);
        if (directoryListMapper.selectById(directoryList.getId()) != null) {
            directoryListMapper.updateById(directoryList);
        } else {
            directoryListMapper.insert(directoryList);
        }
    }

    /**
     * 獲取接口詳情
     */
    @Override
    public void getDocumentDetail() {
        QueryWrapper<DirectoryList> queryWrapper = new QueryWrapper<>();
        List<DirectoryList> list = directoryListMapper.selectList(queryWrapper);
        for (DirectoryList directoryList : list) {
            try {
                getDocumentDetail(directoryList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void getDocumentDetail(DirectoryList directoryList) throws Exception {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(detailUrl + directoryList.getFullPath()).build();
        String res = "";
        try (Response response = client.newCall(request).execute()) {
            res = response.body().string();
            directoryList.setDocumentDetail(res);
            directoryListMapper.updateById(directoryList);
        }
    }

    @Override
    public void getOpenApiByJson() {
        try {
            File jsonFile = ResourceUtils.getFile("classpath:feishu-openapi.json");
            String json = FileUtil.readUtf8String(jsonFile);
            OpenAPI openAPI = JSONObject.parseObject(json, OpenAPI.class);
            log.info("{}", openAPI.getPaths().size());
            for (String entry : openAPI.getPaths().keySet()) {
                JSONObject.parseObject(openAPI.getPaths().get(entry).toString());
            }
        } catch (Exception e) {
            log.error("tag: {}", e);
        }
    }

    private void getContent(String path, PathItem pathItem) {
        String name = pathItem.getSummary();
        QueryWrapper<DirectoryList> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(DirectoryList::getName, "%" + name + "%")
                        .like(DirectoryList::getDocumentDetail, "%" + path.substring(0, path.length() - 1) + "");
        List<DirectoryList> directoryListList = directoryListMapper.selectList(queryWrapper);
        if (directoryListList.size() == 1) {
            DirectoryList directoryList = directoryListList.get(0);

            log.info("{}", directoryList.getDocumentDetail());
        }
    }

}
