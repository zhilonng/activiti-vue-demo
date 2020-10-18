package com.cj.tools.flowable.utils.flow;

/**
 * @Description:
 * @author: hzl
 * @date: 2020/8/19 5:40 下午
 */
public interface ImageService {

    /**
     * 流程追踪图生成类
     *
     * @param procInstId
     * @return
     * @throws Exception
     */
    byte[] generateImageByProcInstId(String procInstId) throws Exception;
}
