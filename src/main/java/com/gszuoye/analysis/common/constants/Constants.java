package com.gszuoye.analysis.common.constants;

public class Constants {
	/**
	 * 科目：subjectId
	 */
	public static final String SUBJECT_ID_KEY = "subjectId";
	/**
	 * msg
	 */
	public static final String MSG_KEY = "msg";
	/**
	 * status
	 */
	public static final String STATUS_KEY = "status";
	/**
	 * data
	 */
	public static final String DATA_KEY = "data";
	/**
	 * 题型：dataInfo
	 */
	public static final String DATAINFO_KEY = "dataInfo";
	/**
	 * 成功状态码：10001
	 */
	public static final Integer SUCCESS_CODE = 10001;
	/**
	 * 点分隔符
	 */
	public static final String SPLIT_DIT = ".";
	/**
	 * 统一图片处理
	 */
	public static final String IMG_CONT = "jpg,jpeg,png";
	public static final String IMG_PNG = "png";
	/**
	 * TODO /home/zuoye/,默认会自动添加前缀tessdata，因此，这里不需要加这个目录
	 * 图片识别语言包的绝对路径
	 */
	public static final String TEST4J_LANAGE_DATA = "/home/zuoye/";
	/**
	 * 中文语言包
	 */
	public static final String TEST4J_LANAGE_CHI = "chi_sim";
	/**
	 * 服务端临时图片存储目录
	 */
	public static final String IMAGE_PATH = "/home/zuoye/temp/image/";
	/**
	 * 临时文件目录
	 */
	public static final String FILE_PATH = "/home/zuoye/temp/word/";
//	public static final String FILE_PATH = "E://myworkspace//wordPOI//img";
	
	/**
	 * oss上传接口
	 */
	public static final String UPLOAD_OSS_URL = "https://test.gszuoye.com/upload/upload/many/base64.do";
	/**
	 * 题型获取接口
	 */
	public static final String SUBJECT_TYPE_URL = "https://test.gszuoye.com/paperupload/list/questype.do";
}
