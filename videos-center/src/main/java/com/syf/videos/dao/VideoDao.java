package com.syf.videos.dao;


import com.syf.videos.entity.Video;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VideoDao {

    /**
     * 条件分页查询
     * @param offset 起始位置
     * @param limit 每页显示记录数
     * @param id  视频id
     * @param name 视频名称
     * @param categoryId 类别id
     * @param username   用户名
     */
    List<Video> findAllByKeywords(@Param("offset") int offset, @Param("limit") int limit, @Param("id") String id, @Param("title") String name, @Param("categoryId") String categoryId, @Param("username") String username);

    /**
     *
     * @param id  视频id
     * @param name 视频名称
     * @param categoryId 类别id
     * @param username   用户名
     * @return 条件符合条数
     */
    Long findTotalCountsByKeywords(@Param("id") String id, @Param("title") String name, @Param("categoryId") String categoryId, @Param("username") String username);


}
