package com.mmall.test;

import com.gogotao.utils.BigDecimalUtil;
import org.junit.Test;

import java.math.BigDecimal;

public class BigDecimalUtilTest {


    @Test
    public void test(){
        BigDecimal b1 = new BigDecimal("1.5");
        BigDecimal b2 = new BigDecimal("1.5");
        System.out.println(BigDecimalUtil.add(b1.doubleValue(), b2.doubleValue()));
        System.out.println(BigDecimalUtil.sub(b1.doubleValue(), b2.doubleValue()));
        System.out.println(BigDecimalUtil.mul(b1.doubleValue(), b2.doubleValue()));
        System.out.println(BigDecimalUtil.div(b1.doubleValue(), b2.doubleValue()));

    }
}
