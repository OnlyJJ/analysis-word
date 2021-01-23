package com.gszuoye.analysis.vo.result;

import java.io.Serializable;

public class FileUploadResult implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 文件路径
	 */
	private String filePath;
	/**
	 * 文件名
	 */
	private String fileName;
	/**
	 * 访问路径
	 */
	private String profileUrl;
	
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getProfileUrl() {
		return profileUrl;
	}
	public void setProfileUrl(String profileUrl) {
		this.profileUrl = profileUrl;
	}
	
	
}
