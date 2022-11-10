package com.atomscat.bootstrap.modules.weixincp.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import javax.persistence.*;
import java.util.List;
import java.util.Map;

@Data
@Entity
@Table(name = "t_tree_origin")
public class TreeOrigin {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    private Boolean root;

    @JSONField(name = "category_id")
    private Long categoryId;

    @JSONField(name = "doc_id")
    private Long docId;

    @OneToMany(mappedBy = "id", fetch = FetchType.LAZY)
    private List<TreeOrigin> treeOrigin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name = "fk_t_tree_origin_id"))
    private TreeOrigin treeOriginParent;

    private Long time;

    private String author;

    private Integer type;

    private Integer status;

    private String title;

    @JSONField(name = "order_id")
    private Long orderId;

    @JSONField(name = "gray_status")
    public Integer grayStatus;

    @Transient
    private Map<String, TreeOrigin> children;
}
