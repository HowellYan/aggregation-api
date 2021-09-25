package com.atomscat.bootstrap.modules.weixincp.entity;

import lombok.Data;

@Data
public class ApiParam {

    private String name;

    private Boolean required = false;

    private String description;

}
