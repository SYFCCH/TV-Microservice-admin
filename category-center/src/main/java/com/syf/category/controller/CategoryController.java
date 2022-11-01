package com.syf.category.controller;


import com.syf.category.entity.Category;
import com.syf.category.service.CategoryService;
import com.syf.common.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.List;

/**
 * 分类(Category)表控制层
 *
 * @author makejava
 * @since 2022-10-26 23:00:05
 */
@RestController
@RequestMapping("/categories")
public class CategoryController {

    private static final Logger log = LoggerFactory.getLogger(CategoryController.class);

    /**
     * 服务对象
     */
    private CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }


    /**
     * 查看一级类别
     * @return
     */
    @GetMapping
    public List<Category> getCategories() {
        return categoryService.queryByFirstLevel();
    }


    /**
     * 更新类别
     * @return
     */
    @PatchMapping("/{id}")
    public Category updateCategory(@PathVariable("id") Integer id, @RequestBody Category category) {
        log.info("更新类别id: {}",id);
        log.info("更新类别信息: {}", JsonUtil.writeJson(category));
        category.setId(id);
        return categoryService.update(category);
    }

    /**
     * 新增类别
     * @param category
     * @return
     */
    @PostMapping
    public Category insertCategory(@RequestBody Category category) {
        log.info("添加类别信息: {}", JsonUtil.writeJson(category));
        Category category1 = categoryService.insert(category);
        log.info("添加之后的类别信息: {}",JsonUtil.writeJson(category1));
        return  category1;
    }

    @DeleteMapping("/{id}")
    public void DeleteCategory(@PathVariable("id") Integer id) {
        boolean b = categoryService.deleteById(id);
        if(b) {
            log.info("删除成功");
        }else{
            log.info("删除失败");
        }
    }

}

