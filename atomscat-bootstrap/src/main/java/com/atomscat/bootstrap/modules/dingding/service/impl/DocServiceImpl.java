package com.atomscat.bootstrap.modules.dingding.service.impl;

import com.atomscat.bootstrap.modules.dingding.dao.mapper.DocMetaMapper;
import com.atomscat.bootstrap.modules.dingding.entity.DocMeta;
import com.atomscat.bootstrap.modules.dingding.service.DocService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Howell
 * @date 2021/10/7 11:15
 */
@Service
public class DocServiceImpl implements DocService {
    @Autowired
    private DocMetaMapper docMetaMapper;

    private static final String[] ids = {"2879797"};

    @Override
    public void getDataHtml() {
        QueryWrapper<DocMeta> queryWrapper = new QueryWrapper<>();
        List<DocMeta> list = docMetaMapper.selectList(queryWrapper);
        for (DocMeta docMeta : list) {
            if (ids.length <= 0) {
                getApi(docMeta.getHtml());
            } else {
                if (Arrays.stream(ids).anyMatch(item->docMeta.getId().equals(item))) {
                    getApi(docMeta.getHtml());
                }
            }
        }
    }

    public void getApi(String html) {
        Document doc = Jsoup.parse(html);

    }





}
