package com.atomscat.bootstrap.modules.weixincp.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.atomscat.bootstrap.modules.weixincp.entity.DocIndex;
import com.atomscat.bootstrap.modules.weixincp.entity.TreeBannerCardConf;
import com.atomscat.bootstrap.modules.weixincp.entity.TreeOrigin;
import com.atomscat.bootstrap.modules.weixincp.entity.TreeRoot;
import com.atomscat.bootstrap.modules.weixincp.repository.TreeBannerCardConfRepository;
import com.atomscat.bootstrap.modules.weixincp.repository.TreeOriginRepository;
import com.atomscat.bootstrap.modules.weixincp.repository.TreeRootRepository;
import com.atomscat.bootstrap.modules.weixincp.service.TreeRootService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author th158
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TreeRootServiceImpl implements TreeRootService {

    private final TreeRootRepository treeRootRepository;

    private final TreeOriginRepository treeOriginRepository;

    private final TreeBannerCardConfRepository treeBannerCardConfRepository;

    @Override
    public void getDocIndexByJson() {
        try {
            File jsonFile = ResourceUtils.getFile("classpath:catagories.json");
            String json = FileUtil.readUtf8String(jsonFile);
            JSONObject jsonObject = JSON.parseObject(json);
            TreeRoot treeRoot = JSONObject.parseObject(jsonObject.toString(), TreeRoot.class);
            treeRoot.setId(1L);
            TreeOrigin treeOrigin = treeRoot.getTreeOrigin();
            List<TreeBannerCardConf> treeBannerCardConfList = treeRoot.getBannerCardConf();
            treeRootRepository.saveAndFlush(treeRoot);
            saveTreeBannerCardConf(treeBannerCardConfList, treeRoot);
            treeOrigin.setId(0L);
            getNextNode(treeOrigin);
        } catch (Exception e) {
            log.error("tag: {}", e);
        }
    }

    private void saveTreeBannerCardConf(List<TreeBannerCardConf> treeBannerCardConfList, TreeRoot treeRoot) {
        try {
            for (TreeBannerCardConf treeBannerCardConf : treeBannerCardConfList) {
                treeBannerCardConf.setTreeRoot(treeRoot);
                treeBannerCardConfRepository.saveAndFlush(treeBannerCardConf);
            }
        } catch (Exception e) {
            log.error("save", e);
        }
    }

    private void getNextNode(TreeOrigin treeOrigin) {
        treeOriginRepository.saveAndFlush(treeOrigin);
        Map<String, TreeOrigin> treeOriginMap = treeOrigin.getChildren();
        if (treeOriginMap != null && treeOriginMap.size() > 0) {
            for (Map.Entry<String, TreeOrigin> treeOriginEntry : treeOriginMap.entrySet()) {
                TreeOrigin children = treeOriginEntry.getValue();
                children.setTreeOriginParent(treeOrigin);
                getNextNode(children);
            }
        }
    }
}
