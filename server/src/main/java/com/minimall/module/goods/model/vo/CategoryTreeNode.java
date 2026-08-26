package com.minimall.module.goods.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 分类树节点
 */
@Data
public class CategoryTreeNode {
    private Long id;
    private String name;
    private Long pid;
    private String icon;
    private Integer sort;
    private List<CategoryTreeNode> children;
}
