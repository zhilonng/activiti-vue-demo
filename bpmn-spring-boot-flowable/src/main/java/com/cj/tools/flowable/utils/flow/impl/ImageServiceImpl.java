package com.cj.tools.flowable.utils.flow.impl;

import com.cj.tools.flowable.utils.flow.ImageService;
import com.cj.tools.flowable.utils.flow.impl.image.CustomProcessDiagramGenerator;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.Execution;
import org.flowable.image.ProcessDiagramGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @Description:
 * @author: hzl
 * @date: 2020/8/19 5:43 下午
 */
@Service
public class ImageServiceImpl implements ImageService {
    protected static Logger logger = LoggerFactory.getLogger(ImageServiceImpl.class);
    @Resource
    private RepositoryService repositoryService;
    @Resource
    private HistoryService historyService;
    @Resource
    private RuntimeService runtimeService;

    /**
     * 通过流程实例ID获取历史流程实例
     *
     * @param procInstId
     * @return
     */
    public HistoricProcessInstance getHistoricProcInst(String procInstId) {
        return historyService.createHistoricProcessInstanceQuery().processInstanceId(procInstId).singleResult();
    }

    /**
     * 通过流程实例ID获取流程中已经执行的节点，按照执行先后顺序排序
     *
     * @param procInstId
     * @return
     */
    public List<HistoricActivityInstance> getHistoricActivityInstAsc(String procInstId) {
        return historyService.createHistoricActivityInstanceQuery().processInstanceId(procInstId)
                .orderByHistoricActivityInstanceId().asc().list();
    }

    /**
     * 通过流程实例ID获取流程中正在执行的节点
     *
     * @param procInstId
     * @return
     */
    public List<Execution> getRunningActivityInst(String procInstId) {
        return runtimeService.createExecutionQuery().processInstanceId(procInstId).list();
    }

    /**
     * 通过流程实例ID获取已经完成的历史流程实例
     *
     * @param procInstId
     * @return
     */
    public List<HistoricProcessInstance> getHistoricFinishedProcInst(String procInstId) {
        return historyService.createHistoricProcessInstanceQuery().processInstanceId(procInstId).finished().list();
    }

    /**
     * 获取已流经的流程线，需要高亮显示高亮流程已发生流转的线id集合
     *
     * @param bpmnModel
     * @param historicActivityInstanceList
     * @return
     */
    public List<String> getHighLightedFlows(BpmnModel bpmnModel,
                                            List<HistoricActivityInstance> historicActivityInstanceList) {
        // 已流经的流程线，需要高亮显示
        List<String> highLightedFlowIdList = new ArrayList<>();
        // 全部活动节点
        List<FlowNode> allHistoricActivityNodeList = new ArrayList<>();
        // 已完成的历史活动节点
        List<HistoricActivityInstance> finishedActivityInstanceList = new ArrayList<>();

        for (HistoricActivityInstance historicActivityInstance : historicActivityInstanceList) {
            // 获取流程节点
            try {
                FlowNode flowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstance
                        .getActivityId(), true);
                allHistoricActivityNodeList.add(flowNode);
                // 结束时间不为空，当前节点则已经完成
                if (historicActivityInstance.getEndTime() != null) {
                    finishedActivityInstanceList.add(historicActivityInstance);
                }
            }catch (Exception e){}
        }

