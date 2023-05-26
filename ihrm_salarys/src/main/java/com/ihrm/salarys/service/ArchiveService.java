package com.ihrm.salarys.service;

import com.ihrm.common.utils.IdWorker;
import com.ihrm.domain.atte.entity.ArchiveMonthlyInfo;
import com.ihrm.domain.salarys.SalaryArchive;
import com.ihrm.domain.salarys.SalaryArchiveDetail;
import com.ihrm.domain.salarys.Settings;
import com.ihrm.domain.salarys.UserSalary;
import com.ihrm.domain.social_security.ArchiveDetail;
import com.ihrm.salarys.dao.ArchiveDao;
import com.ihrm.salarys.dao.ArchiveDetailDao;
import com.ihrm.salarys.dao.SettingsDao;
import com.ihrm.salarys.dao.UserSalaryDao;
import com.ihrm.salarys.feign.FeignClientService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

//归档service
@Service
public class ArchiveService {

    @Autowired
    private ArchiveDao archiveDao; //归档主表dao
    @Autowired
    private ArchiveDetailDao archiveDetailDao; //归档明细表dao
    @Autowired
    private UserSalaryDao userSalaryDao; //员工薪资设置dao
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private SalaryService salaryService; //员工薪资service
    @Autowired
    private SettingsService settingsService ; //福利津贴service
	@Autowired
	private FeignClientService feignClientService;

	//根据企业和年月查询归档主表数据
	public SalaryArchive findSalaryArchive(String yearMonth, String companyId) {
		return archiveDao.findByCompanyIdAndYearsMonth(companyId,yearMonth);
	}

	//根据归档的id查询所有的归档明细记录
	public List<SalaryArchiveDetail> findSalaryArchiveDetail(String id) {
		return archiveDetailDao.findByArchiveId(id);
	}

	//查询月报表数据

	/**
	 * 计算薪资 : 社保和考勤已经归档
	 * @return
	 */
	public List<SalaryArchiveDetail> getReports(String yearMonth, String companyId) {
		List<SalaryArchiveDetail> list = new ArrayList<>();

		//查询当前企业的福利津贴
		Settings settings = settingsService.findById(companyId);
		//1.查询所有的用户
		Page<Map> page = userSalaryDao.findPage(companyId, null);
		//2.遍历用户数据
		for (Map map : page.getContent()) {
			//3.构造SalaryArchiveDetail
			SalaryArchiveDetail detail = new SalaryArchiveDetail();
			detail.setUser(map); //构造用户数据
			//4.获取每个用户的社保数据
			ArchiveDetail socialInfo = feignClientService.getSocialInfo(detail.getUserId(), yearMonth);
			detail.setSocialInfo(socialInfo);
			//5.获取每个用户的考勤数据
			ArchiveMonthlyInfo atteInfo = feignClientService.getAtteInfo(detail.getUserId(), yearMonth);
			detail.setAtteInfo(atteInfo);
			//6.获取每个用户的薪资
			UserSalary userSalary = salaryService.findUserSalary(detail.getUserId());
			detail.setUserSalary(userSalary);
			//7.计算工资
			detail.calSalary(settings);
			list.add(detail);
		}
		return list;
	}
}
