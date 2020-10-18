package com.cj.tools.activiti.vo.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Description:
 * @author: hzl
 * @date: 2020/10/20 11:39 下午
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class JsonResult <T> implements Serializable {

    private int code;

    private String msg;

    private T data;
}
