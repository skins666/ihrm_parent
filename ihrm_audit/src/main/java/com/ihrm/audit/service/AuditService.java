package com.ihrm.audit.service;

import com.alibaba.fastjson.JSON;
import com.ihrm.audit.client.FeignClientService;
import com.ihrm.audit.dao.ProcInstanceDao;
import com.ihrm.audit.dao.ProcTaskInstanceDao;
import com.ihrm.audit.dao.ProcUserGroupDao;
import com.ihrm.audit.entity.ProcInstance;
import com.ihrm.audit.entity.ProcTaskInstance;
import com.ihrm.audit.entity.ProcUserGroup;
import com.ihrm.common.utils.IdWorker;
import com.ihrm.domain.system.User;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.hibernate.SQLQuery;
import org.hibernate.query.internal.NativeQueryImpl;
import org.hibernate.transform.Transformers;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.util.*;

@Service
public class AuditService {

	@Autowired
	private ProcInstanceDao procInstanceDao;

	@Autowired
	private ProcTaskInstanceDao procTaskInstanceDao;


	@Autowired
	private FeignClientService feignClientService;

	@Autowired
	private IdWorker idWorker;

	@Autowired
	private RepositoryService repositoryService;

	@Autowired
	private RuntimeService runtimeService;

	@Autowired
	private TaskService taskService;

	@Autowired
	private ProcUserGroupDao procUserGroupDao;

	@Autowired
	private EntityManager entityManager;


	/**
	 * 查询所有的业务的申请流程
	 */
	public Page getInstanceList(ProcInstance instance, int page, int size) {
		//1.使用Specification查询,构造Specification
		Specification<ProcInstance> spec = new Specification<ProcInstance>() {
			//2.构造查询条件 (根据传入参数判断,构造)
			public Predicate toPredicate(Root<ProcInstance> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
				List<Predicate> list = new ArrayList<>();
				//审批类型      -- processKey
				if(!StringUtils.isEmpty(instance.getProcessKey())) {
					list.add(cb.equal(root.get("processKey").as(String.class),instance.getProcessKey()));
				}
				//审批状态(多个,每个状态之间使用","隔开)        --processState
				if(!StringUtils.isEmpty(instance.getProcessState())) {
					Expression<String> exp = root.<String>get("processState");
					list.add(exp.in(instance.getProcessState().split(",")));
				}
				//当前节点的待处理人     --procCurrNodeUserId
				if(!StringUtils.isEmpty(instance.getProcCurrNodeUserId())) {
					list.add(cb.like(root.get("procCurrNodeUserId").as(String.class),"%"+instance.getProcCurrNodeUserId()+"%"));
				}
				//发起人 -- userId
				if(!StringUtils.isEmpty(instance.getUserId())) {
					list.add(cb.equal(root.get("userId").as(String.class),instance.getUserId()));
				}
				return cb.and(list.toArray(new Predicate[list.size()]));
			}
		};
		//3.调用dao进行Specification查询
		return procInstanceDao.findAll(spec,new PageRequest(page-1,size));//
	}

	//根据id查询申请数据响应
	public ProcInstance findInstanceDetail(String id) {
		return procInstanceDao.findById(id).get();
	}

	//流程申请
	public void startProcess(Map map, String companyId) {
		//1.构造业务数据
		String userId =(String) map.get("userId");
		String processKey =(String) map.get("processKey");
		String processName =(String) map.get("processName");
		User user = feignClientService.getUserInfoByUserId(userId);
		ProcInstance instance = new ProcInstance();
		BeanUtils.copyProperties(user,instance);
		instance.setUserId(userId);
		instance.setProcessId(idWorker.nextId()+"");
		instance.setProcApplyTime(new Date());
		instance.setProcessKey(processKey);
		instance.setProcessName(processName);
		instance.setProcessState("1");
		String data = JSON.toJSONString(map);
		instance.setProcData(data);
		//2.查询流程定义
		ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processKey)
				.processDefinitionTenantId(companyId).latestVersion().singleResult();
		//3.开启流程
		Map vars = new HashMap();
		if("process_leave".equals(processKey)) {
			//请假
			vars.put("days",map.get("duration"));
		}
		ProcessInstance processInstance = runtimeService.startProcessInstanceById(definition.getId(), instance.getProcessId(), vars);//流程定义的id,业务数据id,内置的参数
		//4.自动执行第一个任务节点
		//4.1 获取待执行的任务节点
		Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
		//4.2 执行即可
		taskService.complete(task.getId());
		//5.获取下一个节点数据,填充业务数据中当前待审批人
		//获取下一个节点,
		Task next = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
		if(next != null) {
			List<User> users = findCurrUsers(next, user);
			String usernames = "", userIdS = "";
			for (User user1 : users) {
				usernames += user1.getUsername() + " ";
				userIdS += user1.getId();
			}
			instance.setProcCurrNodeUserId(userIdS);
			instance.setProcCurrNodeUserName(usernames);
		}

