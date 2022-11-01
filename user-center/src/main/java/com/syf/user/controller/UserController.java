package com.syf.user.controller;

import com.syf.user.entity.User;
import com.syf.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author  syf
 */
@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 分页展示用户并且返回用户条数
     */
    @GetMapping
    public Map<String,Object> users(@RequestParam(value = "page",defaultValue = "1") Integer pageNow,@RequestParam(value = "per_page",defaultValue = "5") Integer rows, @RequestParam(required = false) String id,
                                    @RequestParam(required = false)String name, @RequestParam(required = false)String phone) {
        Map<String,Object> map = new HashMap<>();
        log.info("分页信息 当前页:{} ,每页展示记录数: {}", pageNow, rows);
        log.info("搜索的值 id:{}, name:{}, phone:{}", id, name, phone);
        //查询用户并且分页展示
        List<User> items = userService.findAllByKeywords(pageNow,rows,id,name,phone);
        map.put("items",items);
        //返回总条数
        Long totalCounts = userService.findTotalCountsByKeywords(id,name,phone);
        map.put("total_count",totalCounts);
        return map;
    }
}
