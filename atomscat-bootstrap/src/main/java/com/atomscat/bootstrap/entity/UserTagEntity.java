package com.atomscat.bootstrap.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Howell
 * @date 2021/6/11 22:43
 */
@Data
@ApiModel(value = "用户标签-数据模型", description = "面向对象的`用户标签`实体类")
public class UserTagEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户标签id
     */
    @ApiModelProperty(value = "用户标签id", example = "0")
    private Integer tagId;

    /**
     * 标签名称
     */
    @ApiModelProperty(value = "标签名称")
    private String tagName;
}
