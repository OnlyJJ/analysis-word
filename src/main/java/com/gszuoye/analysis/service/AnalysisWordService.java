package com.gszuoye.analysis.service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FilenameUtils;
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
	/**
	 * 题型缓存
	 */
	private Map<String, Map<String, QuesTypeAO>> subCahe = new ConcurrentHashMap<String, Map<String, QuesTypeAO>>();
	
	/**
	 * 解析word
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public AnalysisWordResult parseWord(AnalysisWordParam param) throws Exception {
		String filePath = param.getFilePath();
		if(StringUtils.isEmpty(filePath)) {
			throw new BusinessException("文件路径不能为空"); 
		}
		File file = new File(filePath);
		String extension = FilenameUtils.getExtension(file.getName());
		if(StringUtils.isEmpty(extension)) {
			throw new BusinessException("文件格式错误");
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
		String suffix = extension.toLowerCase();
		if(Constants.IMG_CONT.indexOf((suffix)) != -1) { // 统一图片处理
			suffix = Constants.IMG_PNG;
		}
		String subjectName = param.getSubjectName();
		return AnalysisContext.getAnalysisHandle(suffix).parse(subjectName, filePath, fileName, quesMap);
	}
	
	/**
	 * 获取题型列表
	 * @param subjectId 科目id
	 * @return
	 * @throws Exception
	 */
	public Map<String, QuesTypeAO> getSubjectType(Integer subjectId) throws BusinessException {
		// 考虑到性能，这里原本做了下缓存的
//		String subId = String.valueOf(subjectId);
//		if (subCahe.containsKey(subId)) {
//			return subCahe.get(subId);
//		}
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
