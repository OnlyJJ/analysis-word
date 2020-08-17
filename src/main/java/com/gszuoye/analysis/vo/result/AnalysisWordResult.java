package com.gszuoye.analysis.vo.result;

import java.io.Serializable;
import java.util.List;

import com.gszuoye.analysis.vo.AnalysisWordAO;
import com.gszuoye.analysis.vo.SubjectAO;

/**
 * 解析word返回结果
 *
 */
public class AnalysisWordResult implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 试卷标题
	 */
	private String title;
	/**
	 * 文件路径
	 */
	private String fileUrl;
	/**
	 * 解析当前word生成的css样式
	 */
	private String style;
	/**
	 * 题目列表
	 */
	private List<SubjectAO> subjects;
	/**
	 * 解析后的内容
	 */
	private List<AnalysisWordAO> content;
	/**
	 * 解析后的文档（图片和pdf使用）
	 */
	private String doc;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getFileUrl() {
		return fileUrl;
	}
	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}
	public String getStyle() {
		return style;
	}
	public void setStyle(String style) {
		this.style = style;
	}
	public List<AnalysisWordAO> getContent() {
		return content;
	}
	public void setContent(List<AnalysisWordAO> content) {
		this.content = content;
	}
	public List<SubjectAO> getSubjects() {
		return subjects;
	}
	public void setSubjects(List<SubjectAO> subjects) {
		this.subjects = subjects;
	}
	public String getDoc() {
		return doc;
	}
	public void setDoc(String doc) {
		this.doc = doc;
	}
	
}
