package com.gszuoye.analysis.common.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RandomUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.util.CollectionUtils;

import com.gszuoye.analysis.vo.AnalysisWordAO;
import com.gszuoye.analysis.vo.OptionAO;
import com.gszuoye.analysis.vo.QuesTypeAO;
import com.gszuoye.analysis.vo.SubjectAO;
import com.gszuoye.analysis.vo.result.AnalysisWordResult;

import fr.opensagres.xdocreport.core.utils.StringUtils;

/**
 * word解析处理工具
 *
 */
public class AnalysisUtil {
	/**
	 * 标题
	 */
	private static final String TITLE = "title";
	/**
	 * 样式
	 */
	private static final String STYLE = "style";
	/**
	 * doc内容封装在body里
	 */
	private static final String BODY = "body>*";
	/**
	 * docx内容封装在div里
	 */
	private static final String DIV = "div>*";
	/**
	 * p标签所有子元素
	 */
	private static final String P = "p>*";
	/**
	 * 前端用到的标签
	 */
	private static final String TABLE = "table";
	private static final String IMG = "img";
	private static final String SRC = "src";
	private static final String IMG_SRC = "img[src]";
	private static final String CLASS = "class";
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
	private static final String	SUP_2 = "<sup>";
	private static final String	SUPER = "super";
	private static final String UNDERLINE = "underline";
	private static final String U_1 = "<u>";
	private static final String U_2 = "</u>";
	private static final String TAB = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	
	/**
	 * 约定的起点节点标示
	 */
	private static String TITLE_INDEX = "一二三四五六七八九十";
	/**
	 * 小标题正则
	 */
	private static String SUB_INDEX_REG = "^([1-9][0-9]*)[.、．(（（]+?";
	/**
	 * 选择题选项正则1，形如：A.
	 */
	private static String OPTION_REG_1 = "^[A-Z]{1}[.、．]+?";
	/**
	 * 选择题选项正则2，形如：（1）A
	 */
	private static String OPTION_REG_2 = "^[(（（]{1}[1-9]*[)））]{1}[A-Z]{1}";
	
	
	/**
	 * 解析doc
	 * @param word
	 * @param quesMap
	 * @param ossMap
	 * @return
	 */
	public static AnalysisWordResult parseDoc(String word, Map<String, QuesTypeAO> quesMap, Map<String, String> ossMap) {
		return parse(word, quesMap, ossMap, false, false);
	}
	
	/**
	 * 解析docx
	 * @param word
	 * @param quesMap
	 * @param uploadOss
	 * @return
	 */
	public static AnalysisWordResult parseDocx(String word, Map<String, QuesTypeAO> quesMap, boolean uploadOss) {
		return parse(word, quesMap, null, true, true);
	}
	

