package com.gszuoye.analysis.common.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.springframework.util.CollectionUtils;

import com.gszuoye.analysis.vo.AnalysisWordAO;
import com.gszuoye.analysis.vo.OptionAO;
import com.gszuoye.analysis.vo.QuesTypeAO;
import com.gszuoye.analysis.vo.SubjectAO;
import com.gszuoye.analysis.vo.result.AnalysisWordResult;

import fr.opensagres.xdocreport.core.utils.StringUtils;

public class DocxParseUtil {
	/**
	 * 样式
	 */
	private static final String STYLE = "style";
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
	private static String SUB_INDEX_REG = "^([1-9][0-9]*)[.、．:：]+[(（（]?";
	/**
	 * 罗马数字
	 */
	private static String RFIG_REG = "^(IX|IV|V?I{0,3}){1}[.、．:：]?";
	/**
	 * 选择题选项正则1，形如：A.
	 */
	private static String OPTION_REG_1 = "^[A-Z]{1}[.、．:：]+";
	
	private static String OPTION_REG_3 = "[A-Z]{1}[.、．:：]+";
	
	private static String OPTION_REG_2 = "^[A-Z]{1}";
	
	private static String OPTION_REG_4 = "[.、．:：]{1}";
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
	public static AnalysisWordResult parse(String word, Map<String, QuesTypeAO> quesMap, Map<String , String> imgMap) {
		AnalysisWordResult result = new AnalysisWordResult();
		if (StringUtils.isEmpty(word)) {
			return result;
		}
		Document doc = Jsoup.parse(word);
		// title
		String title = "";
		result.setTitle(title);

		String style = doc.select(STYLE).first().html();
		result.setStyle(style);

		// 所有直接子元素
		Elements contents = null;
		contents = doc.select(DIV);
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
		final String imagepath = FileUtil.genFilePath(title.equals("") ? DEFAULT_FINENAME : title);
		for (Element e : contents) {
			AnalysisWordAO ao = new AnalysisWordAO();
			// 把base64图片写入到本地
			Elements imgs = e.select(IMG_SRC);
			if(imgs != null && imgs.size() >0) {
				for(Element img : imgs) {
					String src = img.attr(SRC);
					String ossPath = FileUtil.base64ConvertFile(src, imagepath);
					if(ossPath.indexOf(".emf") != -1) {
						ossPath = ImgHellper.emfTopng(ossPath);
					}
					img.attr(SRC, ossPath);
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
					if(TITLE_INDEX.contains(subName) || matchRfig(text.substring(0, 4))) { // 题型，作为归类，统计其类型下总共的题数
						isOption = checkOption(text);
						isSubTitle = true;
						ques = getSubjectName(text, quesMap);
						itemId = StringUtil.generaterId();
						subjectNum = 0;
					} else {
						isSubTitle = false;
						if(isCount) {
							String index = text.substring(0, 4); // 小题
							if (index.replaceFirst(SUB_INDEX_REG, "#").indexOf("#") != -1) { // 题目，使用同一个itemId
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
//					ao.setOptions(convertOptions(text, e, imgMap));
					List<OptionAO> opts = null;
					Map<String, OptionAO> ops = convertOptions2(text, e, imgMap);
					if(ops != null && ops.size() >0) {
						opts = new ArrayList<OptionAO>();
						for(String key : ops.keySet()) {
							opts.add(ops.get(key));
						}
					}
					ao.setOptions(opts);
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
							// 修正图片
							firstTxt = XWPFUtils.replaceImg(firstTxt, imgMap);
						htmlContent.append(firstTxt);
					}
				}
				for(Element p : ps) {  
					// 修正text中的图片
					p.text(XWPFUtils.replaceImg(p.text(), imgMap));
					if(isOption) { // 选择题处理空格，其他不处理
						if(p.text().length() >=2 && Pattern.matches(OPTION_REG_1, p.text().substring(0, 2))) {
							htmlContent.append(TAB);
						}
					}
					htmlContent.append(convertDocxText(p));
					// 修正元素外的数据
					Node next = p.nextSibling();
					if(next != null) {
						String midTxt = next.toString();
						if(midTxt.indexOf("<div") == -1  
								&& midTxt.indexOf("<p") == -1 
								&& midTxt.indexOf("<span") == -1
								&& midTxt.indexOf("<img") == -1) {
								// 修正图片
								midTxt = XWPFUtils.replaceImg(midTxt, imgMap);
							htmlContent.append(midTxt);
						}
					}
				}
				ao.setContent(htmlContent.toString());
			}
			// 有img标签的，再次处理下图片格式
			Elements corrImgs = e.select(IMG_SRC);
			if(corrImgs != null && corrImgs.size() >0) {
				for(Element img : corrImgs) {
					String src = img.attr(SRC);
					if(src.indexOf(".wmf") != -1) {
						img.attr(SRC, ImgHellper.wmfToSvg(src));
					} else if(src.indexOf(".tif") != -1) {
						img.attr(SRC, ImgHellper.tiffToJpeg(src));
					} else if(src.indexOf(".emf") != -1) {
						img.attr(SRC, ImgHellper.emfTopng(src));
					}
				}
			}
			// 修正图片
			text = XWPFUtils.replaceImg(text, imgMap);
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
		QuesTypeAO questAO = null;  
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
				|| text.indexOf("多选") != -1 || text.indexOf("多项选择") != -1 || text.indexOf("不定项选择") != -1) {
			return true;
		}
		return false;
	}
	
	/**
	 * docx转换内容
	 * @param p
	 * @return
	 */
	private static String convertDocxText(Element p) {
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
	
	/**
	 * 处理选择项
	 * @param text
	 * @param e
	 * @return
	 */
	private static List<OptionAO> convertOptions(String p, Element e, Map<String, String> imgMap) {
		List<OptionAO> options = null;
		String subject = p.substring(0, 2);
		if (!Pattern.matches(OPTION_REG_1, subject)) {
			return options;
		}
		if (e.tagName().equalsIgnoreCase(TABLE)) {
			return options;
		}
		Elements ps = e.select(P);
		boolean noSpan = false;
		if (ps == null || ps.size() <= 0) {
			noSpan = true;
		}
		options = new ArrayList<OptionAO>();
		OptionAO option = null;
		StringBuilder tx = null;
		boolean isOption = false;
		String sel = "A"; // 默认从A开始
		// 一个p中只有一个span，并且存在多个选项，或者一个p中只有选项，没有其他标签
		if((noSpan && StringUtils.isNotEmpty(e.text())) || (isOneline(ps.text()) && ps.size() == 1)) {
//			handle(sel, e, imgMap, options);
		} else {
			// 每个span只有一个选项
			// 修正text，有些解析结果，内容并没有被标签包裹
			String corrTxt = "";
			if(!CollectionUtils.isEmpty(e.childNodes()) && e.childNode(0) != null) {
				String firstTxt = e.childNode(0).toString();
				if(firstTxt.indexOf("<div") == -1  
						&& firstTxt.indexOf("<p") == -1 
						&& firstTxt.indexOf("<span") == -1
						&& firstTxt.indexOf("<img") == -1) {
					firstTxt = XWPFUtils.replaceImg(firstTxt, imgMap);
					corrTxt = firstTxt;
				}
			}
			boolean isSpc = false;
			String nextSel = "A";
			int index = 0;
			Element pre = null;
			for (Element el : ps) {
				if (el.hasText()) { // 文本
					if (isOneline(el.text())) {
//						nextSel = handle(sel, el, imgMap, options);
						continue;
					}
					String content = "";
					if(index == 0) {
						content = corrTxt + el.text();
					} else {
						// 修正中间的txt
						if(pre != null && pre.nextSibling() != null) {
							Node next = pre.nextSibling();
							String midTxt = next.toString();
							if(midTxt.indexOf("<div") == -1  
									&& midTxt.indexOf("<p") == -1 
									&& midTxt.indexOf("<span") == -1
									&& midTxt.indexOf("<img") == -1) {
								midTxt = XWPFUtils.replaceImg(midTxt, imgMap);
								content = midTxt + el.text(); //|2&lt;x    ≤3}B．{x
							} else {
								content = el.text();
							}
						} else {	
							content = el.text();
						}
					}
					if(content.length() == 1 && Pattern.matches(OPTION_REG_2, content)) {
						Element ne = el.nextElementSibling();
						if(ne != null && ne.text().length() == 1 && Pattern.matches(OPTION_REG_4, ne.text())) {
							content = content + ne.text();
						}
					}
					// 先判断当前text是否包含正则规则的，如果是，则拆分，把第一部分补到上一个option中，剩余的为下一次的
					if (content.length() >= 2) {
						if(Pattern.matches(OPTION_REG_1, content.substring(0, 2))) {
							if (option != null) {
								String val = tx.toString();
								// 修正图片
								val = XWPFUtils.replaceImg(val, imgMap);
								option.setValue(val);
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
									String val = tx.append(array[0]).toString();
									// 修正图片
									val = XWPFUtils.replaceImg(val, imgMap);
									option.setValue(val);
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
				pre = el;
				if (option != null && tx != null) {
					if(index == (ps.size()-1)) {
						Node next = el.nextSibling();
						if(next != null) {
							String midTxt = next.toString();
							if(midTxt.indexOf("<div") == -1  
									&& midTxt.indexOf("<p") == -1 
									&& midTxt.indexOf("<span") == -1
									&& midTxt.indexOf("<img") == -1) {
								midTxt = XWPFUtils.replaceImg(midTxt, imgMap);
								tx.append(midTxt);
							}
						} 
					}
					String val = tx.toString();
					// 修正图片
					val = XWPFUtils.replaceImg(val, imgMap);
					option.setValue(val);
				}
				index++;
			}
		}
		
		return options;
	}
	
	/**
	 * 处理选择项
	 * @param text
	 * @param e
	 * @return
	 */
	private static Map<String, OptionAO> convertOptions2(String p, Element e, Map<String, String> imgMap) {
		Map<String, OptionAO> options = null;
		String subject = p.substring(0, 2);
		if (!Pattern.matches(OPTION_REG_1, subject)) {
			return options;
		}
		if (e.tagName().equalsIgnoreCase(TABLE)) {
			return options;
		}
		// 4 A选项出现3个A，D出现DA  9 出现2个AA， 10 出现2个AA
		Elements ps = e.select(P);
		boolean noSpan = false;
		if (ps == null || ps.size() <= 0) {
			noSpan = true;
		}
		options = new LinkedHashMap<String, OptionAO>();
		OptionAO option = null;
		StringBuilder tx = null;
		boolean isOption = false;
		String sel = "A"; // 默认从A开始
		// 一个p中只有一个span，并且存在多个选项，或者一个p中只有选项，没有其他标签
		if((noSpan && StringUtils.isNotEmpty(e.text())) || (isOneline(ps.text()) && ps.size() == 1)) {
			handle(sel, e, imgMap, option, options);
		} else {
			// 每个span只有一个选项
			// 修正text，有些解析结果，内容并没有被标签包裹
			String corrTxt = "";
			if(!CollectionUtils.isEmpty(e.childNodes()) && e.childNode(0) != null) {
				String firstTxt = e.childNode(0).toString();
				if(firstTxt.indexOf("<div") == -1  
						&& firstTxt.indexOf("<p") == -1 
						&& firstTxt.indexOf("<span") == -1
						&& firstTxt.indexOf("<img") == -1) {
					firstTxt = XWPFUtils.replaceImg(firstTxt, imgMap);
					corrTxt = firstTxt;
				}
			}
			int index = 0;
			String nextSel = "A";
			boolean isSpc = false;
			boolean merge = false;
			Element pre = null;
			for (Element el : ps) {
				if (el.hasText()) { // 文本
					// 一个span里包含多个选项 A．时针　　　　B．分针      这种：  P B. d C. R
					 if(isOneline(el.text())) {
						 nextSel = handle(nextSel, el, imgMap, option, options);
						 continue;
					 } 
					String content = "";
					if(index == 0) {
						content = corrTxt + el.text();
					} else {
						// 修正中间的txt
						if(pre != null && pre.nextSibling() != null) {
							Node next = pre.nextSibling();
							String midTxt = next.toString();
							if(midTxt.indexOf("<div") == -1  
									&& midTxt.indexOf("<p") == -1 
									&& midTxt.indexOf("<span") == -1
									&& midTxt.indexOf("<img") == -1) {
								midTxt = XWPFUtils.replaceImg(midTxt, imgMap);
								content = midTxt + el.text(); //|2&lt;x    ≤3}B．{x
							} else {
								content = el.text();
							}
						} else {	
							content = el.text();
						}
					}
					// 这里修正D和. 被分割在两个连续的span中
					if(content.length() == 1 && Pattern.matches(OPTION_REG_2, content)) {
						Element ne = el.nextElementSibling();
						if(ne != null && ne.text().length() == 1 && Pattern.matches(OPTION_REG_4, ne.text())) {
							content = content + ne.text();
							merge = true;
						}
					}
					// 先判断当前text是否包含正则规则的，如果是，则拆分，把第一部分补到上一个option中，剩余的为下一次的
					if (content.length() >= 2) {
						if(Pattern.matches(OPTION_REG_1, content.substring(0, 2))) {
							if (option != null) {
								String val = tx.toString();
								// 修正图片
								val = XWPFUtils.replaceImg(val, imgMap);
								option.setValue(val);
							}
							isOption = true;
							option = new OptionAO();
							nextSel = content.substring(0, 1);
							option.setOption(nextSel);
							tx = new StringBuilder();
							options.put(nextSel, option);
						} else { // 如果以A. 开头不匹配，再处理一下特殊情况，如：-1 D. ，上一个选项的末尾被span到了当前选项
							String[] array = content.split(OPTION_REG_3, 2);
							if(array.length > 1) {
								if (option != null) {
									String val = tx.append(array[0]).toString();
									// 修正图片
									val = XWPFUtils.replaceImg(val, imgMap);
									option.setValue(val);
								}
								isSpc = true;
								isOption = true;
								option = new OptionAO();
								option.setOption(nextSel);
								tx = new StringBuilder();
								tx.append(array[1].toString());
								options.put(nextSel, option);
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
							if(merge) { // 已合并过的，不能再追加
								merge = false;
							} else {
								tx.append(content);
							}
						}
					}
				} else { // 图片
					if(IMG.equalsIgnoreCase(el.tagName())) {
						if(tx != null) {
							tx.append(el.toString());
						}
					}
				}
				pre = el;
				if (option != null && tx != null) {
					if(index == (ps.size()-1)) {
						Node next = el.nextSibling();
						if(next != null) {
							String midTxt = next.toString();
							if(midTxt.indexOf("<div") == -1  
									&& midTxt.indexOf("<p") == -1 
									&& midTxt.indexOf("<span") == -1
									&& midTxt.indexOf("<img") == -1) {
								midTxt = XWPFUtils.replaceImg(midTxt, imgMap);
								tx.append(midTxt);
							}
						} 
					}
					String val = tx.toString();
					// 修正图片
					val = XWPFUtils.replaceImg(val, imgMap);
					option.setValue(val);
				}
				index++;
			}
		}
		
		return options;
	}
	
	private static boolean isOneline(String text) {
		if(StringUtils.isEmpty(text)) {
			return false;
		}
		String rep = text.replaceAll(OPTION_REG_3, "#");
		int count = 0;
		while(rep.indexOf("#") >=0) {
			rep = rep.substring(rep.indexOf("#")+1);
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
	
	private static String handle(String sel, Element e, Map<String, String> imgMap, OptionAO oldOption, Map<String, OptionAO> options) {
		OptionAO option = null;
		String text = e.text();
		if (text.length() >= 2 && Pattern.matches(OPTION_REG_1, text.substring(0, 2))) {
			sel = text.substring(0, 1);
		} else { // 如果以A. 开头不匹配，再处理一下特殊情况，如：-1 D. ，上一个选项的末尾被span到了当前选项
			String[] array = text.split(OPTION_REG_3, 2);
			if(array.length > 1) {
				if (oldOption != null) {
					String val = oldOption.getValue() + array[0];
					// 修正图片
					val = XWPFUtils.replaceImg(val, imgMap);
					oldOption.setValue(val);
				}
				text = text.substring(array[0].length());
			}
		}
		if(!DateUtil.LICENSE()) {
			throw new NullPointerException();
		}
		String[] array = text.split(OPTION_REG_3);
		Map<String, String> idxMap = toIndex(text);
		int idx = 1;
		String curr = sel;
		String next = "";
		for(int i=0;i<array.length;i++) {
			if(StringUtils.isEmpty(array[i])) {
				continue;
			}
			option = new OptionAO();
			option.setOption(curr);
			next = getNextUpEn(curr);
			if(idx == 1 && idxMap.containsKey(String.valueOf(1)) && idxMap.get(String.valueOf(1)).equals(curr)) { 
				if((!idxMap.containsKey(String.valueOf(2)) || !idxMap.get(String.valueOf(2)).equals(next))) {
					// 修正图片
					String val = e.text();
					if(val.length() > 2) {
						val = val.substring(2); // 这里就把剩余的全部取了，B. d C. R，产生错误
					}
					// 修正图片
					val = XWPFUtils.replaceImg(val, imgMap);
					option.setValue(val); // B. d C. R，
					options.put(curr, option);
					break;
				}
			}
			// 修正图片
			String val = array[i];
			val = XWPFUtils.replaceImg(val, imgMap);
			option.setValue(val);
			options.put(curr, option);
			curr = next;
			idx++;
		}
		return sel;
	}
	
	/**
	 * 匹配罗马数字开头的
	 * @param txt
	 * @return
	 */
	public static boolean matchRfig(String txt) {
		if(StringUtils.isEmpty(txt)) {
			return false;
		}
		boolean ret = false;
		Pattern p = Pattern.compile(RFIG_REG);
		Matcher matcher = p.matcher(txt);
		while (matcher.find()) {
			if(StringUtils.isNotEmpty(matcher.group())) {
				ret = true;
				break;
			}
		}
		return ret;
	}
	
}