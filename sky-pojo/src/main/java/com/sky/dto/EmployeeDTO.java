package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author liyun
 */
@Data
@ApiModel(value = "")
public class EmployeeDTO implements Serializable {

    @ApiModelProperty("员工id")
    private Long id;

    @ApiModelProperty("员工账号")
    private String username;

    @ApiModelProperty("员工姓名")
    private String name;

    @ApiModelProperty("员工手机号")
    private String phone;

    @ApiModelProperty("")
    private String sex;

    private String idNumber;

}
