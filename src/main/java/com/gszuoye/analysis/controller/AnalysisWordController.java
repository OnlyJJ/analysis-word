package com.gszuoye.analysis.controller;


import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.gszuoye.analysis.common.constants.Constants;
import com.gszuoye.analysis.common.utils.FileUtil;
import com.gszuoye.analysis.exception.BusinessException;
import com.gszuoye.analysis.service.AnalysisWordService;
import com.gszuoye.analysis.vo.param.AnalysisWordParam;
import com.gszuoye.analysis.vo.result.AnalysisWordResult;
import com.gszuoye.analysis.vo.result.BaseResult;
import com.gszuoye.analysis.vo.result.FileUploadResult;

@Controller
@RequestMapping("/analysis/word")
public class AnalysisWordController {
	private Logger LOG = LoggerFactory.getLogger(AnalysisWordService.class);
	
	@Autowired
	AnalysisWordService analysisWordService;
	

	@PostMapping("/uploadFile")
	@ResponseBody
	public BaseResult uploadFile(MultipartFile file, HttpServletRequest request) {
		LOG.info("开始上传。。。。");
		FileUploadResult res = new FileUploadResult();
		try {
			String filePath = FileUtil.upload(Constants.FILE_PATH, file);
			String name = file.getOriginalFilename();
			res.setFileName(name);
			res.setFilePath(filePath);
		} catch(BusinessException e) {
			LOG.error(e.getMessage(), e);
			return BaseResult.error(e.getMessage());
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return BaseResult.error("文件上传失败，请重试");
		}
		LOG.info("上传文件成功！");
		return BaseResult.success("success", res);
	}
	
	
	@PostMapping("/parseWord")
	@ResponseBody
	public BaseResult parseWord(@RequestBody AnalysisWordParam param) {
		LOG.info("解析word开始。。。。。");
		long time1 = System.currentTimeMillis();
		AnalysisWordResult res = null;
		try {
			res = analysisWordService.parseWord(param);
		} catch(BusinessException e) {
			LOG.error(e.getMessage(), e);
			return BaseResult.error(e.getMessage());
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return BaseResult.error("解析word失败，请重试");
		}
		long time2 = System.currentTimeMillis();
		LOG.info("解析耗时：" +  (time2 - time1));
		return BaseResult.success("success", JSONObject.toJSON(res));
	}
}
