package com.bluemsun.blog.module.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluemsun.blog.common.core.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 鐢ㄦ埛瀹炰綋锛屽搴旇〃锛歵_user銆?
 */
@TableName("t_user")
public class User extends BaseEntity {

    /** 閭锛堝敮涓€鐧诲綍鍚嶏級 */
    private String email;

    /** 瀵嗙爜锛圔Crypt/SA-Token 鍔犲瘑瀛樺偍锛?*/
    private String password;

    /** 鏄电О */
    private String nickname;

    /** 澶村儚鍦板潃 */
    private String avatar;

    /** 涓汉绠€浠?*/
    private String profile;

    /** 瑙掕壊锛歎SER / ADMIN */
    private String role;

    /** 鐘舵€侊細active/disabled/blacklist */
    private String status;

    /** 鏈€杩戠櫥褰曟椂闂?*/
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;



    /** 账户余额 */

    private BigDecimal balance;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

}


