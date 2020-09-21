package com.gszuoye.analysis.vo;

/**
 * 题目列表
 *
 */
public class SubjectAO {
	/**
	 * 题型id
	 */
	private Integer questionTypeId;
	/**
	 * 题型名称
	 */
	private String subjectTitle;
	
	/**
	 * 题量
	 */
	private int count;
	
	/**
	 * 大题型id
	 */
	private Integer id;
	/**
	 * 题型归类id
	 */
	private String classifyId;
	
	public Integer getQuestionTypeId() {
		return questionTypeId;
	}

	public void setQuestionTypeId(Integer questionTypeId) {
		this.questionTypeId = questionTypeId;
	}

	public String getSubjectTitle() {
		return subjectTitle;
	}

	public void setSubjectTitle(String subjectTitle) {
		this.subjectTitle = subjectTitle;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getClassifyId() {
		return classifyId;
	}

	public void setClassifyId(String classifyId) {
		this.classifyId = classifyId;
	}
	
	
}
