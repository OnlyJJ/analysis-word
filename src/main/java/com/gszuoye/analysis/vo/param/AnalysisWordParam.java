package com.gszuoye.analysis.vo.param;

/**
 * 上传试卷接口参数
 * 	前端需要回显的字段，可以在此做中转
 *
 */
public class AnalysisWordParam {
	/**
	 * 教师id
	 */
	private Integer teacherId;
	/**
	 * 科目id
	 */
	private Integer subjectId;
	/**
	 * 科目名
	 */
	private String subjectName;
	/**
	 * 文件上传后，生成的路径
	 */
	private String filePath;
	/**
	 * 文件名
	 * @return
	 */
	private String fileName;
	
	public Integer getTeacherId() {
		return teacherId;
	}
	public void setTeacherId(Integer teacherId) {
		this.teacherId = teacherId;
	}
	public Integer getSubjectId() {
		return subjectId;
	}
	public void setSubjectId(Integer subjectId) {
		this.subjectId = subjectId;
	}
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
	public String getSubjectName() {
		return subjectName;
	}
	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}
	
}
