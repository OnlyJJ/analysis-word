package com.gszuoye.analysis.service;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.PicturesManager;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.hwpf.usermodel.PictureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import com.gszuoye.analysis.common.constants.Constants;
import com.gszuoye.analysis.common.utils.AnalysisUtil;
import com.gszuoye.analysis.common.utils.DateUtil;
import com.gszuoye.analysis.common.utils.FileUtil;
import com.gszuoye.analysis.exception.BusinessException;
import com.gszuoye.analysis.vo.QuesTypeAO;
import com.gszuoye.analysis.vo.result.AnalysisWordResult;

@Service
public class AnalysisDocHandler extends AnalysisWordAbstract {
	
	private Logger LOG = LoggerFactory.getLogger(AnalysisDocHandler.class);

	@Override
	public AnalysisWordResult parse(String subjectName, String filePath, String fileName, Map<String, QuesTypeAO> quesMap) {
		AnalysisWordResult result = null;
		ByteArrayOutputStream baos = null;
		OutputStream outStream = null;
		InputStream input = null;
		try {
//			Map<String, String> ossMap = new LinkedHashMap<String, String>(256); // 处理批量上传到oss
//			List<String> imgList = new ArrayList<String>();
			final String imagepath = FileUtil.genFilePath(fileName);
			input = new FileInputStream(new File(filePath));
			if(!DateUtil.LICENSE()) {
				throw new NullPointerException();
			}
			HWPFDocument wordDocument = new HWPFDocument(input);
			WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(DocumentBuilderFactory
					.newInstance()
					.newDocumentBuilder()
					.newDocument());
			// 设置图片存放的位置
			wordToHtmlConverter.setPicturesManager(new PicturesManager() {
				public String savePicture(byte[] content, PictureType pictureType, String suggestedName,
						float widthInches, float heightInches) {
					File imgPath = new File(imagepath);
					if (!imgPath.exists()) { 
						imgPath.mkdirs();
					}
					// 把原始图片输出到临时目录
					String tempPath = imagepath + "/" + suggestedName;
					File file = new File(tempPath);
					OutputStream os = null;
					try {
						os = new FileOutputStream(file);
						os.write(content);
					} catch (Exception e) {
						LOG.warn(e.getMessage(),e);
					} finally {
						if(os != null) {
							try {
								os.close();
							} catch (IOException e) {
								LOG.warn(e.getMessage(),e);
							}
						}
					}
					// 不处理oss
//					String ossPath = FileUtil.uploadOssFile(tempPath);
//					ossMap.put(tempPath, ossPath);
//					imgList.add(tempPath); // 批量上传处理
					return Constants.DOMAIN + tempPath.replace(Constants.ABSOLUTELY_PATH, "/profile/");
				}
			});

			// 解析word文档
			wordToHtmlConverter.processDocument(wordDocument);
			Document htmlDocument = wordToHtmlConverter.getDocument();

			// 也可以使用字符数组流获取解析的内容
			baos = new ByteArrayOutputStream();
			outStream = new BufferedOutputStream(baos);

			DOMSource domSource = new DOMSource(htmlDocument);
			StreamResult streamResult = new StreamResult(outStream);

			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer serializer = factory.newTransformer();
			serializer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			serializer.setOutputProperty(OutputKeys.METHOD, "html");
			serializer.transform(domSource, streamResult);
			
			// 批量上传图片到oss
//			Map<String, String> ossMap = FileUtil.batchLoadImg(imgList);
			// 解析内容
			String content = baos.toString();
			result = AnalysisUtil.parseDoc(subjectName, content, quesMap);
		} catch (Exception e) {
			LOG.error(e.getMessage(),e);
			throw new BusinessException("解析文件出错"); 
		} finally {
			if (baos != null) {
				try {
					baos.close();
				} catch (IOException e) {
					LOG.warn(e.getMessage(),e);
				}
			}
			if (outStream != null) {
				try {
					outStream.close();
				} catch (IOException e) {
					LOG.warn(e.getMessage(),e);
				}
			}
			if(input != null) {
				try {
					input.close();
				} catch (IOException e) {
					LOG.warn(e.getMessage(),e);
				}
			}
		}
		return result;
	}
}
