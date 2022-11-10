package com.atomscat.bootstrap.modules.weixincp.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @author th158
 */
@Data
@Entity
@Table(name="t_tree_banner_card_conf", indexes = {@Index(name = "value_pid", columnList = "value, tree_root_id", unique = true)})
public class TreeBannerCardConf {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tree_root_id", foreignKey = @ForeignKey(name = "fk_tree_root_id"))
    private TreeRoot treeRoot;

    private String value;

    private String label;

    private String link;

    private String icon;

}
