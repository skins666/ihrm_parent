package com.ihrm.audit;

import com.ihrm.audit.dao.ProcUserGroupDao;
import com.ihrm.audit.entity.ProcUserGroup;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class AuditTest {

	/**
	 * 测试业务数据库
	 */
	@Autowired
	private ProcUserGroupDao procUserGroupDao;

	@Test
	public void test() {
		List<ProcUserGroup> list = procUserGroupDao.findAll();
		System.out.println(list.size());
	}

	/**
	 * 测试activiti数据库
	 *  使用activiti提供的接口测试
	 *      activiti提供的对象: RepositoryService
	 */
	@Autowired
	private RepositoryService repositoryService;

	@Test
	public void testActiviti() {
		//测试查看activiti中的所有流程
		List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().list();
		System.out.println(list.size());
	}

}
