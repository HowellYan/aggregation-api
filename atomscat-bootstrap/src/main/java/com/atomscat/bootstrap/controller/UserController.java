package com.atomscat.bootstrap.controller;

import com.atomscat.bootstrap.param.request.AddUserEntityRequestParam;
import com.atomscat.bootstrap.param.response.AddUserEntityResponseParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * @author Howell
 * @date 2021/6/12 0:25
 */
@RestController
@RequestMapping(value = "/user")
@Tag(name = "用户信息")
public class UserController {


    @PostMapping(value = "/add/json" )
    @Operation(summary = "添加用户信息 json格式")
    public AddUserEntityResponseParam addJson(@RequestBody AddUserEntityRequestParam addUserEntityRequestParam) {
        return new AddUserEntityResponseParam();
    }

    @PostMapping(value = "/add/urlform", consumes = "application/x-www-form-urlencoded")
    @Operation(summary = "添加用户信息 form-urlencoded格式")
    public AddUserEntityResponseParam addUrlForm(@ModelAttribute AddUserEntityRequestParam addUserEntityRequestParam) {
        return new AddUserEntityResponseParam();
    }

    @PostMapping(value = "/add/form", consumes = "multipart/form-data")
    @Operation(summary = "添加用户信息 form-data")
    public AddUserEntityResponseParam addForm(@ModelAttribute AddUserEntityRequestParam addUserEntityRequestParam) {
        return new AddUserEntityResponseParam();
    }
}
