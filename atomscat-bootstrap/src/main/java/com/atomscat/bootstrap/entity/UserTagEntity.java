package com.atomscat.bootstrap.entity;


import lombok.Data;

import java.io.Serializable;

/**
 * @author Howell
 * @date 2021/6/11 22:43
 */
@Data
public class UserTagEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户标签id
     */
    private Integer tagId;

    /**
     * 标签名称
     */
    private String tagName;
}
