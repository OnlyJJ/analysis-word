package com.gszuoye.analysis.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gszuoye.analysis.factory.AnalysisFactory;

/**
 * word解析方式上下文
 *
 */
public class AnalysisContext {
	private static Logger LOG = LoggerFactory.getLogger(AnalysisContext.class);
	
	/**
	 * 获取解析者
	 * @param fileType 文件类型
	 * @return
	 */
	public static AnalysisWordAbstract getAnalysisHandle(String fileType) {
		try {
			return AnalysisFactory.getInstance().getWordAnalysisAbstract(fileType);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return null;
	}
}
