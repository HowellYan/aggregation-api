package com.atomscat.bootstrap.modules.dingding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Howell
 * @date 2021/10/4 12:05
 */
@Data
@TableName("doc_meta")
public class DocMeta implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "唯一标识")
    private Long id;

    private Long ditaId;

    private Long gmtModify;

    private String labels;

    private Long parentId;

    private String shortDescription;

    private String slug;

    private Integer sort;

    private Long sourceId;

    private String tags;

    private String title;

    private String topic;

    private String type;

    private String docType;

    /**
     *
     */
    private String html;

    /**
     * 1: 成功
     */
    private Integer result;
}
