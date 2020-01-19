package com.gogotao.common;

import com.google.common.collect.Sets;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Const {

    public static final String CURRENT_USER = "currentUser";
    public static final String USERNAME = "username";
    public static final String EMAIL = "email";

    public interface ProductListOrderBy{
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_asc", "price_desc");
    }

    public interface Cart{
        int CHECKED = 1;
        int UN_CHECKED = 0;
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
        String LIMIT_NUM_FAIL= "LIMIT_NUM_FAIL";
    }

    public interface Role{
        int ROLE_CUSTOMER = 0; //普通用户
        int ROLE_ADMIN = 1; //管理员
    }


    public enum ProductStatusEnum{
        ON_SALE(1, "在线");
        int code;
        String desc;
        ProductStatusEnum(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        public int getCode() {
            return code;
        }
        public String getDesc() {
            return desc;
        }
        public static ProductStatusEnum codeOf(int code){
            for (ProductStatusEnum productStatusEnum : values()){
                if (code == productStatusEnum.getCode()){
                    return productStatusEnum;
                }
            }
            throw new RuntimeException("没有找到该枚举");
        }
    }


    public interface AlipayCallback{
        String TRADE_SUATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        String TRADE_SUATUS_TRADE_SUCCESS = "TRADE_SUCCESS";
        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";
    }

    public enum OrderStatusEnum{
        CANCLED(0, "已取消"),
        NO_PAY(10, "未支付"),
        PAID(20, "已付款"),
        SHIPPED(40, "已发货"),
        ORDER_SUCCESS(50, "订单完成"),
        ORDER_CLOSE(60, "订单关闭");

        private int code;
        private String desc;

        OrderStatusEnum(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public int getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static OrderStatusEnum codeOf(int code){
            for (OrderStatusEnum orderStatusEnum : values()){
                if (code == orderStatusEnum.getCode()){
                    return orderStatusEnum;
                }
            }
            throw new RuntimeException("没有找到该枚举");
        }
    }

    public enum PayPlatformEnum{
        ALIPAY(1, "支付宝");

        private int code;
        private String desc;
        PayPlatformEnum(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public int getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
        public static PayPlatformEnum codeOf(int code){
            for (PayPlatformEnum payPlatformEnum : values()){
                if (code == payPlatformEnum.getCode()){
                    return payPlatformEnum;
                }
            }
            throw new RuntimeException("没有找到该枚举");
        }
    }

    public enum PaymentTypeEnum{
        ONLINE_PAY(1, "线上支付");
        private int code;
        private String desc;
        PaymentTypeEnum(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public int getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static PaymentTypeEnum codeOf(int code){
            for (PaymentTypeEnum paymentTypeEnum : values()){
                if (code == paymentTypeEnum.getCode()){
                    return paymentTypeEnum;
                }
            }
            throw new RuntimeException("没有找到该枚举");
        }
    }
}
