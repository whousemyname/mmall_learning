package com.gogotao.utils;

import java.math.BigDecimal;

public class BigDecimalUtil {

    public static BigDecimal add(double b1, double b2){
        BigDecimal b11 = new BigDecimal(Double.toString(b1));
        BigDecimal b22 = new BigDecimal(Double.toString(b2));
        return b11.add(b22);
    }
    public static BigDecimal sub(double b1, double b2){
        BigDecimal b11 = new BigDecimal(Double.toString(b1));
        BigDecimal b22 = new BigDecimal(Double.toString(b2));
        return b11.subtract(b22);
    }
    public static BigDecimal mul(double b1, double b2){
        BigDecimal b11 = new BigDecimal(Double.toString(b1));
        BigDecimal b22 = new BigDecimal(Double.toString(b2));
        return b11.multiply(b22);
    }
    public static BigDecimal div(double b1, double b2){
        BigDecimal b11 = new BigDecimal(Double.toString(b1));
        BigDecimal b22 = new BigDecimal(Double.toString(b2));
        return b11.divide(b22, 2, BigDecimal.ROUND_HALF_UP);    //四舍五入
    }

}
