package com.cj.tools.flowable.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @Description:
 * @author: hzl
 * @date: 2020/10/20 11:09 下午
 */
@Getter
@Setter
@ToString
public class ProcessDeploymentVo {

    /**
     * 流程定义key
     */
    private String processKey;

    /**
     * 流程定义名字
     */
    private String processName;

    /**
     * 流程发布文件名字
     */
    private String resourceName;

    /**
     * 流程定义内容
     */
    private String xml;

    /**
     * 流程定义svg
     */
    private String svg;
}
