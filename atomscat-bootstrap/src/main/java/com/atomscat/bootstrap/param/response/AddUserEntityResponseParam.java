package com.atomscat.bootstrap.param.response;

import com.atomscat.bootstrap.entity.UserEntity;
import com.atomscat.bootstrap.entity.UserTagEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Howell
 * @date 2021/6/12 0:37
 */
@Data
public class AddUserEntityResponseParam extends UserEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<UserTagEntity> userTagEntityList;
}
