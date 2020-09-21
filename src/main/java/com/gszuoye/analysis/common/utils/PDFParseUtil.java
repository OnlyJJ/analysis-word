package com.gszuoye.analysis.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.gszuoye.analysis.vo.AnalysisWordAO;
import com.gszuoye.analysis.vo.result.AnalysisWordResult;

import fr.opensagres.xdocreport.core.utils.StringUtils;

public class PDFParseUtil {
	/**
	 * 样式
	 */
	private static final String STYLE = "style";
	/**
	 * 内容封装在div里
	 */
	private static final String BODY = "body>*";
	/**
	 * p标签所有子元素
	 */
	private static final String DIV = "div>*";
	/**
	 * 前端用到的标签
	 */
	private static final String TABLE = "table";
	private static final String IMG = "img";
	private static final String SRC = "src";
	private static final String IMG_SRC = "img[src]";
	private static final String BOLD = "bold";
	private static final String STRONG_1 = "<strong>";
	private static final String STRONG_2 = "</strong>";
	private static final String ITALIC = "italic";
	private static final String EM_1 = "<em>";
	private static final String EM_2 = "</em>";
	private static final String	SUB = "sub";
	private static final String	SUB_1 = "<sub>";
	private static final String	SUB_2 = "</sub>";
	private static final String	SUP = "sup";
	private static final String	SUP_1 = "<sup>";
	private static final String	SUP_2 = "</sup>";
	private static final String	SUPER = "super";
	private static final String UNDERLINE = "underline";
	private static final String U_1 = "<u>";
	private static final String U_2 = "</u>";
	private static final String TAB = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	
	/**
	 * 选择题选项正则1，形如：A.
	 */
	private static String OPTION_REG_1 = "^[A-Z]{1}[.、．:：]+";
	
	/**
	 * 默认文件名，用于创建文件时hash，保证正确
	 */
	private static final String DEFAULT_FINENAME = "试卷解析图片";	
	
	/**
	 * 解析doc正文
	 * @param word
	 * @param quesMap
	 * @return
	 */
	public static AnalysisWordResult parse(String word) {
		AnalysisWordResult result = new AnalysisWordResult();
		if (StringUtils.isEmpty(word)) {
			return result;
		}
		Document doc = Jsoup.parse(word);
		String title = "";
		result.setTitle(title);

		String style = doc.select(STYLE).first().html();
		if(style.indexOf("src:url") == -1) {
			result.setStyle(style);
		}

		// 所有直接子元素
		Elements contents = null;
		contents = doc.select(BODY);
		List<AnalysisWordAO> content = new ArrayList<AnalysisWordAO>(256);
		boolean isOption = false;
		final String imagepath = FileUtil.genFilePath(title.equals("") ? DEFAULT_FINENAME : title);
		for (Element e : contents) {
			AnalysisWordAO ao = new AnalysisWordAO();
			// 处理图片路径
			Elements imgs = e.select(IMG_SRC);
			if(imgs != null && imgs.size() >0) {
				for(Element img : imgs) {
					String src = img.attr(SRC);
					img.attr(SRC, FileUtil.base64ConvertFile(src, imagepath));
				}
			}
			String text = e.text();
			ao.setText(text);
			// 处理content
			if(e.tagName().equalsIgnoreCase(TABLE)) { // 如果是表格，则直接引用，不处理格式
				ao.setContent(e.toString());
			} else {
				Elements ps = e.select(DIV);
				StringBuilder htmlContent = new StringBuilder();
				for(Element p : ps) {  
					if(isOption) { // 选择题处理空格，其他不处理
						if(p.text().length() >=2 && Pattern.matches(OPTION_REG_1, p.text().substring(0, 2))) {
							htmlContent.append(TAB);
						}
					}
					htmlContent.append(convertText(p));
				}
				ao.setContent(htmlContent.toString());
			}
			ao.setAnser(false);
			content.add(ao);
		}
		result.setContent(content);
		return result;
	}
	
	/**
	 * 转换内容
	 * @param p
	 * @return
	 */
	private static String convertText(Element p) {
		StringBuilder result = new StringBuilder();
		if(p.tagName().equalsIgnoreCase(IMG)) { // 图片不处理
			return result.append(p.toString()).toString();
		}
		String css = p.attr(STYLE);
		if(StringUtils.isNotEmpty(css)) {
			StringBuilder per = new StringBuilder();
			StringBuilder suf = new StringBuilder();
			// 加粗
			if(css.indexOf(BOLD) != -1) { 
				per.append(STRONG_1);
				suf.append(STRONG_2);
			}
			// 斜体
			if(css.indexOf(ITALIC) != -1) {
				per.append(EM_1);
				suf.append(EM_2);
			}
			// 上标
			if(css.indexOf(SUB) != -1) {
				per.append(SUB_1);
				suf.append(SUB_2);
			}
			// 下标
			if(css.indexOf(SUPER) != -1 || css.indexOf(SUP) != -1) {
				per.append(SUP_1);
				suf.append(SUP_2);
			}
			// 下滑线
			if(css.indexOf(UNDERLINE) != -1) {
				per.append(U_1);
				suf.append(U_2);
			}
			result.append(per.toString()).append(p.text()).append(suf.toString());
		} else {
			result.append(p.text());
		}
		return result.toString();
	}
}
