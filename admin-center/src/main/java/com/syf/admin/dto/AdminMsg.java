package com.syf.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 作为admin在前后端直接传输的对象，省略了一些属性，比如创建时间等，减少json的数据大小，进而增加网络传输速度
 * @author syf
 */
public class AdminMsg {

    /**
     * 用户姓名
     */
    @JsonProperty("name")
    private String username;
    /**
     * 用户头像
     */
    private String avatar;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
