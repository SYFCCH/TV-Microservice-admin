package com.syf.user.service;

import com.syf.user.entity.User;

import java.util.List;

public interface UserService {

     /**
      * 分页查询
      * @param pageNow
      * @param rows
      * @param id
      * @param name
      * @param phone
      * @return
      */
     List<User> findAllByKeywords(int pageNow, int rows, String id, String name, String phone);


     /**
      * 根据条件查询总条数
      */
      Long findTotalCountsByKeywords(String id, String name, String phone);

}
