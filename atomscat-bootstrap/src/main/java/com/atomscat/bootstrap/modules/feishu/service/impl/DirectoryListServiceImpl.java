package com.atomscat.bootstrap.modules.feishu.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
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
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

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
     *
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
            // log.info("{}", openAPI.getPaths().size());
            for (String entry : openAPI.getPaths().keySet()) {
                LinkedHashMap paths = JSONObject.parseObject(JSONObject.toJSONString(openAPI.getPaths()), LinkedHashMap.class);
                PathItem pathItem = JSONObject.parseObject(paths.get(entry).toString(), PathItem.class);
                // log.info("{}, {}", entry, pathItem.toString());
                DirectoryList directoryList = getDirectory(entry, pathItem);
                // log.info("{}", JSONObject.toJSONString(directoryList));
            }
        } catch (Exception e) {
            log.error("tag: {}", e);
        }
    }

    @Override
    public void getOpenApi(String[] ids) {
        QueryWrapper queryWrapper = new QueryWrapper();
        List<DirectoryList> directoryListList = directoryListMapper.selectList(queryWrapper);
        for (DirectoryList directoryList : directoryListList) {
            if (ids.length == 0 || Arrays.stream(ids).anyMatch(s -> s.equals(directoryList.getId()))) {
                String docStr = directoryList.getDocumentDetail();
                JSONObject jsonObject = JSONObject.parseObject(docStr);
                if (jsonObject != null && jsonObject.get("code") != null && 0 == jsonObject.getInteger("code")) {
                    String content = jsonObject.getJSONObject("data").getJSONObject("document").getString("content");
                    PegDownProcessor md = new PegDownProcessor(Extensions.ALL_WITH_OPTIONALS);
                    log.info(md.markdownToHtml(content));
                } else {
                    log.error("{}", JSONObject.toJSONString(directoryList));
                }
            }
        }
    }


    private DirectoryList getDirectory(String entry, PathItem pathItem) {
        QueryWrapper<DirectoryList> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().like(DirectoryList::getDocumentDetail, entry);
        List<DirectoryList> directoryListList = directoryListMapper.selectList(queryWrapper);
        if (directoryListList.size() == 1) {
            return directoryListList.get(0);
        } else {
            String[] s = entry.split("/");
            if (pathItem.getGet() != null) {
                queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().like(DirectoryList::getDocumentDetail, pathItem.getGet().getSummary());
            } else if (pathItem.getPost() != null) {
                queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().like(DirectoryList::getDocumentDetail, pathItem.getPost().getSummary());
            } else if (pathItem.getDelete() != null) {
                queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().like(DirectoryList::getDocumentDetail, pathItem.getDelete().getSummary());
            } else if (pathItem.getPut() != null) {
                queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().like(DirectoryList::getDocumentDetail, pathItem.getPut().getSummary());
            } else if (pathItem.getPatch() != null) {
                queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().like(DirectoryList::getDocumentDetail, pathItem.getPatch().getSummary());
            }
            for (String item : s) {
                if (!item.contains("{") && !item.contains("}") && !StrUtil.isBlank(item)) {
                    queryWrapper.lambda().like(DirectoryList::getDocumentDetail, item);
                }
            }
            directoryListList = directoryListMapper.selectList(queryWrapper);
            if (directoryListList.size() == 1) {
                return directoryListList.get(0);
            }
            log.info(entry);
        }
        DirectoryList directory = new DirectoryList();
        return directory;
    }


    private void getContent(String path, PathItem pathItem) {
        String name = pathItem.getSummary();
        QueryWrapper<DirectoryList> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().like(DirectoryList::getName, "%" + name + "%").like(DirectoryList::getDocumentDetail, "%" + path.substring(0, path.length() - 1) + "");
        List<DirectoryList> directoryListList = directoryListMapper.selectList(queryWrapper);
        if (directoryListList.size() == 1) {
            DirectoryList directoryList = directoryListList.get(0);
            log.info("数据库: {}", directoryList.getDocumentDetail());
        }
    }

}
