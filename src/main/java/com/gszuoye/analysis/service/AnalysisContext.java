package com.gszuoye.analysis.service;

import com.gszuoye.analysis.factory.AnalysisFactory;

/**
 * word解析方式上下文
 *
 */
public class AnalysisContext {
	
	/**
	 * 获取解析者
	 * @param fileType 文件类型
	 * @return
	 * @throws Exception 
	 */
	public static AnalysisWordAbstract getAnalysisHandle(String fileType) throws Exception {
		return AnalysisFactory.getInstance().getWordAnalysisAbstract(fileType);
	}
}
