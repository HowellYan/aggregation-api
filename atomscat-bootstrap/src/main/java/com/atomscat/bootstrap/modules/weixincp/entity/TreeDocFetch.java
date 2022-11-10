package com.atomscat.bootstrap.modules.weixincp.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "t_tree_doc_fetch")
public class TreeDocFetch {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    private String title;

    private Long time;

    private Integer type;

    @JSONField(name = "parent_id")
    private Long parentId;

    @Lob
    @JSONField(name = "content_html")
    private String contentHtml;

    @JSONField(name = "is_deleted")
    private Boolean isDeleted;

    @Lob
    @JSONField(name = "content_md")
    private String contentMd;

    private Integer status;

    @JSONField(name = "doc_id")
    private Long docId;

    @JSONField(name = "order_id")
    private Long orderId;

    @Lob
    @JSONField(name = "content_txt")
    private String contentTxt;

    @JSONField(name = "gray_status")
    public Integer grayStatus;

    @Lob
    @JSONField(name = "content_html_v2")
    private String contentHtmlV2;

    private String formatTime;

    @Lob
    @JSONField(name = "gray_info")
    private String grayInfoJson;

    @Lob
    @JSONField(name = "extra")
    private String extraJson;

    @Lob
    @JSONField(name = "topic")
    private String topicJson;
}
