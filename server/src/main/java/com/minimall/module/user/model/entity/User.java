package com.minimall.module.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {

    /** 用户编号（对外展示） */
    private String sn;

    /** 昵称 */
    private String nickname;

    /** 头像 */
    private String avatar;

    /** 手机号 */
    private String mobile;

    /** 登录密码（BCrypt 加密） */
    private String password;

    /** 性别：0未知 1男 2女 */
    private Integer sex;

    /** 生日 */
    private LocalDateTime birthday;

    /** 会员等级 id */
    private Long levelId;

    /** 余额（分） */
    private Long balance;

    /** 积分 */
    private Long integral;

    /** 累计消费金额（分） */
    private Long totalConsume;

    /** 累计消费次数 */
    private Integer totalOrderNum;

    /** 状态：1正常 0禁用 */
    private Integer status;

    /** 注册渠道：h5/miniapp/wechat */
    private String channel;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;
}