        FlowNode currentFlowNode = null;
        FlowNode targetFlowNode = null;
        HistoricActivityInstance currentActivityInstance;
        // 遍历已完成的活动实例，从每个实例的outgoingFlows中找到已执行的
        for (int k = 0; k < finishedActivityInstanceList.size(); k++) {
            currentActivityInstance = finishedActivityInstanceList.get(k);
            // 获得当前活动对应的节点信息及outgoingFlows信息
            currentFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(currentActivityInstance
                    .getActivityId(), true);
            // 当前节点的所有流出线
            List<SequenceFlow> outgoingFlowList = currentFlowNode.getOutgoingFlows();

            /**
             * 遍历outgoingFlows并找到已流转的 满足如下条件认为已流转：
             * 1.当前节点是并行网关或兼容网关，则通过outgoingFlows能够在历史活动中找到的全部节点均为已流转
             * 2.当前节点是以上两种类型之外的，通过outgoingFlows查找到的时间最早的流转节点视为有效流转
             * (第2点有问题，有过驳回的，会只绘制驳回的流程线，通过走向下一级的流程线没有高亮显示)
             */
            if ("parallelGateway".equals(currentActivityInstance.getActivityType()) || "inclusiveGateway".equals(
                    currentActivityInstance.getActivityType())) {
                // 遍历历史活动节点，找到匹配流程目标节点的
                for (SequenceFlow outgoingFlow : outgoingFlowList) {
                    // 获取当前节点流程线对应的下级节点
                    targetFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(outgoingFlow.getTargetRef(),
                            true);
                    // 如果下级节点包含在所有历史节点中，则将当前节点的流出线高亮显示
                    if (allHistoricActivityNodeList.contains(targetFlowNode)) {
                        highLightedFlowIdList.add(outgoingFlow.getId());
                    }
                }
            } else {
                /**
                 * 2、当前节点不是并行网关或兼容网关
                 * 【已解决-问题】如果当前节点有驳回功能，驳回到申请节点，
                 * 则因为申请节点在历史节点中，导致当前节点驳回到申请节点的流程线被高亮显示，但实际并没有进行驳回操作
                 */
                List<Map<String, Object>> tempMapList = new ArrayList<>();
                // 当前节点ID
                String currentActivityId = currentActivityInstance.getActivityId();
                int size = historicActivityInstanceList.size();
                boolean ifStartFind = false;
                boolean ifFinded = false;
                HistoricActivityInstance historicActivityInstance;
                // 循环当前节点的所有流出线
                // 循环所有历史节点
                logger.info("【开始】-匹配当前节点-ActivityId=【{}】需要高亮显示的流出线", currentActivityId);
                logger.info("循环历史节点");
                for (int i = 0; i < historicActivityInstanceList.size(); i++) {
                    // // 如果当前节点流程线对应的下级节点在历史节点中，则该条流程线进行高亮显示（【问题】有驳回流程线时，即使没有进行驳回操作，因为申请节点在历史节点中，也会将驳回流程线高亮显示-_-||）
                    // if (historicActivityInstance.getActivityId().equals(sequenceFlow.getTargetRef())) {
                    // Map<String, Object> map = new HashMap<>();
                    // map.put("highLightedFlowId", sequenceFlow.getId());
                    // map.put("highLightedFlowStartTime", historicActivityInstance.getStartTime().getTime());
                    // tempMapList.add(map);
                    // // highLightedFlowIdList.add(sequenceFlow.getId());
                    // }
                    // 历史节点
                    historicActivityInstance = historicActivityInstanceList.get(i);
                    logger.info("第【{}/{}】个历史节点-ActivityId=[{}]", i + 1, size, historicActivityInstance.getActivityId());
                    // 如果循环历史节点中的id等于当前节点id，从当前历史节点继续先后查找是否有当前节点流程线等于的节点
                    // 历史节点的序号需要大于等于已完成历史节点的序号，防止驳回重审一个节点经过两次是只取第一次的流出线高亮显示，第二次的不显示
                    if (i >= k && historicActivityInstance.getActivityId().equals(currentActivityId)) {
                        logger.info("第[{}]个历史节点和当前节点一致-ActivityId=[{}]", i + 1, historicActivityInstance
                                .getActivityId());
                        ifStartFind = true;
                        // 跳过当前节点继续查找下一个节点
                        continue;
                    }
                    if (ifStartFind) {
                        logger.info("[开始]-循环当前节点-ActivityId=【{}】的所有流出线", currentActivityId);

                        ifFinded = false;
                        for (SequenceFlow sequenceFlow : outgoingFlowList) {
                            // 如果当前节点流程线对应的下级节点在其后面的历史节点中，则该条流程线进行高亮显示
                            // 【问题】
                            logger.info("当前流出线的下级节点=[{}]", sequenceFlow.getTargetRef());
                            if (historicActivityInstance.getActivityId().equals(sequenceFlow.getTargetRef())) {
                                logger.info("当前节点[{}]需高亮显示的流出线=[{}]", currentActivityId, sequenceFlow.getId());
                                highLightedFlowIdList.add(sequenceFlow.getId());
                                // 暂时默认找到离当前节点最近的下一级节点即退出循环，否则有多条流出线时将全部被高亮显示
                                ifFinded = true;
                                break;
                            }
                        }
                        logger.info("[完成]-循环当前节点-ActivityId=【{}】的所有流出线", currentActivityId);
                    }
                    if (ifFinded) {
                        // 暂时默认找到离当前节点最近的下一级节点即退出历史节点循环，否则有多条流出线时将全部被高亮显示
                        break;
                    }
                }
                logger.info("【完成】-匹配当前节点-ActivityId=【{}】需要高亮显示的流出线", currentActivityId);
                // if (!CollectionUtils.isEmpty(tempMapList)) {
                // // 遍历匹配的集合，取得开始时间最早的一个
                // long earliestStamp = 0L;
                // String highLightedFlowId = null;
                // for (Map<String, Object> map : tempMapList) {
                // long highLightedFlowStartTime = Long.valueOf(map.get("highLightedFlowStartTime").toString());
                // if (earliestStamp == 0 || earliestStamp <= highLightedFlowStartTime) {
                // highLightedFlowId = map.get("highLightedFlowId").toString();
                // earliestStamp = highLightedFlowStartTime;
                // }
                // }
                // highLightedFlowIdList.add(highLightedFlowId);
                // }

            }

        }
        return highLightedFlowIdList;
    }

    /**
     * 根据流程实例Id,获取实时流程图片
     *
     * @param procInstId
     * @return
     * @throws Exception
     */
    @Override
    public byte[] generateImageByProcInstId(String procInstId) throws Exception {
        if (StringUtils.isEmpty(procInstId)) {
            logger.error("[错误]-传入的参数procInstId为空！");
            throw new Exception("[异常]-传入的参数procInstId为空！");
        }
        InputStream imageStream = null;
        try {
            // 通过流程实例ID获取历史流程实例
            HistoricProcessInstance historicProcessInstance = getHistoricProcInst(procInstId);

            // 通过流程实例ID获取流程中已经执行的节点，按照执行先后顺序排序
            List<HistoricActivityInstance> historicActivityInstanceList = getHistoricActivityInstAsc(procInstId);


            // 将已经执行的节点ID放入高亮显示节点集合
            List<String> highLightedActivitiIdList = new ArrayList<>();
            for (HistoricActivityInstance historicActivityInstance : historicActivityInstanceList) {
                highLightedActivitiIdList.add(historicActivityInstance.getActivityId());
                logger.info("已执行的节点[{}-{}-{}-{}]", historicActivityInstance.getId(), historicActivityInstance
                        .getActivityId(), historicActivityInstance.getActivityName(), historicActivityInstance
                        .getAssignee());
            }

            // 通过流程实例ID获取流程中正在执行的节点
            List<Execution> runningActivityInstanceList = getRunningActivityInst(procInstId);
            List<String> runningActivitiIdList = new ArrayList<String>();
            for (Execution execution : runningActivityInstanceList) {
                if (StringUtils.isNotEmpty(execution.getActivityId())) {
                    runningActivitiIdList.add(execution.getActivityId());
                    logger.info("执行中的节点[{}-{}-{}]", execution.getId(), execution.getActivityId(), execution.getName());
                }
            }

            // 通过流程实例ID获取已经完成的历史流程实例
            List<HistoricProcessInstance> historicFinishedProcessInstanceList = getHistoricFinishedProcInst(procInstId);

            // 定义流程画布生成器
            ProcessDiagramGenerator processDiagramGenerator = null;
            // 如果还没完成，流程图高亮颜色为绿色，如果已经完成为红色
            // if (!CollectionUtils.isEmpty(historicFinishedProcessInstanceList)) {
            // // 如果不为空，说明已经完成
            // processDiagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
            // } else {
            processDiagramGenerator = new CustomProcessDiagramGenerator();
            // }

            // 获取流程定义Model对象
            BpmnModel bpmnModel = repositoryService.getBpmnModel(historicProcessInstance.getProcessDefinitionId());


            // 获取已流经的流程线，需要高亮显示高亮流程已发生流转的线id集合
            List<String> highLightedFlowIds = getHighLightedFlows(bpmnModel, historicActivityInstanceList);

            // 使用默认配置获得流程图表生成器，并生成追踪图片字符流
            imageStream = ((CustomProcessDiagramGenerator) processDiagramGenerator)
                    .generateDiagramCustom(bpmnModel, "png",
                            highLightedActivitiIdList, runningActivitiIdList, highLightedFlowIds,
                            "宋体", "微软雅黑", "黑体",
                            null, 2.0);
            // 将InputStream数据流转换为byte[]
            byte[] buffer = new byte[imageStream.available()];
            imageStream.read(buffer);
            return buffer;
        } catch (Exception e) {
            logger.error("通过流程实例ID[{}]获取流程图时出现异常！", e);
            throw new Exception("通过流程实例ID" + procInstId + "获取流程图时出现异常！", e);
        } finally {
            if (imageStream != null) {
                imageStream.close();
            }
        }
    }
}
