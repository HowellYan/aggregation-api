package com.atomscat.bootstrap.modules.weixincp.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("doc_index")
public class DocIndex {
    private Long id;
    @JSONField(name = "category_id")
    private Long categoryId;
    @JSONField(name = "doc_id")
    private Long docId;
    @JSONField(name = "parent_id")
    private Long parentId;
    private Long time;
    private String author;
    private Long type;
    private Long status;
    private String title;
    @JSONField(name = "order_id")
    private Long orderId;
    @JSONField(name = "gray_status")
    private Long grayStatus;
}


