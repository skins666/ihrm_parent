package com.ihrm.audit.service;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProcessService {

	@Autowired
	private RepositoryService repositoryService;

	@Autowired
	private RuntimeService runtimeService;

	/**
	 * 流程部署
	 * @param file  上传bpmn文件
	 * @param companyId  企业id
	 */
	public void deployProcess(MultipartFile file, String companyId) throws IOException {
		//1.获取上传的文件名
		String fileName = file.getOriginalFilename();
		//2.通过repositoryService进行流程部署
		//DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
		//文件名称,文件的bytes数组
		//deploymentBuilder.addBytes(fileName,file.getBytes()); //部署流程
		//deploymentBuilder.tenantId(companyId);
		DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().addBytes(fileName, file.getBytes()).tenantId(companyId);
		Deployment deploy = deploymentBuilder.deploy();
		//3.打印部署结果
		System.out.println(deploy);
	}

	//根据企业id查询所有的流程定义对象
	public List getProcessDefinitionList(String companyId) {
		List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(companyId)
				.latestVersion().list();
		return list;
	}

	//挂起或者激活流程
	public void suspendProcess(String processKey,String companyId) {
		//1.根据processKey查询流程定义
		ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processKey)
				.processDefinitionTenantId(companyId).latestVersion().singleResult();
		//2.判断是否为挂起状态
		if(definition.isSuspended()) {
			//2.1 如果是挂起状态:设置为激活
			repositoryService.activateProcessDefinitionById(definition.getId());
		}else {
			//2.2 如果不是激活状态: 设置为挂起
			repositoryService.suspendProcessDefinitionById(definition.getId());
		}
	}
}
