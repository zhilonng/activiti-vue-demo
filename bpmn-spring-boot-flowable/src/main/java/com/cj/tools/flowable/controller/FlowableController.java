package com.cj.tools.flowable.controller;

import com.cj.tools.flowable.utils.ImageUtil;
import com.cj.tools.flowable.vo.JsonResult;
import com.cj.tools.flowable.vo.ProcessDeploymentVo;
import com.cj.tools.flowable.vo.Resp;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.*;

/**
 * @Description:
 * @author: hzl
 * @date: 2020/10/20 2:11 下午
 */
@RestController
@RequestMapping("/flow")
@Slf4j
public class FlowableController {

    @Value("${file.diagram.path}")
    private String diagramPath;

    @Resource
    private RepositoryService repositoryService;

    @PostMapping("/deploy")
    public JsonResult deploy(@RequestBody ProcessDeploymentVo deployment) {
        String xmlFileName = deployment.getResourceName() + ".bpmn20.xml";
        String pngFileName = deployment.getResourceName() + ".png";
        File xmlFile = new File(diagramPath + xmlFileName);
        File pngFile = new File(diagramPath + pngFileName);
        ImageUtil.convertToPng(deployment.getSvg(), pngFile);

        InputStream xmlInputStream = null;
        InputStream pngInputStream = null;
        try {
            FileCopyUtils.copy(deployment.getXml().getBytes(), xmlFile);
            xmlInputStream = new FileInputStream(xmlFile);
            pngInputStream = new FileInputStream(pngFile);
        } catch (IOException e) {
        }
        repositoryService.createDeployment().
                addInputStream(xmlFileName, xmlInputStream)
                .addInputStream(pngFileName, pngInputStream)
                .name(deployment.getProcessName())
                .key(deployment.getProcessKey())
                .deploy();

        return Resp.ok("流程发布成功");
    }



}
