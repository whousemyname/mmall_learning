package com.gogotao.controller.portal;


import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.gogotao.common.Const;
import com.gogotao.common.ResponseCode;
import com.gogotao.common.ServerResponse;
import com.gogotao.pojo.User;
import com.gogotao.service.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Controller
@RequestMapping("/order/")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    @Autowired
    private IOrderService iOrderService;

    @RequestMapping("create.do")
    @ResponseBody
    public ServerResponse create(HttpSession session, Integer shippingId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.create(user.getId(), shippingId);
    }

    @RequestMapping("cancle.do")
    @ResponseBody
    public ServerResponse cancle(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.cancle(user.getId(), orderNo);
    }

    @RequestMapping("get_order_product.do")
    @ResponseBody
    public ServerResponse getOrderProduct(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderProduct(user.getId());
    }

    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse detail(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.detail(user.getId(), orderNo);
    }

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse list(HttpSession session,
                               @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                               @RequestParam(value = "pageSize", defaultValue = "10")Integer pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.list(user.getId(), pageNum, pageSize);
    }

    @RequestMapping("pay.do")
    @ResponseBody
    public ServerResponse<Map<String, String>> pay(HttpSession session, Long orderNo, HttpServletRequest request){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String path = request.getSession().getServletContext().getRealPath("uplaod");
        System.out.println(path);
        return iOrderService.pay(user.getId(), orderNo, path);
    }


    @RequestMapping("alipay_callback.do")
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request){
        Map<String, String[]> requestParams = request.getParameterMap();
        Map<String, String> params = new HashMap<>();
        for (Iterator iterator = requestParams.keySet().iterator(); iterator.hasNext();){
            String key = (String) iterator.next();
            String[] values = requestParams.get(key);
            String valueStr = "";
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < values.length; i++){
                stringBuilder.append(i == (values.length - 1)? values[i] : values[i] + ",");
            }
            valueStr = stringBuilder.toString();
            params.put(key, valueStr);
        }
        logger.info("支付宝回调,sign{},trade_status:{},参数:{}", params.get("sign"), params.get("trade_status"), params.toString());
        params.remove("sign_type");
        try {
            boolean signature = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(), "utf-8", Configs.getSignType());
            if (!signature){
                return ServerResponse.createByErrorMessage("支付宝验证签名失败, 非法请求,请勿再次进行");
            }
        } catch (AlipayApiException e) {
            logger.error("支付宝验证签名出现异常", e);
            return Const.AlipayCallback.RESPONSE_FAILED;
        }

        //todo 各种验证, 如:订单号, 总价等订单信息


        ServerResponse serverResponse = iOrderService.alipayCallback(params);
        if (serverResponse.isSuccess()){    //成功像支付宝返回success, 否则返回failed;
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        }
        return Const.AlipayCallback.RESPONSE_FAILED;
    }

    @RequestMapping("query_order_status.do")
    @ResponseBody
    public ServerResponse<Boolean> queryPaied(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.queryPaied(user.getId(), orderNo);
    }
}
