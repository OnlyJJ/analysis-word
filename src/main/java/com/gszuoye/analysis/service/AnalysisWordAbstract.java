package com.gszuoye.analysis.service;

import java.util.Map;

import com.gszuoye.analysis.vo.QuesTypeAO;
import com.gszuoye.analysis.vo.result.AnalysisWordResult;

public abstract class AnalysisWordAbstract {
	
	/**
	 * 解析文档
	 * @param filePath 文件路径
	 * @return
	 * @throws Exception
	 */
	public abstract AnalysisWordResult parse(String filePath, String fileName, Map<String, QuesTypeAO> quesMap);
}
