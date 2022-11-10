package com.atomscat.bootstrap.modules.weixincp.service;

import org.springframework.data.domain.PageRequest;

/**
 * @author th158
 */
public interface TreeRootService {
    void getDocIndexByJson();

    void getDocFetch(PageRequest pageable);
}
