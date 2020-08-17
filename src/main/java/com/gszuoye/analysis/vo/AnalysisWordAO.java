package com.gszuoye.analysis.vo;

/**
 * 内容包装类
 *
 */
public class AnalysisWordAO {
	/**
	 * 内容，html片段
	 */
	private String content; 
	/**
	 * 分块id
	 */
	private String itemId; 
	/**
	 * 题型id，0-无
	 */
	private int questionTypeId; 
	/**
	 * 类型，0-无
	 */
	private int type;
	/**
	 * 是否设置答案
	 */
	private boolean isAnser;
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getItemId() {
		return itemId;
	}
	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
	public int getQuestionTypeId() {
		return questionTypeId;
	}
	public void setQuestionTypeId(int questionTypeId) {
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
	
}
