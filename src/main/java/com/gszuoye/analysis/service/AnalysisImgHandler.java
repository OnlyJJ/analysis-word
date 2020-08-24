package com.gszuoye.analysis.service;

import java.net.URLEncoder;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gszuoye.analysis.common.utils.Base64Util;
import com.gszuoye.analysis.common.utils.FileUtil;
import com.gszuoye.analysis.common.utils.HttpUtil;
import com.gszuoye.analysis.exception.BusinessException;
import com.gszuoye.analysis.vo.QuesTypeAO;
import com.gszuoye.analysis.vo.result.AnalysisWordResult;


/**
 * 使用百度OCR接口实现
 *
 */
@Service
public class AnalysisImgHandler extends AnalysisWordAbstract {
	
	private Logger LOG = LoggerFactory.getLogger(AnalysisImgHandler.class);
	
	/** 
	 * 百度通用文字识别OCR
	 */
	private static final String BAIDU_ORC_URL = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic";

	@Override
	public AnalysisWordResult parse(String filePath, String fileName, Map<String, QuesTypeAO> quesMap) {
		AnalysisWordResult result = new AnalysisWordResult();
		try {
            byte[] imgData = FileUtil.readFileByBytes(filePath);
            String imgStr = Base64Util.encode(imgData);
            String imgParam = URLEncoder.encode(imgStr, "UTF-8");

            String param = "image=" + imgParam;

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = BaiduAuthService.getAuth();

            String res = HttpUtil.post(BAIDU_ORC_URL, accessToken, param);
            JSONObject words = JSONObject.parseObject(res);
            StringBuilder content = new StringBuilder();
            if(words.containsKey("words_result")) {
            	JSONArray arrays = JSONObject.parseArray(words.getString("words_result"));
            	if(arrays.size() >0) {
            		arrays.stream().forEach(w -> {
            			JSONObject json = (JSONObject) JSONObject.toJSON(w);
            			content.append(json.getString("words"));
            		});
            	}
            }
			result.setDoc(content.toString());
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new BusinessException("该图片格式异常");
		} 
		return result;
	}

}
