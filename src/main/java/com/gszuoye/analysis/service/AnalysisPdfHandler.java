package com.gszuoye.analysis.service;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.fit.pdfdom.PDFDomTree;
import org.springframework.stereotype.Service;

import com.gszuoye.analysis.exception.BusinessException;
import com.gszuoye.analysis.vo.QuesTypeAO;
import com.gszuoye.analysis.vo.result.AnalysisWordResult;

@Service
public class AnalysisPdfHandler extends AnalysisWordAbstract {

	@Override
	public AnalysisWordResult parse(String filePath, String fileName, Map<String, QuesTypeAO> quesMap) {
		AnalysisWordResult result = new AnalysisWordResult();
		Writer output = null;
		ByteArrayOutputStream baos = null;
		OutputStream outStream = null;
		try {
			baos = new ByteArrayOutputStream();
	        outStream = new BufferedOutputStream(baos);
			PDDocument pdf = PDDocument.load(new File(filePath));
//			output = new PrintWriter("F:\\pdf.html", "utf-8"); // 输出html
			output = new PrintWriter(outStream, true);
			PDFDomTree tree = new PDFDomTree();
			tree.writeText(pdf, output);
			result.setDoc(baos.toString());
		} catch (Exception e) {
			throw new BusinessException("解析异常");
		} finally {
			try {
				output.close();
			} catch (IOException e) {
//				e.printStackTrace();
			}
			if(baos != null) {
				try {
					baos.close();
				} catch (IOException e) {
//					e.printStackTrace();
				}
			}
			if(outStream != null) {
				try {
					outStream.close();
				} catch (IOException e) {
//					e.printStackTrace();
				}
			}
		}
		return result;
	}

}
