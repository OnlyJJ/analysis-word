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
	 */
	public static AnalysisWordAbstract getAnalysisHandle(String fileType) {
		try {
			return AnalysisFactory.getInstance().getWordAnalysisAbstract(fileType);
		} catch (Exception e) {
//			e.printStackTrace();
		}
		return null;
	}
}
