package com.gszuoye.analysis.vo;

import java.util.List;

/**
 * 内容包装类
 *
 */
public class AnalysisWordAO {
	/**
	 * 标识每段content的唯一id
	 */
	private String contentId;
	/**
	 * 内容，html片段
	 */
	private String content; 
	/**
	 * 文本内容
	 */
	private String text; 
	/**
	 * 分块id
	 */
	private String itemId; 
	/**
	 * 题型id，0-无
	 */
	private Integer questionTypeId; 
	/**
	 * 细分的题型id（唯一）
	 */
	private Integer id;
	/**
	 * 类型，0-无
	 */
	private int type;
	/**
	 * 是否设置答案
	 */
	private boolean isAnser;
	/**
	 * 选择项处理
	 */
	private List<OptionAO> options;
	
	public String getContentId() {
		return contentId;
	}
	public void setContentId(String contentId) {
		this.contentId = contentId;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getItemId() {
		return itemId;
	}
	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
	public Integer getQuestionTypeId() {
		return questionTypeId;
	}
	public void setQuestionTypeId(Integer questionTypeId) {
		this.questionTypeId = questionTypeId;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public boolean isAnser() {
		return isAnser;
	}
	public void setAnser(boolean isAnser) {
		this.isAnser = isAnser;
	}
	public List<OptionAO> getOptions() {
		return options;
	}
	public void setOptions(List<OptionAO> options) {
		this.options = options;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
}
