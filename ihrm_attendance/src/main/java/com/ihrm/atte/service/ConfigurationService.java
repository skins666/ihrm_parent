package com.ihrm.atte.service;

import com.ihrm.atte.dao.*;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.common.exception.CommonException;
import com.ihrm.common.utils.IdWorker;
import com.ihrm.domain.atte.entity.*;
import com.ihrm.domain.atte.enums.DeductionEnum;
import com.ihrm.domain.atte.enums.LeaveTypeEnum;
import com.ihrm.domain.atte.vo.ConfigVO;
import com.ihrm.domain.atte.vo.ExtDutyVO;
import com.ihrm.domain.atte.vo.ExtWorkVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;


@Service
public class ConfigurationService{

    @Autowired
    private AttendanceConfigDao attendanceConfigDao;

    @Autowired
    private LeaveConfigDao leaveConfigDao;

    @Autowired
    private DeductionDictDao deductionDictDao;

    @Autowired
    private ExtraDutyConfigDao extraDutyConfigDao;

    @Autowired
    private ExtraDutyRuleDao extraDutyRuleDao;

    @Autowired
    private DayOffConfigDao dayOffConfigDao;

    @Autowired
    private IdWorker idWorker;

    //查询考勤设置
	public AttendanceConfig getAtteConfig(String companyId, String departmentId) {
		AttendanceConfig ac = attendanceConfigDao.findByCompanyIdAndDepartmentId(companyId,departmentId);
		return ac == null ?new AttendanceConfig() :ac;
	}

	//保存或者更新考勤设置
	public void saveAtteConfig(AttendanceConfig attendanceConfig) {
		//1.查询是否存在响应的考勤记录
		AttendanceConfig vo = attendanceConfigDao.findByCompanyIdAndDepartmentId(attendanceConfig.getCompanyId(), attendanceConfig.getDepartmentId());
		//2.如果存在,更新
		if(vo != null) {
			attendanceConfig.setId(vo.getId());
		}else {
			//3.如果不存在,设置id,保存
			attendanceConfig.setId(idWorker.nextId() +"");
		}
		attendanceConfigDao.save(attendanceConfig);
	}
}
