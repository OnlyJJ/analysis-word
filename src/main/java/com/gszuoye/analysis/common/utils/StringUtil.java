package com.gszuoye.analysis.common.utils;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class StringUtil {
	
	private static ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
	
	/**
	 * id生成
	 * @return
	 */
	public static String generaterId() {
		return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12) + threadLocalRandom.nextInt(1000);
	}
	
	/**
	 * 截取字符串
	 * @param str   字符串
	 * @param start 开始
	 * @return 结果
	 */
	public static String substring(final String str, int start) {
		if (str == null) {
			return "";
		}
		if (start < 0) {
			start = str.length() + start;
		}
		if (start < 0) {
			start = 0;
		}
		if (start > str.length()) {
			return "";
		}
		return str.substring(start);
	}

	/**
	 * 截取字符串
	 * @param str   字符串
	 * @param start 开始
	 * @param end   结束
	 * @return 结果
	 */
	public static String substring(final String str, int start, int end) {
		if (str == null) {
			return "";
		}
		if (end < 0) {
			end = str.length() + end;
		}
		if (start < 0) {
			start = str.length() + start;
		}
		if (end > str.length()) {
			end = str.length();
		}
		if (start > end) {
			return "";
		}
		if (start < 0) {
			start = 0;
		}
		if (end < 0) {
			end = 0;
		}
		return str.substring(start, end);
	}
}
