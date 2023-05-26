package com.ihrm.salarys.feign;

import com.alibaba.fastjson.JSON;
import com.ihrm.common.entity.Result;
import com.ihrm.domain.atte.entity.ArchiveMonthlyInfo;
import com.ihrm.domain.social_security.ArchiveDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FeignClientService {

	@Autowired
	private AttendanceFeignClient attendanceFeignClient;

	@Autowired
	private SocialSecurityFeignClient socialFeignClient;

	//考勤
	public ArchiveMonthlyInfo getAtteInfo(String userId,String yearMonth) {
		Result result = attendanceFeignClient.atteStatisMonthly(userId, yearMonth);
		ArchiveMonthlyInfo info = null;
		if (result.isSuccess()) {
			info = JSON.parseObject(JSON.toJSONString(result.getData()), ArchiveMonthlyInfo.class);
		}
		return info;
	}

	//社保
	public ArchiveDetail getSocialInfo(String userId, String yearMonth) {
		Result result = socialFeignClient.getSocialInfo(userId, yearMonth);
		ArchiveDetail info = null;
		if (result.isSuccess()) {
			info = JSON.parseObject(JSON.toJSONString(result.getData()), ArchiveDetail.class);
		}
		return info;
	}
}
