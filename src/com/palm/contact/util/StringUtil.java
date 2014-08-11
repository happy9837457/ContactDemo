package com.palm.contact.util;

import java.util.Locale;

/**
 * 字符串工具类
 * 
 * @author weixiang.qin
 * 
 */
public class StringUtil {
	/**
	 * 是否为null或""
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str) {
		if (str == null || "".equals(str.trim())) {
			return true;
		}
		return false;
	}

	/**
	 * 替换空格
	 * 
	 * @param str
	 * @return
	 */
	public static String replaceSpace(String str) {
		if (str == null) {
			return str;
		}
		return str.replace(" ", "");
	}

	/**
	 * 
	 * @param b
	 * @return
	 */
	public static String hexToHexString(byte[] b) {
		int len = b.length;
		int[] x = new int[len];
		String[] y = new String[len];
		StringBuilder str = new StringBuilder();
		int j = 0;
		for (; j < len; j++) {
			x[j] = b[j] & 0xff;
			y[j] = Integer.toHexString(x[j]);
			while (y[j].length() < 2) {
				y[j] = "0" + y[j];
			}
			str.append(y[j]);
			str.append("");
		}
		return new String(str).toUpperCase(Locale.getDefault());
	}
	
	/**
	 * 前面补零(一位前面补零,两位以上不操作)
	 * 
	 * @param str
	 * @return
	 */
	public static String appendZero(String str) {
		return "00".substring(str.length()) + str;
	}
}
