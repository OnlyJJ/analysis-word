package com.gszuoye.analysis.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
	/**
	 * yyyy/MM/dd
	 */
	private static final String YYYY_MM_DD = "yyyy/MM/dd";
	
	 /**
     * 日期路径 即年/月/日 如2020/08/08
     */
    public static final String datePath() {
        Date now = new Date();
        return new SimpleDateFormat(YYYY_MM_DD).format(now);
    }

}
