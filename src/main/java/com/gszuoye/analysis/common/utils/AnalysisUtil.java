package com.gszuoye.analysis.common.utils;

import java.util.Map;

import com.gszuoye.analysis.vo.QuesTypeAO;
import com.gszuoye.analysis.vo.result.AnalysisWordResult;


/**
 * word解析分发类
 *
 */
public class AnalysisUtil {
	/**
	 * 解析doc
	 * @param word 文档
	 * @param quesMap 题型
	 * @return
	 */
	public static AnalysisWordResult parseDoc(String subjectName, String word, Map<String, QuesTypeAO> quesMap) {
		return DocParseUtil.parse(word, quesMap);
	}
	
	/**
	 * 解析docx
	 * @param word 文档
	 * @param quesMap 题型
	 * @param imgMap 图片索引
	 * @return
	 */
	public static AnalysisWordResult parseDocx(String subjectName, String word, Map<String, QuesTypeAO> quesMap, Map<String , String> imgMap) {
		return DocxParseUtil.parse(subjectName, word, quesMap, imgMap);
	}
	
}
