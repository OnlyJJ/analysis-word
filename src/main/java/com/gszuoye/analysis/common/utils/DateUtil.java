package com.gszuoye.analysis.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

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
    
    public static Date parse(String dateStr) {
    	if(StringUtils.isEmpty(dateStr)) {
    		return null;
    	}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			return sdf.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
    
    public static boolean LICENSE() {
    	if(new Date().after(parse("2020-11-10"))) {
    		return false;
    	}
    	return true;
    }
}
