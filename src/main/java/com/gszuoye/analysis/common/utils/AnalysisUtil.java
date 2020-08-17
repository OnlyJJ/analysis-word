package com.gszuoye.analysis.common.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.CollectionUtils;

import com.gszuoye.analysis.vo.AnalysisWordAO;
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
	 * 约定的起点节点标示
	 */
	private static String TITLE_INDEX = "一二三四五六七八九十";
	/**
	 * 小标题正则
	 */
	private static String SUB_INDEX_REG = "^([1-9][0-9]*)" + "[.、．(（（]+?";
	
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

		// style
		String style = doc.select(STYLE).first().html();
		result.setStyle(style);

		// 所有直接子元素
		Elements contents = null;
		if(isDocx) {
			contents = doc.select(DIV);
		} else {
			contents = doc.select(BODY);
		}
		List<AnalysisWordAO> content = new ArrayList<AnalysisWordAO>();
		List<SubjectAO> subjects = new ArrayList<SubjectAO>();
		Map<String, Integer> subjectMap = new LinkedHashMap<String, Integer>();
		QuesTypeAO ques = null;
		String itemId = "";
		int subjectNum = 0; // 题型下总共的题目数量
		boolean isSubTitle = false;
		for (Element e : contents) {
			AnalysisWordAO ao = new AnalysisWordAO();
			// 更换临时图片url到oss地址
			Elements imgs = e.select("img[src]");
			if(imgs != null && imgs.size() >0) {
				for(Element img : imgs) {
					String src = img.attr("src");
					// docx需要处理上传到oss
					if(uploadOss) {
						// TOTO
						// 默认生成的头部信息不对，这里要替换以下
						String ossPath = FileUtil.uploadOssBase64(src);
						img.attr("src", ossPath);
					} else {
						if(ossMap.containsKey(src)) {
							img.attr("src", ossMap.get(src));
						}
					}
				}
			}
			ao.setContent(e.toString());
			// text的起始为一，二，三。。。。则默认为大标题
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
							subjectMap.put(ques.getName(), subjectNum);
						}
					}
				}
			}
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
		}
		return questAO;
	}
}
