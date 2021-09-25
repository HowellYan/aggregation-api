package com.atomscat.bootstrap.api;

import com.atomscat.bootstrap.entity.UserEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.ws.rs.BeanParam;


@RequestMapping(value = "/test/api")
@Api("/test/api")
public interface IndexApi {


    @ApiOperation("getUserEntity")
    @GetMapping(value = "/getUserEntity")
    UserEntity getUserEntity(@BeanParam UserEntity userEntity);
}
