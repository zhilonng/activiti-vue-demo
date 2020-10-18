package com.cj.tools.flowable.config;

import org.flowable.engine.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @Description:
 * @author: hzl
 * @date: 2020/10/20 2:00 下午
 */
@Configuration
public class FlowableServiceConfig {

    @Resource
    private ProcessEngine processEngine;

    @Bean
    @ConditionalOnMissingBean
    public RepositoryService getRepositoryService() {
        return processEngine.getRepositoryService();
    }

    @Bean
    @ConditionalOnMissingBean
    public HistoryService getHistoryService() {
        return processEngine.getHistoryService();
    }

    @Bean
    @ConditionalOnMissingBean
    public RuntimeService getRuntimeService() {
        return processEngine.getRuntimeService();
    }

    @Bean
    @ConditionalOnMissingBean
    public TaskService getTaskService() {
        return processEngine.getTaskService();
    }

}
