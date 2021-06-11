package com.atomscat.bootstrap.param.response;

import com.atomscat.bootstrap.entity.UserEntity;
import com.atomscat.bootstrap.entity.UserTagEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Howell
 * @date 2021/6/12 0:37
 */
@Data
@ApiModel(value = "添加用户响应参数", parent = UserEntity.class, description = "响应参数")
public class AddUserEntityResponseParam extends UserEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户标签")
    private List<UserTagEntity> userTagEntityList;
}
