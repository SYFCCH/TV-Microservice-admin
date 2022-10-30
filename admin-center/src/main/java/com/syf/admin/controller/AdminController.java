package com.syf.admin.controller;

import com.syf.admin.dto.AdminMsg;
import com.syf.admin.entity.Admin;
import com.syf.admin.service.AdminService;
import com.syf.common.constants.RedisPrefix;
import com.syf.common.utils.JsonUtil;
import com.syf.redis.config.RedisAutoConfig;
import com.syf.redis.util.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
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

    ApplicationContext context = new AnnotationConfigApplicationContext(RedisAutoConfig.class);
    RedisUtils redisUtils;
    public AdminController() {}

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
        redisUtils = (RedisUtils) context.getBean("redisUtils");
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
        //登录成功，则拿token
        String token = session.getId();
        redisUtils.set(RedisPrefix.TOKEN_KEY+token,adminDB,30, TimeUnit.MINUTES);
        map.put("token",token);
        return map;
    }

    /**
     *  登录以后获得用户信息
     */
    @GetMapping("/admin-user")
    public AdminMsg getAdmin(String token) {
        log.info("当前token信息: {}",token);
        Admin admin = (Admin) redisUtils.get(RedisPrefix.TOKEN_KEY+token);
        AdminMsg adminMsg = new AdminMsg();
        BeanUtils.copyProperties(admin,adminMsg);
        return adminMsg;
    }

    @DeleteMapping("/tokens/{token}")
    public void logout(@PathVariable String token) {
        redisUtils.del(RedisPrefix.TOKEN_KEY+token);
    }



    /**
     * 分页查询
     *
     * @param admin 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */
    @GetMapping
    public ResponseEntity<Page<Admin>> queryByPage(Admin admin, PageRequest pageRequest) {
        return ResponseEntity.ok(this.adminService.queryByPage(admin, pageRequest));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public ResponseEntity<Admin> queryById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(this.adminService.queryById(id));
    }

    /**
     * 新增数据
     *
     * @param admin 实体
     * @return 新增结果
     */
    @PostMapping
    public ResponseEntity<Admin> add(Admin admin) {
        return ResponseEntity.ok(this.adminService.insert(admin));
    }

    /**
     * 编辑数据
     *
     * @param admin 实体
     * @return 编辑结果
     */
    @PutMapping
    public ResponseEntity<Admin> edit(Admin admin) {
        return ResponseEntity.ok(this.adminService.update(admin));
    }

    /**
     * 删除数据
     *
     * @param id 主键
     * @return 删除是否成功
     */
    @DeleteMapping
    public ResponseEntity<Boolean> deleteById(Integer id) {
        return ResponseEntity.ok(this.adminService.deleteById(id));
    }

}

