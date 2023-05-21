package cn.itcast;

import com.ihrm.common.poi.ExcelImportUtil;
import com.ihrm.domain.atte.vo.AtteUploadVo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

public class PoiTest {

	public static void main(String[] args) throws FileNotFoundException {
		ExcelImportUtil<AtteUploadVo> util = new ExcelImportUtil<>(AtteUploadVo.class);
		FileInputStream stream = new FileInputStream(new File("E:\\demo.xlsx"));
		List<AtteUploadVo> list = util.readExcel(stream, 1, 0);
		for (AtteUploadVo atteUploadVo : list) {
			System.out.println(atteUploadVo);
		}
	}
}
