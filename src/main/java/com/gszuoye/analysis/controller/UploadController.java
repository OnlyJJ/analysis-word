package com.gszuoye.analysis.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gszuoye.analysis.common.constants.Constants;
import com.gszuoye.analysis.common.utils.FileUtil;
import com.gszuoye.analysis.exception.BusinessException;
import com.gszuoye.analysis.vo.result.BaseResult;
import com.gszuoye.analysis.vo.result.FileUploadResult;

/**
 * 测试上传
 * 	axios这里的两种方式都是成功的
 *
 */
@RestController()
public class UploadController {

	@RequestMapping("/file/upload1")
	@ResponseBody
	public BaseResult uploadFile5(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
		FileUploadResult res = new FileUploadResult();
		try {
			String filePath = FileUtil.upload(Constants.FILE_PATH, file);
			String name = file.getOriginalFilename();
			res.setFileName(name);
			res.setFilePath(filePath);
		} catch(BusinessException e) {
			System.err.println(e);
			return BaseResult.error(e.getMessage());
		} catch (Exception e) {
			System.err.println(e);
			return BaseResult.error("文件上传失败，请重试");
		}
		return BaseResult.success("success", res);
	}
	
	@RequestMapping("/file/upload2")
	@ResponseBody
	public BaseResult uploadFile6(MultipartFile file, HttpServletRequest request) {
		FileUploadResult res = new FileUploadResult();
		try {
			String filePath = FileUtil.upload(Constants.FILE_PATH, file);
			String name = file.getOriginalFilename();
			res.setFileName(name);
			res.setFilePath(filePath);
		} catch(BusinessException e) {
			System.err.println(e);
			return BaseResult.error(e.getMessage());
		} catch (Exception e) {
			System.err.println(e);
			return BaseResult.error("文件上传失败，请重试");
		}
		return BaseResult.success("success", res);
	}
}