		procInstanceDao.save(instance);
		ProcTaskInstance pti = new ProcTaskInstance();
		pti.setTaskId(idWorker.nextId()+"");
		pti.setProcessId(instance.getProcessId());
		pti.setHandleTime(new Date());
		pti.setHandleType("2");
		pti.setHandleUserId(userId);
		pti.setHandleUserName(user.getUsername());
		pti.setTaskKey(task.getTaskDefinitionKey());
		pti.setTaskName(task.getName());
		pti.setHandleOpinion("发起申请");
		procTaskInstanceDao.save(pti);
	}

	private List<User> findCurrUsers(Task nextTask,User user) {
		//查询任务的节点数据(候选人组)
		List<IdentityLink> list = taskService.getIdentityLinksForTask(nextTask.getId());
		List<User> users = new ArrayList<>();
		for (IdentityLink identityLink : list) {
			String groupId = identityLink.getGroupId(); //候选人组id
			ProcUserGroup userGroup = procUserGroupDao.findById(groupId).get();//查询userGroup
			String param = userGroup.getParam();
			String paramValue = null;
			if ("user_id".equals(param)) {
				paramValue = user.getId();
			}
			else if ("department_id".equals(param)) {
				paramValue = user.getDepartmentId();
			}
			else if ("company_id".equals(param)) {
				paramValue = user.getCompanyId();
			}
			String sql = userGroup.getIsql().replaceAll("\\$\\{" + param + "\\}", paramValue);
			Query query = entityManager.createNativeQuery(sql);
			query.unwrap(NativeQueryImpl.class).setResultTransformer(Transformers.aliasToBean(User.class));
			users.addAll(query.getResultList());
		}
		return users;
	}

	/**
	 * 提交审核
	 *      handleType; // 处理类型（2审批通过；3审批不通过；4撤销）
	 *
	 *  ProcTaskInstance
	 *       handleOpinion: "xxxxxxc"                   操作说明
			 handleType: "4"                            处理类型（2审批通过；3审批不通过；4撤销）
			 handleUserId: "1063705989926227968"        处理人
			 processId: "1175593305465352192"           业务流程id
	 *
	 */
	public void commit(ProcTaskInstance taskInstance, String companyId) {
		//1.查询业务流程对象
		String processId = taskInstance.getProcessId();
		ProcInstance instance = procInstanceDao.findById(processId).get();
		//2.设置业务流程状态
		instance.setProcessState(taskInstance.getHandleType());
		//3.根据不同的操作类型,完成不同的业务处理
		//查询出activiti中的流程实例 (根据自己的业务id查询activiti中的流程实例)
		ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(processId).singleResult();
		User user = feignClientService.getUserInfoByUserId(taskInstance.getHandleUserId());
		if ("2".equals(taskInstance.getHandleType())) {
			//3.1 如果审核通过,完成当前的任务
			//查询出当前节点,并完成当前节点任务
			Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
			taskService.complete(task.getId());
			//查询出下一个任务节点,如果存在下一个流程没有结束
			Task next = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
			if(next != null) {
				List<User> users = findCurrUsers(next, user);
				String usernames = "", userIdS = "";
				for (User user1 : users) {
					usernames += user1.getUsername() + " ";
					userIdS += user1.getId();
				}
				instance.setProcCurrNodeUserId(userIdS);
				instance.setProcCurrNodeUserName(usernames);
				instance.setProcessState("1");
			}else{
				//如果不存在下一个节点,任务结束
				instance.setProcessState("2");
			}

		} else {
			//3.2 如果审核不通过,或者撤销(删除activiti流程)
			runtimeService.deleteProcessInstance(processInstance.getId(),taskInstance.getHandleOpinion());
		}
		//4.更新业务流程对象,保存业务任务对象
		procInstanceDao.save(instance);
		taskInstance.setTaskId(idWorker.nextId()+"");
		taskInstance.setHandleUserName(user.getUsername());
		taskInstance.setHandleTime(new Date());
		procTaskInstanceDao.save(taskInstance);
	}

	//根据processId查询业务流程明细
	public List<ProcTaskInstance> findTasksByProcess(String id) {
		return procTaskInstanceDao.findByProcessId(id);
	}
}
