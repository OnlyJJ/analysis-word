package com.gszuoye.analysis.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.gszuoye.analysis.common.utils.ApplicationContextHelper;
import com.gszuoye.analysis.exception.BusinessException;
import com.gszuoye.analysis.service.AnalysisDocHandler;
import com.gszuoye.analysis.service.AnalysisDocxHandler;
import com.gszuoye.analysis.service.AnalysisImgHandler;
import com.gszuoye.analysis.service.AnalysisPdfHandler;
import com.gszuoye.analysis.service.AnalysisWordAbstract;

/**
 * 解析工厂类 
 *
 */
public class AnalysisFactory {
	
	private Map<String, Class<? extends AnalysisWordAbstract>> strategyMap = new ConcurrentHashMap<>();
	
	private AnalysisFactory() {
		strategyMap.put("doc", AnalysisDocHandler.class);
		strategyMap.put("docx", AnalysisDocxHandler.class);
		strategyMap.put("pdf", AnalysisPdfHandler.class);
		strategyMap.put("png", AnalysisImgHandler.class);
	}
	
	private static class InnerFactory {
		private final static AnalysisFactory msgFactory = new AnalysisFactory();
	}
	
	public static AnalysisFactory getInstance() {
		return AnalysisFactory.InnerFactory.msgFactory;
	}
	
	public AnalysisWordAbstract getWordAnalysisAbstract(String fileType) throws Exception {
		Class<? extends AnalysisWordAbstract> clazz = strategyMap.get(fileType);
		if(clazz == null) {
			throw new BusinessException("文件格式不支持");
		}
		return ApplicationContextHelper.getBean(clazz);
	}
}
