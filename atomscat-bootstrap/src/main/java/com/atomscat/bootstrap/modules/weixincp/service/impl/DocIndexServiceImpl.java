package com.atomscat.bootstrap.modules.weixincp.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.atomscat.bootstrap.modules.weixincp.dao.mapper.DocIndexMapper;
import com.atomscat.bootstrap.modules.weixincp.entity.DocIndex;
import com.atomscat.bootstrap.modules.weixincp.service.DocIndexService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.util.Map;

/**
 * @author th158
 */
@Slf4j
@Service
public class DocIndexServiceImpl implements DocIndexService {

    @Autowired
    private DocIndexMapper docIndexMapper;

    @Override
    public void getDocIndexByJson() {
        try {
            File jsonFile = ResourceUtils.getFile("classpath:catagories.json");
            String json = FileUtil.readUtf8String(jsonFile);
            JSONObject jsonObject = JSON.parseObject(json);
            getNextNode(jsonObject);
        } catch (Exception e) {
            log.error("tag: {}", e);
        }
    }

    public void getNextNode(JSONObject jsonObject) {
        for (Map.Entry<String, Object> stringSet : jsonObject.entrySet()) {
            if (stringSet == null || StrUtil.isBlank(stringSet.getKey()) || stringSet.getValue() == null || StrUtil.isBlank(stringSet.getValue().toString())) {
                continue;
            }
            save(jsonObject);
            if (stringSet.getValue() instanceof Iterable) {
                JSONArray jsonArray = JSON.parseArray(stringSet.getValue().toString());
                if ("children".equals(stringSet.getKey())) {
                    for (Object obj : jsonArray) {
                        getNextNode(JSON.parseObject(obj.toString()));
                    }
                }
            } else if (!(stringSet.getValue() instanceof String) &&
                    !(stringSet.getValue() instanceof Number) &&
                    !(stringSet.getValue() instanceof Boolean)
            ) {
                JSONObject jsonNextObject = JSON.parseObject(stringSet.getValue().toString());
                getNextNode(jsonNextObject);
            }
        }
    }

    public void save(JSONObject jsonObject) {
        try {
            DocIndex docIndex = JSONObject.parseObject(jsonObject.toString(), DocIndex.class);

            if (docIndex != null &&
                    docIndex.getId() != null &&
                    docIndex.getDocId() != null &&
                    docIndex.getCategoryId() != null &&
                    docIndex.getParentId() != null
            ) {
                QueryWrapper<DocIndex> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(DocIndex::getId, docIndex.getId())
                        .eq(DocIndex::getDocId, docIndex.getDocId())
                        .eq(DocIndex::getParentId, docIndex.getParentId())
                        .eq(DocIndex::getCategoryId, docIndex.getCategoryId());
                if (docIndexMapper.selectCount(queryWrapper) > 0) {
                    docIndexMapper.update(docIndex, queryWrapper);
                } else {
                    docIndexMapper.insert(docIndex);
                }
            }
        } catch (Exception e) {
            log.error("{}", e);
        }
    }

}
