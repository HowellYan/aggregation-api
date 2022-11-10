package com.atomscat.bootstrap.controller;

import com.atomscat.bootstrap.entity.UserEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Howell
 * @date 2021/6/11 22:39
 */
@RestController
@RequestMapping(value = "/index")
@Api(value = "首页数据接口", tags = {"index", "首页"})
@ApiOperation(value = "首页数据接口")
public class IndexController {

    @ApiOperation(value = "首页用户信息")
    @GetMapping(value = "/getUserEntity",name = "/getUserEntity")
    public UserEntity getUserEntity(UserEntity userEntity) {
        return new UserEntity();
    }

}
