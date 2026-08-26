package com.minimall.module.goods.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minimall.module.goods.mapper.GoodsCategoryMapper;
import com.minimall.module.goods.model.entity.GoodsCategory;
import com.minimall.module.goods.model.vo.CategoryTreeNode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsCategoryService extends ServiceImpl<GoodsCategoryMapper, GoodsCategory> {

    /** 分类树 */
    public List<CategoryTreeNode> tree() {
        List<GoodsCategory> all = this.lambdaQuery().orderByAsc(GoodsCategory::getSort).list();
        return toTree(all, 0L);
    }

    private List<CategoryTreeNode> toTree(List<GoodsCategory> all, Long pid) {
        return all.stream()
                .filter(c -> c.getPid() != null && c.getPid().equals(pid))
                .map(c -> {
                    CategoryTreeNode node = new CategoryTreeNode();
                    node.setId(c.getId());
                    node.setName(c.getName());
                    node.setPid(c.getPid());
                    node.setIcon(c.getIcon());
                    node.setSort(c.getSort());
                    node.setChildren(toTree(all, c.getId()));
                    return node;
                })
                .collect(Collectors.toList());
    }
}
