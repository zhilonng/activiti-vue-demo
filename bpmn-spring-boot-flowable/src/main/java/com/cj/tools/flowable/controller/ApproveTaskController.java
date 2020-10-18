package com.cj.tools.flowable.controller;

import com.cj.tools.flowable.model.ApproveTask;
import com.cj.tools.flowable.service.ApproveTaskService;
import com.cj.tools.flowable.vo.JsonResult;
import com.cj.tools.flowable.vo.QueryResult;
import com.cj.tools.flowable.vo.Resp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description:
 * @author: hzl
 * @date: 2020/8/19 2:12 下午
 */
@RestController
@RequestMapping("/task")
public class ApproveTaskController {

    @Autowired
    private ApproveTaskService approveTaskService;

    @GetMapping("/{userId}/todo")
    public JsonResult todo(@PathVariable("userId") Integer userId, Integer page, Integer pageSize) {
        ApproveTask approveTask = new ApproveTask();
        approveTask.setApproveId(userId);
        approveTask.setStatus((byte) 1);
        QueryResult<ApproveTask> queryResult = approveTaskService.pageQuery(page, pageSize, approveTask);
        return Resp.ok(queryResult);
    }

    @GetMapping("/{userId}/done")
    public JsonResult done(@PathVariable("userId") Integer userId, Integer page, Integer pageSize) {
        ApproveTask approveTask = new ApproveTask();
        approveTask.setApproveId(userId);
        approveTask.setStatus((byte) 2);
        QueryResult<ApproveTask> queryResult = approveTaskService.pageQuery(page, pageSize, approveTask);
        return Resp.ok(queryResult);
    }
}
