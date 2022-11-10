package com.atomscat.bootstrap.controller;

import com.atomscat.bootstrap.entity.UserEntity;
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
public class IndexController {

    @GetMapping(value = "/getUserEntity",name = "/getUserEntity")
    public UserEntity getUserEntity(UserEntity userEntity) {
        return new UserEntity();
    }

}
