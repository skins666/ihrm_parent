package com.ihrm.salarys.controller;

import com.ihrm.common.controller.BaseController;
import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.domain.salarys.SalaryArchive;
import com.ihrm.domain.salarys.SalaryArchiveDetail;
import com.ihrm.domain.social_security.Archive;
import com.ihrm.domain.social_security.ArchiveDetail;
import com.ihrm.salarys.service.ArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

//归档controller
@CrossOrigin
@RestController
@RequestMapping(value = "/salarys")
public class ArchiveController extends BaseController {

	@Autowired
	private ArchiveService archiveService;

	/**
	 * 制作薪资报表
	 *  请求URL:/salarys/reports/201907?yearMonth=201907&opType=1
	 *      参数:
	 *          地址参数 : 201907
	 *          请求参数 : opType(1:新制作的报表,其他:查询已归档的报表数据)
	 */
	@RequestMapping(value = "/reports/{yearMonth}",method = RequestMethod.GET)
	public Result historyDetail(@PathVariable String yearMonth,int opType) {
		List<SalaryArchiveDetail> list = new ArrayList<>();
		//1.判断opType参数:如果==1,自己构造报表数据
		if (opType == 1) {
			list = archiveService.getReports(yearMonth,companyId);
		}else{
			//2.如果!=1,查询归档历史表
			//2.1 查询主表数据
			SalaryArchive sa = archiveService.findSalaryArchive(yearMonth,companyId);
			//2.2 根据主表的id,查询明细表的所有数据
			if(sa != null) {
				list = archiveService.findSalaryArchiveDetail(sa.getId());
			}
		}
		return new Result(ResultCode.SUCCESS,list);
	}
}
