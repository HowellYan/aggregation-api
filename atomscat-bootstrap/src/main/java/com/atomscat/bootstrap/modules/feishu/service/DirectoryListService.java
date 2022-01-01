package com.atomscat.bootstrap.modules.feishu.service;

/**
 * @author Howell
 * @date 2021/11/21 20:28
 */
public interface DirectoryListService {
    /**
     * 獲取全部接口文檔列表
     * @throws Exception
     */
    void getDirectoryList() throws Exception;

    /**
     * 獲取接口詳情
     */
    void getDocumentDetail();

    /**
     * 從json獲取OpenApi 對象
     */
    void getOpenApiByJson();

    /**
     *
     */
    void getOpenApi(String[] ids);
}
