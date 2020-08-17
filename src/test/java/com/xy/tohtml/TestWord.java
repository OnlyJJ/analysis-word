package com.xy.tohtml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.poi.xwpf.converter.core.FileImageExtractor;
import org.apache.poi.xwpf.converter.core.FileURIResolver;
import org.apache.poi.xwpf.converter.xhtml.XHTMLConverter;
import org.apache.poi.xwpf.converter.xhtml.XHTMLOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import com.gszuoye.analysis.common.utils.AnalysisUtil;
import com.gszuoye.analysis.common.utils.FileUtil;
import com.gszuoye.analysis.exception.BusinessException;
import com.gszuoye.analysis.vo.QuesTypeAO;
import com.gszuoye.analysis.vo.result.AnalysisWordResult;

public class TestWord {
	
	private AnalysisWordResult convertDocx(String filePath, String fileName, Map<String, QuesTypeAO> quesMap) throws BusinessException {
		AnalysisWordResult result = new AnalysisWordResult();
		ByteArrayOutputStream baos = null;
		OutputStream out = null;
        String htmlName = "nb.html";
        InputStream in = null;
        try {
        	Map<String, String> ossMap = new LinkedHashMap<String, String>(256);
            // 1) 加载word文档生成 XWPFDocument对象
            in = new FileInputStream(new File(filePath));
            XWPFDocument document = new XWPFDocument(in);

            // 2) 解析 XHTML配置 (这里设置IURIResolver来设置图片存放的目录)
            String imagepath = FileUtil.genFilePath(fileName);
            File imageFolderFile = new File(imagepath);
            XHTMLOptions options = XHTMLOptions.create().URIResolver(new FileURIResolver(imageFolderFile));
            options.setExtractor(new FileImageExtractor(imageFolderFile));
//            options.setExtractor(new IImageExtractor() {
//				@Override
//				public void extract(String imagePath, byte[] imageData) throws IOException {
//					// TODO Auto-generated method stub
//					
//				}
//            	
//            });
            options.setIgnoreStylesIfUnused(false);
            options.setFragment(true);

            // 3) 将 XWPFDocument转换成XHTML
            out = new FileOutputStream(new File(filePath));
            XHTMLConverter.getInstance().convert(document, out, options);
            
            // 处理图片
//            List<XWPFPictureData> picList = document.getAllPictures();
//            for (XWPFPictureData pic : picList) {
//                byte[] bytev = pic.getData();
//                // 大于1000bites的图片我们才弄下来，消除word中莫名的小图片的影响
//                if (bytev.length > 300) {
//                    FileOutputStream fos = new FileOutputStream(imagepath + pic.getFileName());
//                    fos.write(bytev);
//                }
//            }

            //也可以使用字符数组流获取解析的内容
            baos = new ByteArrayOutputStream();
            XHTMLConverter.getInstance().convert(document, baos, options);
            String content = baos.toString();
            System.out.println(content);
            result = AnalysisUtil.parseDoc(content, quesMap, ossMap);
        } catch(Exception e) {
        	throw new BusinessException("解析文件出错"); 
        } finally {
			if (baos != null) {
				try {
					baos.close();
				} catch (IOException e) {
//					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
//					e.printStackTrace();
				}
			}
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
//					e.printStackTrace();
				}
			}
		}
		return result;
	}
}
