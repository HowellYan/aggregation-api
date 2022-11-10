package com.atomscat.bootstrap.modules.weixincp.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atomscat.bootstrap.modules.weixincp.dao.mapper.DocFetchMapper;
import com.atomscat.bootstrap.modules.weixincp.entity.DocFetch;
import com.atomscat.bootstrap.modules.weixincp.entity.DocId;
import com.atomscat.bootstrap.modules.weixincp.entity.TreeDocFetch;
import com.atomscat.bootstrap.modules.weixincp.entity.TreeRoot;
import com.atomscat.bootstrap.modules.weixincp.repository.TreeDocFetchRepository;
import com.atomscat.bootstrap.modules.weixincp.service.DocFetchService;
import com.atomscat.bootstrap.modules.weixincp.service.OpenAPIService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author th158
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocFetchServiceImpl implements DocFetchService {

    private final DocFetchMapper docFetchMapper;

    private final OpenAPIService openAPIService;

    private final TreeDocFetchRepository treeDocFetchRepository;

    private static String[] ids = {"34656"};

    @Async
    @Override
    public void getDocFetchByDocID() {
        if (ids.length <= 0) {
            ids = DocId.ids;
        }
        for (String id : ids) {
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

    @Override
    public String getOpenAPI() {
        QueryWrapper queryWrapper = new QueryWrapper();
        return openAPIService.build(docFetchMapper.selectList(queryWrapper));
    }

    @Override
    public void update(Long id, Integer result) {
        DocFetch docFetch = new DocFetch();
        docFetch.setId(id);
        docFetch.setResult(result);
        docFetchMapper.updateById(docFetch);
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

    @Async
    @Override
    public void getDocFetchJsonByDocID(Long docID) {
        String cookie = "RK=+1atPQyuFM; ptcz=f9b297500b047f2bcdcdac1ce4251a6d2f37466869147729a7954e44479574cb; pgv_pvid=1632910830918540; _ga=GA1.2.599786528.1632405590; o_cookie=1726474652; pac_uid=1_1726474652; ptui_loginuin=1726474652@qq.com; wwrtx.i18n_lan=zh; Hm_lvt_b9a29d420538f7c51b95531dd135a21b=1667960066,1667988180,1668062231; wwrtx.ref=direct; wwrtx.c_gdpr=0; wwrtx.refid=12843024562483366; Hm_lpvt_b9a29d420538f7c51b95531dd135a21b=1668087668; wwapidoc.sid=E389668E8AD7790DC3C6C2CF02C44B72B84C9DCE3E79362EE72174031DC92737BE93B54A735750176E0064A067026C6B4A3F03DF169AA28A9A2AC02DFF122507; wwapidoc.token_wt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbiI6IkNtd0tCZ2dDRUFFWUFoSWdQV1lvV3Q4QWp4eVhZem1McGJ0enh0WjFFMndIU2RselpxMm5NeStSNFdvYVFPZzdFanA3dTRYNVM2Y0tmWE5VNk41Q1BhSUVkaXVxdjJmb1Nacm5TZXM4L1hqeDZrcG5PempUNzZLYnFYS0sySU9YbDdrY3VKdnRNL2ZWV3lCajBwQWlJUW9mQ0pPS3Ewd1FsTjBER0lDQWdCQWlEM2QzYlc1bmMyVnpjMmx2Ym5OMmNnPT0iLCJpYXQiOjE2NjgwODc2ODQsImV4cCI6MTY2ODA4OTQ4NH0.uwUJ_U6-PV4L7GIJInZeKOEoBGQmwxnLwCRz0-NIyIE";
        String url = "https://developer.work.weixin.qq.com/docFetch/fetchCnt?id=" + docID + "&lang=zh_CN&ajax=1&f=json&random=" + RandomUtil.randomNumbers(5);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).addHeader("cookie", cookie).build();
        String res = "";
        try (Response response = client.newCall(request).execute()) {
            res = Objects.requireNonNull(response.body()).string();
            JSONObject jsonObject = JSON.parseObject(res);
            TreeDocFetch treeDocFetch = JSONObject.parseObject(jsonObject.getString("data"), TreeDocFetch.class);
            treeDocFetchRepository.saveAndFlush(treeDocFetch);
        } catch (Exception e) {
            log.error("{}", url);
        }
    }


}
