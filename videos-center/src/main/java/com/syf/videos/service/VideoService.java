package com.syf.videos.service;


import com.syf.videos.entity.Video;

import java.util.List;

public interface VideoService {

    /**
     * 条件分页查询
     * @param offset 起始位置
     * @param limit 每页显示记录数
     * @param id  视频id
     * @param name 视频名称
     * @param categoryId 类别id
     * @param username   用户名
     */
    List<Video> findAllByKeywords(int offset, int limit, String id, String name, String categoryId, String username);

    /**
     *
     * @param id  视频id
     * @param name 视频名称
     * @param categoryId 类别id
     * @param username   用户名
     * @return 条件符合条数
     */
    Long findTotalCountsByKeywords(String id, String name, String categoryId, String username);
}
