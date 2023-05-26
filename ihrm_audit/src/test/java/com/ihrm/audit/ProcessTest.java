package com.ihrm.audit;

import com.ihrm.audit.dao.ProcUserGroupDao;
import com.ihrm.audit.entity.ProcUserGroup;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class ProcessTest {

	/**
	 * 注入activiti提供的查询的service
	 *      RepositoryService
	 */
	@Autowired
	private RepositoryService repositoryService;


	/**
	 * 测试查询所有的流程定义对象
	 */
	@Test
	public void findAll() {
		//1.获取流程定义查询对象query
		ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
		//2.添加查询的条件
		query.processDefinitionTenantId("传智播客");
		//3.查询
		List<ProcessDefinition> list = query.latestVersion().list();
		System.out.println(list.size());
	}

	/**
	 * 测试流程的挂起与激活
	 * 	 *      流程定义表:act_re_procdef
	 *          状态字段 :SUSPENSION_STATE_
	 *                  1.激活状态
	 *                  2.挂起状态
	 */
	@Test
	public void testSuspend() {
		//流程挂起
		//repositoryService.suspendProcessDefinitionById("process_leave:1:42656fa9-d155-11e9-a469-005056c00008");

		//流程激活
		repositoryService.activateProcessDefinitionById("process_leave:1:42656fa9-d155-11e9-a469-005056c00008");
	}

}
