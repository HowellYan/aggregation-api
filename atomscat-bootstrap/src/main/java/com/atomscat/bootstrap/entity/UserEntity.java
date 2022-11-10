package com.atomscat.bootstrap.entity;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Howell
 * @date 2021/6/11 22:42
 */
@Data
public class UserEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 实名状态：0-未实名；1-已实名；
     */
    private Integer realStatus;

    /**
     * 用户邮箱
     */
    private String userMail;

    /**
     * 登录密码
     */
    private String loginPassword;

    /**
     * 支付密码
     */
    private String payPassword;

    /**
     * 手机号码
     */
    private String userMobile;

    /**
     * 邀请码
     */
    private String inviteCode;

    /**
     * 修改时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date modifyTime;

    /**
     * 注册时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date userRegtime;

    /**
     * 注册IP
     */
    private String userRegip;

    /**
     * 备注
     */
    private String userMemo;

    /**
     * 0男 1女
     */
    private String sex;

    /**
     * 例如：2009-11-27
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String birthDate;

    /**
     * 头像图片路径
     */
    private String pic;

    /**
     * 状态 1 正常 0 无效
     */
    private Integer status;

    /**
     * 积分
     */
    private Integer score;

    /**
     * 会员成长值
     */
    private Long growth;

    /**
     * 会员等级
     */
    private Integer level;

    /**
     * 等级条件 0 普通会员 1 付费会员
     */
    private Integer levelType;

    /**
     * vip结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date vipEndTime;

    /**
     * 会员等级名称
     */
    private String levelName;

}
