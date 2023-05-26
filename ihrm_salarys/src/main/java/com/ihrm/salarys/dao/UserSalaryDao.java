package com.ihrm.salarys.dao;

import com.ihrm.domain.salarys.UserSalary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Map;

/**
 * 自定义dao接口继承
 * JpaRepository<实体类，主键>
 * JpaSpecificationExecutor<实体类>
 */
public interface UserSalaryDao extends JpaRepository<UserSalary, String>, JpaSpecificationExecutor<UserSalary> {

	/**
	 * springdatajpa, 使用本地sql分页查询
	 * @query注解
	 *      nativeQuery : true标识本地sql
	 *      value :查询的sql语句
	 *      countQuery : 查询总记录数的sql语句
	 * 参数:
	 *      PageRequest : 配置分页的基本数据(页码,每页查询条数)
	 * 返回值:
	 *      构造实体类返回值
	 *          不构造实体类,
	 *       返回map
	 *          key = 查询的字段名   value :值
	 */
	@Query(nativeQuery = true,
			value="select bu.id,bu.username,bu.mobile,bu.work_number workNumber," +
					"bu.in_service_status inServiceStatus,bu.department_name departmentName,bu.department_id departmentId,bu.time_of_entry timeOfEntry ," +
					"bu.form_of_employment formOfEmployment ,sauss.current_basic_salary currentBasicSalary,sauss.current_post_wage currentPostWage from bs_user bu LEFT JOIN sa_user_salary sauss ON bu.id=sauss.user_id WHERE bu.company_id = ?1",
			countQuery = "select count(*) from bs_user bu LEFT JOIN sa_user_salary sauss ON bu.id=sauss.user_id WHERE bu.company_id = ?1"
	)
	Page<Map> findPage(String companyId, PageRequest pageRequest);
}
