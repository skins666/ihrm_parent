package com.ihrm.audit.controller;

import com.ihrm.audit.entity.ProcInstance;
import com.ihrm.audit.entity.ProcTaskInstance;
import com.ihrm.audit.service.AuditService;
import com.ihrm.audit.service.ProcessService;
import com.ihrm.common.controller.BaseController;
import com.ihrm.common.entity.PageResult;
import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 流程控制的controller
 */
@CrossOrigin
@RestController
@RequestMapping(value="/user/process")
public class ProcessController extends BaseController {

	@Autowired
	private ProcessService processService;

	@Autowired
	private AuditService auditService;

	/**
	 * 部署新流程
	 *     前端将绘制好的流程模型图(bpmn)文件上传到方法中
	 *     参数 : 上传的文件
	 *          MultipartFile
	 */
	@RequestMapping(value = "/deploy",method = RequestMethod.POST)
	public Result deployProcess(@RequestParam("file") MultipartFile file) throws IOException {
		processService.deployProcess(file,companyId);
		return new Result(ResultCode.SUCCESS);
	}

	/**
	 * 查询所有的流程定义
	 */
	@RequestMapping(value = "/definition",method = RequestMethod.GET)
	public Result definitionList() throws IOException {
		//调用service查询
		List list = processService.getProcessDefinitionList(companyId);
		return new Result(ResultCode.SUCCESS,list);
	}

	/**
	 * 设置流程的挂起与激活状态
	 */
	@RequestMapping(value = "/suspend/{processKey}",method = RequestMethod.GET)
	public Result suspendProcess(@PathVariable String processKey) throws IOException {
		processService.suspendProcess(processKey,companyId);
		return new Result(ResultCode.SUCCESS);
	}


	/**
	 * 查询申请列表
	 *  参数:
	 *      page,size
	 *  业务参数:
	 *      审批类型
	 *      审批状态(多个,每个状态之间使用","隔开)
	 *      当前节点的待处理人
	 */
	@RequestMapping(value = "/instance/{page}/{size}",method = RequestMethod.PUT)
	public Result instanceList(@RequestBody ProcInstance instance,@PathVariable int page,@PathVariable int size) throws IOException {
		//1.调用service分页查询(springdatajpa封装的page对象)
		Page pages = auditService.getInstanceList(instance,page,size);
		//2.page对象转化为自己的pageResult对象
		PageResult pr = new PageResult(pages.getTotalElements(),pages.getContent());
		//3.返回
		return new Result(ResultCode.SUCCESS,pr);
	}


	/**
	 * 查询申请的详情数据
	 *  参数 : 申请对象的id
	 */
	@RequestMapping(value = "/instance/{id}",method = RequestMethod.GET)
	public Result instanceDetail(@PathVariable String id) throws IOException {
		//调用service根据id查询
		ProcInstance instance = auditService.findInstanceDetail(id);
		return new Result(ResultCode.SUCCESS,instance);
	}


	/**
	 * 流程申请
	 */
	@RequestMapping(value = "/startProcess",method = RequestMethod.POST)
	public Result startProcess(@RequestBody Map map) throws IOException {
		//调用service
		auditService.startProcess(map,companyId);
		return new Result(ResultCode.SUCCESS);
	}

	/**
	 * 提交审核
	 *      handleType; // 处理类型（2审批通过；3审批不通过；4撤销）
	 */
	@RequestMapping(value = "/instance/commit",method = RequestMethod.PUT)
	public Result commit(@RequestBody ProcTaskInstance taskInstance) throws IOException {
		//调用service
		auditService.commit(taskInstance,companyId);
		return new Result(ResultCode.SUCCESS);
	}

	//查询流程任务明细
	@RequestMapping(value = "/instance/tasks/{id}",method = RequestMethod.GET)
	public Result tasks(@PathVariable String id) throws IOException {
		//调用service
		return new Result(ResultCode.SUCCESS,auditService.findTasksByProcess(id));
	}

}
