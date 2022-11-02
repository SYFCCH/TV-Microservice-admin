package com.syf.admin.controller;

import com.syf.admin.dto.AdminMsg;
import com.syf.admin.entity.Admin;
import com.syf.admin.service.AdminService;
import com.syf.common.constants.RedisPrefix;
import com.syf.common.utils.JsonUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * (Admin)表控制层
 *
 * @author makejava
 * @since 2022-10-26 15:33:41
 */
@RestController
public class AdminController {


    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private AdminService adminService;
    private RedisTemplate redisTemplate;


    @Autowired
    public AdminController(AdminService adminService, RedisTemplate redisTemplate) {
        this.adminService = adminService;
        this.redisTemplate = redisTemplate;
    }


    /**
     * 登录方法
     */
    @PostMapping("/tokens")
    public Map<String,String> tokens(@RequestBody Admin admin, HttpSession session) {
        Map<String,String> map = new HashMap<>();
        log.info("接收到admin对象为: {}", JsonUtil.writeJson(admin));
        //进行登录
        Admin adminDB = adminService.login(admin);
        log.info("adminB {}",adminDB);
        log.info("登录成功");
        //登录成功，则拿token
        String token = session.getId();
        log.info("拿到登录token {}",token);
        redisTemplate.opsForValue().set(RedisPrefix.TOKEN_KEY + token, adminDB, 30, TimeUnit.MINUTES);
        map.put("token",token);
        return map;
    }

    /**
     *  登录以后获得用户信息
     */
    @GetMapping("/admin-user")
    public AdminMsg getAdmin(String token) {
        log.info("当前token信息: {}",token);
        Admin admin =  (Admin) redisTemplate.opsForValue().get(RedisPrefix.TOKEN_KEY + token);
        AdminMsg adminMsg = new AdminMsg();
        BeanUtils.copyProperties(admin,adminMsg);
        return adminMsg;
    }

    /**
     * 登出接口
     * @param token
     */
    @DeleteMapping("/tokens/{token}")
    public void logout(@PathVariable String token) {
        redisTemplate.delete(RedisPrefix.TOKEN_KEY + token);
    }







}

