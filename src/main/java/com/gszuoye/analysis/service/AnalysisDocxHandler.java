package com.gszuoye.analysis.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.gszuoye.analysis.common.utils.AnalysisUtil;
import com.gszuoye.analysis.exception.BusinessException;
import com.gszuoye.analysis.vo.QuesTypeAO;
import com.gszuoye.analysis.vo.result.AnalysisWordResult;

import fr.opensagres.poi.xwpf.converter.xhtml.Base64EmbedImgManager;
import fr.opensagres.poi.xwpf.converter.xhtml.XHTMLConverter;
import fr.opensagres.poi.xwpf.converter.xhtml.XHTMLOptions;
import fr.opensagres.xdocreport.core.io.internal.ByteArrayOutputStream;

@Service
public class AnalysisDocxHandler extends AnalysisWordAbstract {

	private Logger LOG = LoggerFactory.getLogger(AnalysisDocxHandler.class);
			
	@Override
	public AnalysisWordResult parse(String filePath, String fileName, Map<String, QuesTypeAO> quesMap) {
		AnalysisWordResult result = null;
		ByteArrayOutputStream baos = null;
		InputStream in = null;
		try {
			in = new FileInputStream(filePath);
			XWPFDocument docxDocument = new XWPFDocument(in);
			XHTMLOptions options = XHTMLOptions.create();
			options.setImageManager(new Base64EmbedImgManager());
			baos = new ByteArrayOutputStream();
			XHTMLConverter.getInstance().convert(docxDocument, baos, options);
			String content = baos.toString();
			result = AnalysisUtil.parseDocx(content, quesMap, true);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new BusinessException("解析文件出错"); 
		}finally {
			if (baos != null) {
				try {
					baos.close();
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LOG.warn(e.getMessage(),e);
				}
			}
		}
		return result;
	}

}