	/**
	 * 解析doc正文
	 * @param word
	 * @param quesMap
	 * @return
	 */
	private static AnalysisWordResult parse(String word, Map<String, QuesTypeAO> quesMap, Map<String, String> ossMap, boolean uploadOss, boolean isDocx) {
		AnalysisWordResult result = new AnalysisWordResult();
		if (StringUtils.isEmpty(word)) {
			return result;
		}
		Document doc = Jsoup.parse(word);
		// title
		String title = "";
		if(!isDocx) {
			Elements titles = doc.getElementsByTag(TITLE);
			if(!CollectionUtils.isEmpty(titles)) {
				title =  titles.first().html();
			}
		}
		result.setTitle(title);

		String style = doc.select(STYLE).first().html();
		result.setStyle(style);
		// 样式提取
		Map<String, String> docCssMap = null;

		// 所有直接子元素
		Elements contents = null;
		if(isDocx) {
			contents = doc.select(DIV);
		} else {
			contents = doc.select(BODY);
			docCssMap =convertDocCss(style);
		}
		List<AnalysisWordAO> content = new ArrayList<AnalysisWordAO>(256);
		List<SubjectAO> subjects = new ArrayList<SubjectAO>();
		Map<String, Integer> subjectMap = new LinkedHashMap<String, Integer>();
		Map<String, Integer> questionTypeIdMap = new HashMap<String, Integer>();
		Map<String, Integer> questionNameIdMap = new HashMap<String, Integer>();
		QuesTypeAO ques = null;
		String itemId = "";
		int subjectNum = 0; // 题型下总共的题目数量
		boolean isSubTitle = false;
		for (Element e : contents) {
			AnalysisWordAO ao = new AnalysisWordAO();
			// 更换临时图片url到oss地址
			Elements imgs = e.select(IMG_SRC);
			if(imgs != null && imgs.size() >0) {
				for(Element img : imgs) {
					String src = img.attr(SRC);
					// docx需要处理上传到oss
					if(uploadOss) {
						// TOTO
						// 默认生成的头部信息不对，这里要替换以下
						String ossPath = FileUtil.uploadOssBase64(src);
						img.attr(SRC, ossPath);
					} else {
						if(ossMap.containsKey(src)) {
							img.attr(SRC, ossMap.get(src));
						}
					}
				}
			}
			// 处理content
			// 如果是表格
			if(e.tagName().equalsIgnoreCase(TABLE)) {
				ao.setContent(e.toString());
			} else {
				Elements ps = e.select(P);
				StringBuilder htmlContent = new StringBuilder();
				for(Element p : ps) {
					// 处理空格
					List<TextNode> texts = p.textNodes();
					if(!CollectionUtils.isEmpty(texts)) {
						for(TextNode node : texts) {
							if(node.text().contains(" ") || node.text().contains("	")) {
								htmlContent.append(TAB);
								break;
							} 
						}
					}
					if(isDocx) {
						htmlContent.append(convertDocxText(p));
					} else {
						htmlContent.append(convertDocText(p, docCssMap));
					}
				}
				ao.setContent(htmlContent.toString());
			}
			// text的起始为一，二，三。。。。则默认为大标题，
			String text = e.text();
			if (StringUtils.isNotEmpty(text)) {
				int length = text.length();
				if (length > 3) {
					String subName = text.substring(0, 1); // 大题
					if(TITLE_INDEX.contains(subName)) { // 题型，作为归类，统计其类型下总共的题数
						isSubTitle = true;
						ques = getSubjectName(text, quesMap);
						itemId = StringUtil.generaterId();
						subjectNum = 0;
					} else {
						isSubTitle = false;
						String index = text.substring(0, 3); // 小题
						if (Pattern.matches(SUB_INDEX_REG, index)) { // 题目，使用同一个itemId
							itemId = StringUtil.generaterId();
							subjectNum += 1;
							if(ques != null) {
								subjectMap.put(ques.getName(), subjectNum);
								questionTypeIdMap.put(ques.getName(), ques.getQuestionTypeId());
								questionNameIdMap.put(ques.getName(), ques.getId());
							}
						}
					}
					// 把text的ABCD。。。选项，拆分成数组
					ao.setOptions(convertOptions(text, e));
				}
			}
			ao.setText(text);
			ao.setAnser(false);
			if(!isSubTitle && ques != null) {
				ao.setQuestionTypeId(null == ques.getQuestionTypeId() ? 0 : ques.getQuestionTypeId());
				ao.setItemId(itemId);
			}
			content.add(ao);
		}
		if(subjectMap.size() > 0) {
			for(String name : subjectMap.keySet()) {
				SubjectAO sub = new SubjectAO();
				sub.setSubjectTitle(name);
				sub.setCount(subjectMap.get(name));
				sub.setQuestionTypeId(questionTypeIdMap.get(name));
				sub.setId(questionNameIdMap.get(name));
				subjects.add(sub);
			}
		}
		result.setContent(content);
		result.setSubjects(subjects);
		return result;
	}
	
	/**
	 * 获取题型名称（题型库没有匹配的，取当前大题目为题型）
	 * @param title
	 * @param quesMap 
	 * @return
	 */
	private static QuesTypeAO getSubjectName(String title, Map<String, QuesTypeAO> quesMap) {
		QuesTypeAO questAO = new QuesTypeAO();
		String subjectName = "";
		if(!CollectionUtils.isEmpty(quesMap)) {
			for(String name : quesMap.keySet()) {
				if(title.indexOf(name) != -1) {
					subjectName = name;
					questAO = quesMap.get(name);
					break;
				}
			}
		}
		if("".equals(subjectName)) { // 如果没有匹配到，取题目
			title = title.replace("(", "@").replace("（", "@").replace("（", "@");
			String[] names = title.split("@");
			if(names.length >0) {
				String mixTitle = names[0].replace("、", "@").replace("、", "@").replace(".", "@").replace("．", "@"); 
				if(mixTitle.split("@").length > 0) {
					subjectName = mixTitle.split("@")[1];
				}
			} else {
				subjectName = title;
			}
			questAO.setName(subjectName);
			// 随机生成一个数作为questionTypeId
			questAO.setQuestionTypeId(RandomUtils.nextInt(1, 100));
		}
		return questAO;
	}
	
