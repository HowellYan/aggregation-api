package com.atomscat.bootstrap.param.request;


import lombok.Data;

import java.io.Serializable;

/**
 * @author Howell
 * @date 2021/6/12 0:36
 */
@Data
public class AddUserEntityRequestParam implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 手机号码
     */
    private String userMobile;
}
