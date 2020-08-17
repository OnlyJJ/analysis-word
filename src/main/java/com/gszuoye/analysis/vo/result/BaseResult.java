package com.gszuoye.analysis.vo.result;

import java.util.HashMap;


public class BaseResult extends HashMap<String, Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** 状态码 */
	public static final String CODE_TAG = "code";

	/** 返回内容 */
	public static final String MSG_TAG = "msg";

	/** 数据对象 */
	public static final String DATA_TAG = "data";

	public enum Type {
		/** 成功 */
		SUCCESS(0),
		/** 错误 */
		ERROR(-1);
		private final int value;

		Type(int value) {
			this.value = value;
		}

		public int value() {
			return this.value;
		}
	}

	public BaseResult(Type type, String msg, Object data) {
		super.put(CODE_TAG, type.value);
		super.put(MSG_TAG, msg);
		if (data != null) {
			super.put(DATA_TAG, data);
		}
	}

	public static BaseResult success(String msg, Object data) {
		return new BaseResult(Type.SUCCESS, msg, data);
	}
	
	public static BaseResult error(String msg) {
		return new BaseResult(Type.ERROR, msg, null);
	}

}
