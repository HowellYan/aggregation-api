package com.atomscat.bootstrap.modules.weixincp.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

/**
 * @author th158
 */
@Data
@Entity
@Table(name="t_tree_root")
public class TreeRoot {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    private String client;

    private Boolean liteapp;

    private String type;

    // 1 -> n treeOrigin todo

    @Transient
    private TreeOrigin treeOrigin;

    // 1 -> n memberInfo todo

    private Boolean bannerGray;

    // 1 -> n docCnt todo

    @OneToMany(mappedBy = "id", fetch = FetchType.LAZY)
    private List<TreeBannerCardConf> bannerCardConf;

    // 1 -> n logininfo todo

    private String title;

}
