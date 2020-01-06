package com.gogotao.service.impl;

import com.gogotao.common.ServerResponse;
import com.gogotao.dao.CategoryMapper;
import com.gogotao.pojo.Category;
import com.gogotao.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.ls.LSInput;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);
    @Override
    public ServerResponse addCategory(String categoryName, Integer parentId){
        if (parentId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("创建分类参数错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);

        if (categoryMapper.insert(category) == 0){
            return ServerResponse.createByErrorMessage("插入分类错误");
        }
        return ServerResponse.createBySuccessMessage("插入分类成功");
    }

    @Override
    public ServerResponse setCategoryName(Integer categoryId, String categoryName){
        if (categoryId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("创建分类参数错误");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        if (categoryMapper.updateByPrimaryKeySelective(category) == 0){
            return ServerResponse.createByErrorMessage("修改分类名称失败");
        }
        return ServerResponse.createBySuccessMessage("修改分类名称成功");
    }

    @Override
    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer parentId){
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(parentId);
        if (CollectionUtils.isEmpty(categoryList)){
            logger.info("未找到当前分类的子分类");
        }
        return ServerResponse.createBySuccessData("获取分类成功", categoryList);
    }

    @Override
    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId){
        Set<Category> categorySet = new HashSet<>();
        findChildrenCategory(categorySet, categoryId);
        List<Integer> list = new ArrayList<>(categorySet.size());
        for (Category category : categorySet){
            list.add(category.getId());
        }
        return ServerResponse.createBySuccessData(list);
    }
    private Set<Category> findChildrenCategory(Set<Category> set, Integer categoryId){
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category != null){
            set.add(category);
        }
        List<Category> list = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        for (Category categoryItem : list){
            findChildrenCategory(set, categoryItem.getId());
        }
        return set;
    }
}
