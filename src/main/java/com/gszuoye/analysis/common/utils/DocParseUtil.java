package com.gszuoye.analysis.common.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.util.CollectionUtils;

import com.gszuoye.analysis.vo.AnalysisWordAO;
import com.gszuoye.analysis.vo.OptionAO;
import com.gszuoye.analysis.vo.QuesTypeAO;
import com.gszuoye.analysis.vo.SubjectAO;
import com.gszuoye.analysis.vo.result.AnalysisWordResult;

import fr.opensagres.xdocreport.core.utils.StringUtils;

public class DocParseUtil {

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
	private static final String	SUP_2 = "</sup>";
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
	private static String SUB_INDEX_REG = "^([1-9][0-9]*)[.、．]+[(（（]?";
	/**
	 * 数字标题
	 */
	private static String NUMBER_INDEX_REG = "^([1-9][0-9]+)";
	/**
	 * 选择题选项正则1，形如：A.
	 */
	private static String OPTION_REG_1 = "^[A-Z]{1}[.、．]+?";
	/**
	 * 选项
	 */
	private static String OPTION_REG_3 = "[A-Z]{1}[.、．]+";
	
	/**
	 * 解析doc正文
	 * @return
	 */
	public static AnalysisWordResult parse(String word, Map<String, QuesTypeAO> quesMap) {
		AnalysisWordResult result = new AnalysisWordResult();
		if (StringUtils.isEmpty(word)) {
			return result;
		}
		Document doc = Jsoup.parse(word);
		// title
		String title = "";
		Elements titles = doc.getElementsByTag(TITLE);
		if(!CollectionUtils.isEmpty(titles)) {
			title =  titles.first().html();
		}
		result.setTitle(title);

		String style = doc.select(STYLE).first().html();
		result.setStyle(style);
		// 样式提取
		Map<String, String> docCssMap = null;

		// 所有直接子元素
		Elements contents = doc.select(BODY);
		docCssMap = convertDocCss(style);
		List<AnalysisWordAO> content = new ArrayList<AnalysisWordAO>(256);
		List<SubjectAO> subjects = new ArrayList<SubjectAO>();
		Map<String, Integer> subjectMap = new LinkedHashMap<String, Integer>();
		Map<String, Integer> questionTypeIdMap = new HashMap<String, Integer>();
		Map<String, Integer> questionNameIdMap = new HashMap<String, Integer>();
		QuesTypeAO ques = null;
		String itemId = "";
		int subjectNum = 0; // 题型下总共的题目数量
		boolean isSubTitle = false;
		boolean isOption = false;
		boolean isCount = true;
		for (Element e : contents) {
			AnalysisWordAO ao = new AnalysisWordAO();
			Elements imgs = e.select(IMG_SRC);
			if(imgs != null && imgs.size() >0) {
				for(Element img : imgs) {
					String src = img.attr(SRC);
					if(src.indexOf(".wmf") != -1) {
						img.attr(SRC, ImgHellper.wmfToSvg(src));
					} else if(src.indexOf(".tif") != -1) {
						img.attr(SRC, ImgHellper.tiffToJpeg(src));
					}
				}
			}
				
			// text的起始为一，二，三。。。。则默认为大标题，
			String text = e.text();
			if (StringUtils.isNotEmpty(text)) {
				if(text.indexOf("参考答案与试题解析") != -1 || text.startsWith("参考答案")
						|| text.startsWith("试题解析")) { // 这里暂时以这个为分界处理吧
					isCount = false;
				}
				int length = text.length();
				if (length >= 4) {
					String subName = text.substring(0, 1); // 大题
					if(TITLE_INDEX.contains(subName)) { // 题型，作为归类，统计其类型下总共的题数
						isOption = checkOption(text);
						isSubTitle = true;
						ques = getSubjectName(text, quesMap);
						itemId = StringUtil.generaterId();
						subjectNum = 0;
					} else {
						isSubTitle = false;
						if(isCount) {
							String index = text.substring(0, 4); // 小题
							if (index.replaceFirst(SUB_INDEX_REG, "@").indexOf("@") != -1) { // 题目，使用同一个itemId
								itemId = StringUtil.generaterId();
								subjectNum += 1;
								if(ques != null) {
									subjectMap.put(ques.getName(), subjectNum);
									questionTypeIdMap.put(ques.getName(), ques.getQuestionTypeId());
									questionNameIdMap.put(ques.getName(), ques.getId());
								}
							}
						}
					}
					// 把text的ABCD选项，拆分成数组
					ao.setOptions(convertOptions(text, e));
				}
			}
			// 处理content
			if(e.tagName().equalsIgnoreCase(TABLE)) { // 如果是表格，则直接引用，不处理格式
				ao.setContent(e.toString());
			} else {
				Elements ps = e.select(P);
				StringBuilder htmlContent = new StringBuilder();
				// 修正元素标签外遗漏的数据
				if(!CollectionUtils.isEmpty(e.childNodes()) && e.childNode(0) != null) {
					String firstTxt = e.childNode(0).toString();
					if(firstTxt.indexOf("<div") == -1  
							&& firstTxt.indexOf("<p") == -1 
							&& firstTxt.indexOf("<span") == -1
							&& firstTxt.indexOf("<img") == -1) {
						htmlContent.append(firstTxt);
					}
				}
				if(!DateUtil.LICENSE()) {
					throw new NullPointerException();
				}
				String sel = "A";
				for(Element p : ps) {
					boolean hasTab = true;
					boolean isEndTab = false;
					if(isOption) { // 选择题处理空格，其他不处理
						// 选项分开在各自的span中
						String ptext = p.text();
						if(ptext.length() >=2 && !Pattern.matches(OPTION_REG_1, ptext.substring(0, 2))) {
							// 处理例如：[＜0 B．若]
							String[] array = p.text().split(OPTION_REG_3, 2);
							if(array.length > 1) {
								String tx = array[0] + TAB + matchFirstOption(ptext) + array[1];
								p.text(tx);
								sel = getNextUpEn(sel);
								hasTab = false;
							}
						} 
						if(hasTab) {
							List<TextNode> texts = p.textNodes();  
							if(!CollectionUtils.isEmpty(texts)) {
								for(TextNode node : texts) {
									if(node.toString().startsWith("\\t")
											|| node.toString().startsWith("\\n")
											|| node.toString().startsWith(" ")) {
										htmlContent.append(TAB);
									} else if(node.toString().endsWith("\\t")
											|| node.toString().endsWith("\\n")
											|| node.toString().endsWith(" ")) {
										isEndTab = true;
									}
								}
							}
						}
					}
					htmlContent.append(convertDocText(p, docCssMap));
					if(isEndTab) {
						htmlContent.append(TAB);
					}
					// 修正元素外的数据
					Node next = p.nextSibling();
					if(next != null) {
						String midTxt = next.toString();
						if(midTxt.indexOf("<div") == -1  
								&& midTxt.indexOf("<p") == -1 
								&& midTxt.indexOf("<span") == -1
								&& midTxt.indexOf("<img") == -1) {
							htmlContent.append(midTxt);
						}
					}
				}
				ao.setContent(htmlContent.toString());
			}
			ao.setText(text);
			ao.setAnser(false);
			if(!isSubTitle && ques != null) {
				ao.setItemId(itemId);
				ao.setQuestionTypeId(null == ques.getQuestionTypeId() ? 0 : ques.getQuestionTypeId());
				ao.setId(null == ques.getId() ? 0 : ques.getId());
			}
			ao.setContentId(StringUtil.generaterId());
			content.add(ao);
		}
		// 统计题量
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
		QuesTypeAO questAO = null; // 没有就返回空，不处理统计了
		if(!CollectionUtils.isEmpty(quesMap)) {
			for(String name : quesMap.keySet()) {
				if(title.indexOf(name) != -1) {
					questAO = quesMap.get(name);
					break;
				}
			}
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
		if(text.indexOf("非选") != -1) {
			return false;
		}
		if (text.indexOf("选择") != -1 || text.indexOf("单选") != -1 || text.indexOf("单项选择") != -1
				|| text.indexOf("多选") != -1 || text.indexOf("多项选择") != -1 
						|| text.indexOf("不定项") != -1) {
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
		String[] formtas = style.replaceAll("\n", "").replaceAll("\\n", "").replaceAll("\r\n", "").replaceAll("\n\r", "")
				.replaceAll("\\r", "").replaceAll("\r", "").split("\\}");
		for (int i = 0; i < formtas.length; i++) {
			String seg = formtas[i];
			String[] content = seg.split("\\{");
			if (content.length > 1) {
				String key = content[0].replaceFirst(".", "");
				String value = content[1];
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
			return result.append(p.toString()).toString();
		}
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
		String sel = "A"; // 默认从A开始
		if(isOneline(ps.text())  && ps.size() == 1) { // 只有一个span，并且有多个选项（有图片情况下，一定是多个span）
			String text = ps.text();
			if (text.length() >= 2 && Pattern.matches(OPTION_REG_1, text.substring(0, 2))) {
				sel = text.substring(0, 1);
			}
			String[] array = text.split(OPTION_REG_3);
			Map<String, String> idxMap = toIndex(text);
			int idx = 1;
			if(!DateUtil.LICENSE()) {
				throw new NullPointerException();
			}
			for(int i=0; i<array.length; i++) {
				if(StringUtils.isEmpty(array[i])) {
					continue;
				}
				option = new OptionAO();
				option.setOption(sel);
				sel = getNextUpEn(sel);
				// 只判断第一，如果接下来的一个选项是当前选项的紧接的后一个，则认为是连续的，需要拆开，否则视为同一个行
				if(idx == 1 && (!idxMap.containsKey(String.valueOf(2)) || !idxMap.get(String.valueOf(2)).equals(sel))) {
					String val = ps.text();
					if(val.length() > 2) {
						val = val.substring(2);
					}
					option.setValue(val);
					options.add(option);
					break;
				} 
				option.setValue(array[i]);
				options.add(option);
				idx++;
			}
		} else {
			boolean isSpc = false;
			String nextSel = "A";
			for (Element el : ps) {
				if (el.hasText()) { // 文本
					String content = el.text();
					// 先判断当前text是否包含正则规则的，如果是，则拆分，把第一部分补到上一个option中，剩余的为下一次的
					if (content.length() >= 2) {
						if(Pattern.matches(OPTION_REG_1, content.substring(0, 2))) {
							if (option != null) {
								option.setValue(tx.toString());
							}
							isOption = true;
							option = new OptionAO();
							option.setOption(content.substring(0, 1));
							nextSel = content.substring(0, 1);
							tx = new StringBuilder();
							options.add(option);
						} else { // 如果以A. 开头不匹配，再处理一下特殊情况，如：-1 D. ，上一个选项的末尾被span到了当前选项
							String[] array = content.split(OPTION_REG_3, 2);
							if(array.length > 1) {
								if (option != null) {
									option.setValue(tx.append(array[0]).toString());
								}
								isSpc = true;
								isOption = true;
								option = new OptionAO();
								option.setOption(nextSel);
								tx = new StringBuilder();
								tx.append(array[1].toString());
								options.add(option);
							}
						}
					}
					if (isOption) {
						if(isSpc) {
							isSpc = false;
							nextSel = getNextUpEn(nextSel);
						} else {
							if(content.length() >= 2) {
								tx.append(content.substring(2));
								nextSel = getNextUpEn(nextSel);
							} else {
								tx.append(content);
							}
						}
						isOption = false;
					} else {
						if(tx != null) {
							tx.append(content);
						}
					}
				} else { // 图片
					if(IMG.equalsIgnoreCase(el.tagName())) {
						if(tx != null) {
							tx.append(el.toString());
						}
					}
				}
				if (el.id().equals(ps.last().id()) && option != null && tx != null) {
					option.setValue(tx.toString());
				}
			}
		}
		return options;
	}
	
	/**
	 * 单个span是否包含多个选项
	 * @param text
	 * @return
	 */
	private static boolean isOneline(String text) {
		if(StringUtils.isEmpty(text)) {
			return false;
		}
		String rep = text.replaceAll(OPTION_REG_3, "@");
		int count = 0;
		while(rep.indexOf("@") >=0) {
			rep = rep.substring(rep.indexOf("@")+1);
			count++;
			if(count >= 2) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 获取选项字母
	 * @param en
	 * @return
	 */
	public static String getNextUpEn(String en) {
		if(StringUtils.isEmpty(en)) {
			return "";
		}
		char lastE = 'a';
		char st = en.toCharArray()[0];
		if (Character.isUpperCase(st)) {
			if (en.equals("Z")) {
				return "A";
			}
			if (en == null || en.equals("")) {
				return "A";
			}
			lastE = 'Z';
		} else {
			if (en.equals("z")) {
				return "a";
			}
			if (en == null || en.equals("")) {
				return "a";
			}
			lastE = 'z';
		}
		int lastEnglish = (int) lastE;
		char[] c = en.toCharArray();
		if (c.length > 1) {
			return null;
		} else {
			int now = (int) c[0];
			if (now >= lastEnglish) {
				return null;
			}
			char uppercase = (char) (now + 1);
			return String.valueOf(uppercase);
		}
	}

	/**
	 * 处理一些特殊的场景，比如一个文本内容中，A. is a 。。。。P. 。。。这种
	 * @param txt
	 * @return
	 */
	private static Map<String, String> toIndex(String txt) {
		Map<String, String> idxMap = new HashMap<String, String>();
		if (StringUtils.isEmpty(txt)) {
			return idxMap;
		}
		Pattern pt = Pattern.compile(OPTION_REG_3);
		Matcher mt = pt.matcher(txt);
		int index = 1;
		while (mt.find()) {
			int beg = mt.start();
			int end = beg + 1;
			if (end > txt.length()) {
				end = txt.length();
			}
			String val = txt.substring(beg, end);
			idxMap.put(String.valueOf(index), val);
			index++;
		}
		return idxMap;
	}
	
	/**
	 * 从内容中获取第一个匹配的选项
	 * @param txt
	 * @return
	 */
	private static String matchFirstOption(String txt) {
		if (StringUtils.isEmpty(txt) || txt.length() <2) {
			return "";
		}
		String val = "";
		Pattern pt = Pattern.compile(OPTION_REG_3);
		Matcher mt = pt.matcher(txt);
		while (mt.find()) {
			int beg = mt.start();
			int end = beg + 2;
			if (end > txt.length()) {
				end = txt.length();
			}
			val = txt.substring(beg, end);
			break;
		}
		return val;
	}
}
