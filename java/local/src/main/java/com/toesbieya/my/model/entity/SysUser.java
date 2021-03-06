package com.toesbieya.my.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysUser {
    private Integer id;
    private String name;
    private String pwd;
    private Integer role;
    private String avatar;
    private Long ctime;
    private Integer admin;
    private Integer status;
}
