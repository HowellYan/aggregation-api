package com.atomscat.bootstrap.modules.weixincp.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.atomscat.bootstrap.modules.weixincp.dao.mapper.DocExtraMapper;
import com.atomscat.bootstrap.modules.weixincp.entity.DocExtra;
import com.atomscat.bootstrap.modules.weixincp.entity.DocFetch;
import com.atomscat.bootstrap.modules.weixincp.entity.DocId;
import com.atomscat.bootstrap.modules.weixincp.service.DocExtraService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
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
public class DocExtraServiceImpl implements DocExtraService {
    @Autowired
    private DocExtraMapper docExtraMapper;

    @Async
    @Override
    public void getDocExtraByDocID() {
        for (String id : DocId.ids) {
            String url = "https://open.work.weixin.qq.com/api/docExtra/getExtraInfo?lang=zh_CN&ajax=1&f=json&doc_id="+ id +"&random=" + RandomUtil.randomNumbers(5);
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
            QueryWrapper<DocExtra> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(DocExtra::getDocId, id);
            if (docExtraMapper.selectCount(queryWrapper) > 0) {
                return;
            }
            DocExtra docExtra = new DocExtra();
            docExtra.setDoc(res);
            docExtra.setDocId(Long.parseLong(id));
            docExtra.setCreateTime(new Date());
            docExtraMapper.insert(docExtra);
        } catch (Exception e) {
            log.error("{}", e);
            e.printStackTrace();
        }
    }
}
