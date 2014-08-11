package com.palm.contact.util;

import java.util.regex.Pattern;

/**
 * 验证工具类
 * 
 * @author weixiang.qin
 * 
 */
public class ValidateUtil {
	/**
	 * 验证手机号
	 * 
	 * @param mobileNo
	 * @return
	 */
	public static boolean validateMobileNo(String mobileNo) {
		String pattern = "^[1][0-9]{10}$";
		return Pattern.compile(pattern).matcher(mobileNo).find();
	}
}
