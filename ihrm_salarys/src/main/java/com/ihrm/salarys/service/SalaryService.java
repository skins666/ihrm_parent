package com.ihrm.salarys.service;

import com.alibaba.fastjson.JSON;
import com.ihrm.common.entity.PageResult;
import com.ihrm.domain.salarys.UserSalary;
import com.ihrm.salarys.dao.UserSalaryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.*;

@Service
public class SalaryService {
	
    @Autowired
    private UserSalaryDao userSalaryDao;

    //定薪或者调薪
    public void saveUserSalary(UserSalary userSalary) {
        userSalaryDao.save(userSalary);
    }

	//查询用户薪资
    public UserSalary findUserSalary(String userId) {
        Optional<UserSalary> optional = userSalaryDao.findById(userId);
        return optional.isPresent() ? optional.get() : null;
    }

	//分页查询当月薪资列表
	public PageResult findAll(Integer page, Integer pageSize, String companyId) {
		Page page1 = userSalaryDao.findPage(companyId, new PageRequest(page - 1, pageSize));
		return new PageResult(page1.getTotalElements(),page1.getContent());
	}

}
