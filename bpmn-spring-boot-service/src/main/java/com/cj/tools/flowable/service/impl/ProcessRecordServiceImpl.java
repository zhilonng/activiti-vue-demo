package com.cj.tools.flowable.service.impl;

import com.cj.tools.flowable.base.service.impl.BaseServiceImpl;
import com.cj.tools.flowable.mapper.ProcessRecordMapper;
import com.cj.tools.flowable.model.ProcessRecord;
import com.cj.tools.flowable.service.ProcessRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Description:
 * @author: hzl
 * @date: 2020/8/14 3:46 下午
 */
@Service
public class ProcessRecordServiceImpl extends BaseServiceImpl<ProcessRecordMapper, ProcessRecord, Integer> implements ProcessRecordService {

    @Resource
    private ProcessRecordMapper processRecordMapper;

}
