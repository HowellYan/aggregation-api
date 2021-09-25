package com.atomscat.bootstrap.modules.weixincp.service.impl;

import com.atomscat.bootstrap.modules.weixincp.dao.mapper.DocFetchMapper;
import com.atomscat.bootstrap.modules.weixincp.entity.DocFetch;
import com.atomscat.bootstrap.modules.weixincp.entity.DocId;
import com.atomscat.bootstrap.modules.weixincp.service.DocFetchService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class DocFetchServiceImpl implements DocFetchService {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Autowired
    private DocFetchMapper docFetchMapper;

    @Async
    @Override
    public void getDocFetchByDocID() {
        for (String id : DocId.ids) {
            String url = "https://work.weixin.qq.com/api/docFetch/fetchCnt?id=" + id;
            try {
                getJson(url, id);
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (Exception e) {
                log.error("{}", e);
                e.printStackTrace();
            }
        }
    }

    public void getJson(String url, String id) throws Exception {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        String res = "";
        try (Response response = client.newCall(request).execute()) {
            res = response.body().string();
            // 保存
            save(res, id);
        }
    }

    public void save(String res, String id) {
        try {
            QueryWrapper<DocFetch> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(DocFetch::getDocId, id);
            if (docFetchMapper.selectCount(queryWrapper) > 0) {
                return;
            }
            DocFetch docFetch = new DocFetch();
            docFetch.setDoc(res);
            docFetch.setDocId(id);
            docFetch.setCreateTime(new Date());
            docFetchMapper.insert(docFetch);
        } catch (Exception e) {
            log.error("{}", e);
            e.printStackTrace();
        }
    }

}
