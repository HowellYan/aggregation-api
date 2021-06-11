package com.atomscat.bootstrap.controller;

import com.atomscat.bootstrap.param.request.AddUserEntityRequestParam;
import com.atomscat.bootstrap.param.response.AddUserEntityResponseParam;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

/**
 * @author Howell
 * @date 2021/6/12 0:25
 */
@RestController
@RequestMapping(value = "/user")
@Api(value = "用户数据接口", tags = {"index", "用户"})
public class UserController {


    @ApiOperation(value = "添加用户信息")
    @PostMapping(value = "/add/json" )
    public AddUserEntityResponseParam addJson(@RequestBody AddUserEntityRequestParam addUserEntityRequestParam) {
        return new AddUserEntityResponseParam();
    }

    @ApiOperation(value = "添加用户信息")
    @PostMapping(value = "/add/urlform", consumes = "application/x-www-form-urlencoded")
    public AddUserEntityResponseParam addUrlForm(@ModelAttribute AddUserEntityRequestParam addUserEntityRequestParam) {
        return new AddUserEntityResponseParam();
    }

    @ApiOperation(value = "添加用户信息")
    @PostMapping(value = "/add/form", consumes = "multipart/form-data")
    public AddUserEntityResponseParam addForm(@ModelAttribute AddUserEntityRequestParam addUserEntityRequestParam) {
        return new AddUserEntityResponseParam();
    }
}
