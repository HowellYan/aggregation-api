package com.atomscat.bootstrap.modules.feishu.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Howell
 * @date 2021/11/21 20:21
 */
@Data
@TableName("directory_list")
public class DirectoryList implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private String id;

    private String fullPath;

    private String name;

    private String parentId;

    private String type;

    private String documentDetail;
}