	/**
	 * 选择题校验
	 * @param text
	 * @return
	 */
	private static boolean checkOption(String text) {
		if(StringUtils.isEmpty(text)) {
			return false;
		}
		if (text.indexOf("选择") != -1 || text.indexOf("单选") != -1 || text.indexOf("单项选择") != -1
				|| text.indexOf("多选") != -1 || text.indexOf("多项选择") != -1) {
			return true;
		}
		return false;
	}
	
	/**
	 * 样式转换
	 * @param style
	 * @return
	 */
	private static Map<String, String> convertDocCss(String style) {
		Map<String, String> styleMap = new HashMap<String, String>(64);
		if(StringUtils.isEmpty(style)) {
			return styleMap;
		}
		// 去掉所有的tab
		String[] s1 = style.replaceAll("\n", "").replaceAll("\\n", "").replaceAll("\r\n", "").replaceAll("\n\r", "")
				.replaceAll("\\r", "").replaceAll("\r", "").split("\\}");
		for (int i = 0; i < s1.length; i++) {
			String s2 = s1[i];
			String[] s3 = s2.split("\\{");
			if (s3.length > 1) {
				String key = s3[0].replaceFirst(".", "");
				String value = s3[1];
				styleMap.put(key, value);
			}
		}
		return styleMap;
	}
	
	/**
	 * doc转换内容
	 * @param p 
	 * @param cssMap
	 * @return
	 */
	private static String convertDocText(Element p, Map<String, String> cssMap) {
		StringBuilder result = new StringBuilder();
		if(p.tagName().equalsIgnoreCase(IMG)) {
			result.append(p.toString());
		} else {
			String css = p.attr(CLASS);
			if(StringUtils.isNotEmpty(css)) {
				StringBuilder per = new StringBuilder();
				StringBuilder suf = new StringBuilder();
				if(cssMap.containsKey(css)) {
					String v = cssMap.get(css);
					// 加粗
					if(v.contains(BOLD)) { 
						per.append(STRONG_1);
						suf.append(STRONG_2);
					}
					// 斜体
					if(v.contains(ITALIC)) {
						per.append(EM_1);
						suf.append(EM_2);
					}
					// 上标
					if(v.contains(SUB)) {
						per.append(SUB_1);
						suf.append(SUB_2);
					}
					// 下标
					if(v.contains(SUPER) || v.contains(SUP)) {
						per.append(SUP_1);
						suf.append(SUP_2);
					}
					// 下滑线
					if(v.contains(UNDERLINE)) {
						per.append(U_1);
						suf.append(U_2);
					}
					result.append(per.toString()).append(p.text()).append(suf.toString());
				}
			} else {
				result.append(p.text());
			}
		}
		return result.toString();
	}
	
	/**
	 * docx转换内容
	 * @param p
	 * @return
	 */
	private static String convertDocxText(Element p) {
		StringBuilder result = new StringBuilder();
		if(p.tagName().equalsIgnoreCase(IMG)) { // 图片不处理
			result.append(p.toString());
		} else {
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
		}
		return result.toString();
	}
	
	/**
	 * 处理选择项
	 * @param text
	 * @param e
	 * @return
	 */
	private static List<OptionAO> convertOptions(String p, Element e) {
		List<OptionAO> options = null;
		String subject = p.substring(0, 2);
		if (!Pattern.matches(OPTION_REG_1, subject)) {
			return options;
		}
		if (e.tagName().equalsIgnoreCase(TABLE)) {
			return options;
		}
		Elements ps = e.select(P);
		if (ps == null || ps.size() <= 0) {
			return options;
		}
		options = new ArrayList<OptionAO>();
		OptionAO option = null;
		StringBuilder tx = null;
		boolean isOption = false;
		for (Element el : ps) {
			if (el.hasText()) {
				String content = el.text();
				if (content.length() >= 2 && Pattern.matches(OPTION_REG_1, content.substring(0, 2))) {
					if (option != null) {
						option.setValue(tx.toString());
					}
					isOption = true;
					option = new OptionAO();
					option.setOption(content.substring(0, 1));
					tx = new StringBuilder();
					options.add(option);
				}
				if (el.nextElementSibling() != null && el.nextElementSibling().tagName() != null
						&& IMG.equalsIgnoreCase(el.nextElementSibling().tagName())) {
					tx.append((el.nextElementSibling().toString()));
				} else {
					if (isOption) {
						tx.append(content.substring(2));
						isOption = false;
					} else {
						tx.append(content);
					}
				}
			}
			if (el.id().equals(ps.last().id()) && option != null && tx != null) {
				option.setValue(tx.toString());
			}
		}
		return options;
	}
	
}
