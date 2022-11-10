package com.atomscat.bootstrap.controller;

import com.atomscat.bootstrap.param.request.AddUserEntityRequestParam;
import com.atomscat.bootstrap.param.response.AddUserEntityResponseParam;
import org.springframework.web.bind.annotation.*;

/**
 * @author Howell
 * @date 2021/6/12 0:25
 */
@RestController
@RequestMapping(value = "/user")
public class UserController {


    @PostMapping(value = "/add/json" )
    public AddUserEntityResponseParam addJson(@RequestBody AddUserEntityRequestParam addUserEntityRequestParam) {
        return new AddUserEntityResponseParam();
    }

    @PostMapping(value = "/add/urlform", consumes = "application/x-www-form-urlencoded")
    public AddUserEntityResponseParam addUrlForm(@ModelAttribute AddUserEntityRequestParam addUserEntityRequestParam) {
        return new AddUserEntityResponseParam();
    }

    @PostMapping(value = "/add/form", consumes = "multipart/form-data")
    public AddUserEntityResponseParam addForm(@ModelAttribute AddUserEntityRequestParam addUserEntityRequestParam) {
        return new AddUserEntityResponseParam();
    }
}
