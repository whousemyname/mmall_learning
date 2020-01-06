package com.gogotao.service;

import com.gogotao.common.ServerResponse;
import com.gogotao.pojo.Category;
import java.util.List;

public interface ICategoryService {

    ServerResponse addCategory(String categoryName, Integer parentId);

    ServerResponse setCategoryName(Integer categoryId, String categoryName);

    ServerResponse<List<Category>> getChildrenParallelCategory(Integer parentId);

    ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId);

}
