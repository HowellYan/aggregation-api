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
import com.atomscat.bootstrap.modules.weixincp.service.DocFetchService;
import com.atomscat.bootstrap.modules.weixincp.service.TreeRootService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

import static cn.hutool.core.thread.ThreadUtil.sleep;

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

    private final DocFetchService docFetchService;

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
        if (StrUtil.isNotBlank(treeOrigin.getPathJson())) {
            try {
                JSONObject jsonObject = JSON.parseObject(treeOrigin.getPathJson());
                treeOrigin.setHref(jsonObject.getString("href"));
            } catch (Exception e) {
                log.error("", e);
            }
        }
        treeOriginRepository.saveAndFlush(treeOrigin);
        // to next
        Map<String, TreeOrigin> treeOriginMap = treeOrigin.getChildren();
        if (treeOriginMap != null && treeOriginMap.size() > 0) {
            for (Map.Entry<String, TreeOrigin> treeOriginEntry : treeOriginMap.entrySet()) {
                TreeOrigin children = treeOriginEntry.getValue();
                children.setTreeOriginParent(treeOrigin);
                getNextNode(children);
            }
        }
    }

    @Async
    @Override
    public void getDocFetch(PageRequest pageable) {
        Page<TreeOrigin> list = treeOriginRepository.findAll(pageable);
        for (TreeOrigin treeOrigin : list.getContent()) {
            if (treeOrigin.getType() == 1) {
                docFetchService.getDocFetchJsonByDocID(treeOrigin.getDocId());
            }
        }
        if (list.hasNext()) {
            sleep(5000);
            getDocFetch(pageable.next());
        }
    }
}
