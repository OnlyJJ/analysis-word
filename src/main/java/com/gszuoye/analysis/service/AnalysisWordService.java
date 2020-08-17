package com.gszuoye.analysis.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gszuoye.analysis.common.constants.Constants;
import com.gszuoye.analysis.common.utils.HttpUtil;
import com.gszuoye.analysis.common.utils.StringUtil;
import com.gszuoye.analysis.exception.BusinessException;
import com.gszuoye.analysis.vo.QuesTypeAO;
import com.gszuoye.analysis.vo.param.AnalysisWordParam;
import com.gszuoye.analysis.vo.result.AnalysisWordResult;

import fr.opensagres.xdocreport.core.utils.StringUtils;

@Service
public class AnalysisWordService {
	
	private Map<String, Map<String, QuesTypeAO>> subMap = new ConcurrentHashMap<String, Map<String, QuesTypeAO>>();
	
	/**
	 * 解析word
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public AnalysisWordResult parseWord(AnalysisWordParam param) throws BusinessException {
		String filePath = param.getFilePath();
		if(StringUtils.isEmpty(filePath)) {
			throw new BusinessException("文件路径不能为空"); 
		}
		String[] files = filePath.split("\\.");
		if(files.length < 2) {
			throw new BusinessException("文件格式不正确");
		}
		Integer subjectId = param.getSubjectId();
		if(subjectId == null) {
			throw new BusinessException("科目id不能为空");
		}
		String fileName = param.getFileName();
		if(StringUtils.isEmpty(fileName)) {
			fileName = StringUtil.generaterId(); // 文件名为空，则随机生成id作为名称
		}
		// 题型列表
		Map<String, QuesTypeAO> quesMap = getSubjectType(subjectId);
		String suffix = files[1].toLowerCase();
		if(Constants.IMG_CONT.indexOf((suffix)) != -1) { // 统一图片处理
			suffix = Constants.IMG_PNG;
		}
		return AnalysisContext.getAnalysisHandle(suffix).parse(filePath, fileName, quesMap);
	}
	
	/**
	 * 获取题型列表
	 * @param subjectId 科目id
	 * @return
	 * @throws Exception
	 */
	public Map<String, QuesTypeAO> getSubjectType(Integer subjectId) throws BusinessException {
		String subId = String.valueOf(subjectId);
		if (subMap.containsKey(subId)) {
			return subMap.get(subId);
		}
		JSONObject param = new JSONObject();
		param.put(Constants.SUBJECT_ID_KEY, subjectId);
		JSONObject result = HttpUtil.post(Constants.SUBJECT_TYPE_URL, param.toJSONString());
		if (!result.getInteger(Constants.STATUS_KEY).equals(Constants.SUCCESS_CODE)) {
			throw new BusinessException(result.getString(Constants.MSG_KEY));
		}
		Map<String, QuesTypeAO> resMap = new HashMap<String, QuesTypeAO>();
		JSONObject dataInfo = result.getJSONObject(Constants.DATAINFO_KEY);
		JSONArray arrays = dataInfo.getJSONArray(Constants.DATA_KEY);
		if (arrays.size() > 0) {
			arrays.stream().forEach(q -> {
				QuesTypeAO ao = JSONObject.parseObject(JSONObject.toJSONString(q), QuesTypeAO.class);
				resMap.put(ao.getName(), ao);
			});
		}
		return resMap;
	}
	
}
