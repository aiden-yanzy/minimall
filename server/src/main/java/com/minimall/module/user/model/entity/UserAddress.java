package com.minimall.module.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_address")
public class UserAddress extends BaseEntity {
    private Long userId;
    private String contact;
    private String mobile;
    private Integer provinceId;
    private Integer cityId;
    private Integer districtId;
    private String address;
    private java.math.BigDecimal longitude;
    private java.math.BigDecimal latitude;
    private Integer isDefault;
}
