package com.micall.utils;

import org.testng.annotations.Test;

import java.util.Random;

public class CreateRegisterName {
    /**
     * 随机生成数字与字母组合用户名
     * @param length 用户名长度
     * @return
     */
    public static String gerCharAndNumb(int length){
        Random random = new Random();
        StringBuffer stringBufferVal = new StringBuffer();
        String charStr = "0123456789abcdefghijklnmopqrstuvwxyzABCDEFGHIJKLNMOPQRSTUVWSYZ";
        int charLength = charStr.length();
        for(int i=0;i<length;i++){
            int index = random.nextInt(charLength);
            stringBufferVal.append(charStr.charAt(index));
        }
        return stringBufferVal.toString();
    }
}
