package com.atomscat.bootstrap.param.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Howell
 * @date 2021/6/12 0:36
 */
@Data
@ApiModel(value = "添加用户请求参数", description = "请求参数")
public class AddUserEntityRequestParam implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名", required = true)
    private String userName;

    /**
     * 用户昵称
     */
    @ApiModelProperty(value = "用户昵称")
    private String nickName;

    /**
     * 手机号码
     */
    @ApiModelProperty(value = "手机号码", required = true)
    private String userMobile;
}
