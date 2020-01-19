package com.gogotao.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.domain.OrderDetailResult;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.sqlsource.PageStaticSqlSource;
import com.gogotao.common.Const;
import com.gogotao.common.ServerResponse;
import com.gogotao.dao.*;
import com.gogotao.pojo.*;
import com.gogotao.service.IOrderService;
import com.gogotao.utils.BigDecimalUtil;
import com.gogotao.utils.DateTimeUtils;
import com.gogotao.utils.FtpUtils;
import com.gogotao.utils.PropertiesUtil;
import com.gogotao.vo.OrderItemVo;
import com.gogotao.vo.OrderProductVo;
import com.gogotao.vo.OrderVo;
import com.gogotao.vo.ShippingVo;
import com.sun.corba.se.impl.resolver.ORBInitRefResolverImpl;
import com.sun.org.apache.bcel.internal.generic.MONITORENTER;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    @Autowired
    private PayInfoMapper payInfoMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ShippingMapper shippingMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse create(Integer userId, Integer shippingId){
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);
        if (cartList == null){
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        Long orderNo = generateOrderNo();
        ServerResponse serverResponse = this.getOrderItemList(cartList);
        if (!serverResponse.isSuccess()){
            return serverResponse;
        }
        List<OrderItem> orderItemList = (List<OrderItem>)serverResponse.getData();
        for (OrderItem orderItem : orderItemList){  //设置orderItem的订单号
            orderItem.setOrderNo(orderNo);
        }
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setShippingId(shippingId);
        order.setPayment(this.getOrderTotalPrice(orderItemList));
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        order.setPostage(0);
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        order.setCreateTime(new Date());
        orderMapper.insert(order);
        orderItemMapper.batchInsert(orderItemList);
        //减少库存
        reduceStock(orderItemList);
        //订单对应的购物车进行清空
        deleteCheckedCart(cartList);
        return ServerResponse.createBySuccessData(assembleOrderVo(order, orderItemList));
    }

    private OrderVo assembleOrderVo(Order order, List<OrderItem> orderItemList){
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getDesc());

        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatusEnum.codeOf(order.getStatus()).getDesc());
        orderVo.setPostage(order.getPostage());
        orderVo.setShippingId(order.getShippingId());
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if (shipping != null){
            orderVo.setReceiveName(shipping.getReceiverName());
            orderVo.setShippingVo(assembleShippingVo(shipping));
        }

        orderVo.setCloseTime(DateTimeUtils.dateToString(order.getCloseTime()));
        orderVo.setCreateTime(DateTimeUtils.dateToString(order.getCreateTime()));
        orderVo.setEndTime(DateTimeUtils.dateToString(order.getEndTime()));
        orderVo.setSendTime(DateTimeUtils.dateToString(order.getSendTime()));

        List<OrderItemVo> orderItemVoList = assembleOrderItemVo(orderItemList);
        orderVo.setOrderItemVoList(orderItemVoList);
        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return orderVo;
    }
    private List<OrderItemVo> assembleOrderItemVo(List<OrderItem> orderItemList){
        List<OrderItemVo> orderItemVoList = new ArrayList<>(orderItemList.size());
        for (OrderItem orderItem : orderItemList){
            OrderItemVo orderItemVo = new OrderItemVo();
            orderItemVo.setOrderNo(orderItem.getOrderNo());
            orderItemVo.setProductId(orderItem.getProductId());
            orderItemVo.setProductName(orderItem.getProductName());
            orderItemVo.setProductImage(orderItem.getProductImage());
            orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
            orderItemVo.setQuantity(orderItem.getQuantity());
            orderItemVo.setTotalPrice(orderItem.getTotalPrice());
            orderItemVo.setCreateTime(DateTimeUtils.dateToString(orderItem.getCreateTime()));
            orderItemVoList.add(orderItemVo);
        }
        return orderItemVoList;
    }
    private ShippingVo assembleShippingVo(Shipping shipping){
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        return shippingVo;
    }
    private void deleteCheckedCart(List<Cart> cartList){
        for (Cart cart : cartList){
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }
    private void reduceStock(List<OrderItem> orderItemList){
        for (OrderItem orderItem : orderItemList){
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock() - orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }
    //获取订单总金额
    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList){
        BigDecimal price = new BigDecimal("0");
        for (OrderItem orderItem : orderItemList){
            price = BigDecimalUtil.add(price.doubleValue(), orderItem.getTotalPrice().doubleValue());
        }
        return price;
    }
    //生成订单号, 切记一个订单创建操作只能调用一次该函数
    private Long generateOrderNo(){
        Long time = System.currentTimeMillis();
        return time + new Random().nextInt(100);
    }
    //根据已勾选的购物车item生成orderItem
    private ServerResponse getOrderItemList(List<Cart> cartList){
        List<OrderItem> orderItemList = new ArrayList<>(cartList.size());
        for (Cart cartItem : cartList){
            OrderItem orderItem = new OrderItem();
            orderItem.setUserId(cartItem.getUserId());
            Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
            if (Const.ProductStatusEnum.ON_SALE.getCode() != product.getStatus()){
                return ServerResponse.createByErrorMessage("产品" + product.getName() + "未上线");
            }
            if (product.getStock() < cartItem.getQuantity()){   //购物车需要购买该商品的数量大于该商品的库存
                return ServerResponse.createByErrorMessage("产品" + product.getName() + "库存不足");
            }
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setTotalPrice(BigDecimalUtil.mul(cartItem.getQuantity().doubleValue(), product.getPrice().doubleValue()));

            orderItem.setCreateTime(new Date());    //因为后续需要用到orderItemList中的createTime用于前端展示, 但是后续并没有访问数据库,直接使用的orderItemList,所以在此处直接赋值
                                                    //但是可能存在问题, 此处赋值会导致数据库与前端展示不一致
            orderItem.setUpdateTime(new Date());
            orderItemList.add(orderItem);
        }
        return ServerResponse.createBySuccessData(orderItemList);
    }


    @Override
    public ServerResponse cancle(Integer userId, Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        if (order.getStatus() != Const.OrderStatusEnum.NO_PAY.getCode()){
            return ServerResponse.createByErrorMessage("已付款,无法取消");
        }
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Const.OrderStatusEnum.CANCLED.getCode());
        orderMapper.updateByPrimaryKeySelective(updateOrder);
        return ServerResponse.createBySuccessMessage("订单已经取消");
    }

    @Override
    public ServerResponse getOrderProduct(Integer userId){
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);
        ServerResponse serverResponse = this.getOrderItemList(cartList);
        if (!serverResponse.isSuccess()){
            return serverResponse;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getData();
        List<OrderItemVo> orderItemVoList = this.assembleOrderItemVo(orderItemList);
        OrderProductVo orderProductVo = new OrderProductVo();
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        BigDecimal price = new BigDecimal("0");
        for (OrderItem orderItem : orderItemList){
            price = BigDecimalUtil.add(price.doubleValue(), orderItem.getTotalPrice().doubleValue());
        }
        orderProductVo.setProductTotalPrice(price);
        return ServerResponse.createBySuccessData(orderProductVo);
    }

    @Override
    public ServerResponse detail(Integer userId, Long orderNo){
        Order order = orderMapper.selectOrderByUserIdOrderNo(userId, orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectOrderItemListByUserIdOrderNo(userId, orderNo);
        return ServerResponse.createBySuccessData(assembleOrderVo(order, orderItemList));
    }

    @Override
    public ServerResponse list(Integer userId, Integer pageNum, Integer pageSize){
        User user = userMapper.selectByPrimaryKey(userId);
        List<Order> orderList;
        if (user.getRole() == Const.Role.ROLE_ADMIN){
            PageHelper.startPage(pageNum, pageSize);
            orderList = orderMapper.selectAll();
        }else{
            PageHelper.startPage(pageNum, pageSize);
            orderList = orderMapper.selectOrderListByUserId(userId);
        }
        List<OrderVo> orderVoList = new ArrayList<>();
        for (Order order : orderList){  //无论是管理员还是普通用户, 根据orderNo获取OrderItem,生成OrderVo
            List<OrderItem> orderItemList = orderItemMapper.selectOrderItemListByOrderNo(order.getOrderNo());
            orderVoList.add(this.assembleOrderVo(order, orderItemList));
        }

        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccessData(pageInfo);
    }

    @Override
    public ServerResponse manageDetail(Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        //返回生成的OrderVo对象(使用order, orderItem生成)
        return ServerResponse.createBySuccessData(this.assembleOrderVo(order, orderItemMapper.selectOrderItemListByOrderNo(orderNo)));
    }

    @Override
    public ServerResponse manageSearch(Long orderNo, Integer pageNum, Integer pageSize){
        PageHelper.startPage(pageNum, pageSize);
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        //返回生成的OrderVo对象(使用order, orderItem生成)
        List<OrderItem> orderItemList = orderItemMapper.selectOrderItemListByOrderNo(orderNo);
        OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
        PageInfo pageInfo = new PageInfo(orderItemList);
        List<OrderVo> orderVoList = new ArrayList();
        orderVoList.add(orderVo);
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccessData(pageInfo);
    }

    @Override
    public ServerResponse manageSendGoods(Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        if (order.getStatus() == Const.OrderStatusEnum.PAID.getCode()){
            order.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
            order.setSendTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
            return ServerResponse.createBySuccessMessage("发货成功");
        }
        return ServerResponse.createByErrorMessage("发货失败");
    }







    @Override
    public ServerResponse<Map<String, String>> pay(Integer userId, Long orderNo, String path){
        Map<String, String> map = new HashMap<>();
        Order order = orderMapper.selectOrderByUserIdOrderNo(userId, orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("该订单不存在");
        }
        map.put("orderNo", String.valueOf(order.getOrderNo()));


        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("gogotao扫码支付,订单号:").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder().append("订单").append(outTradeNo).
                append("购买商品共").append(totalAmount).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();
        List<OrderItem> orderItemList = new ArrayList<>();
        orderItemList = orderItemMapper.selectOrderItemListByUserIdOrderNo(userId, orderNo);
        for (OrderItem orderItem : orderItemList){
            // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
            GoodsDetail goods1 = GoodsDetail.newInstance(orderItem.getProductId().toString(), orderItem.getProductName(),
                    BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(), new BigDecimal("100").doubleValue()).longValue(),
                    orderItem.getQuantity());
            // 创建好一个商品后添加至商品明细列表
            goodsDetailList.add(goods1);
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);


        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");
        // 支付宝当面付2.0服务
        AlipayTradeService tradeService;
        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);


                File folder = new File(path);
                if (!folder.exists()){
                    folder.setWritable(true);
                    folder.mkdirs();
                }
                String qrPath = String.format(path + "/qr-%s.png", response.getOutTradeNo());
                String qrName = String.format("qr-%s.png", response.getOutTradeNo());
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);
                File qrFile = new File(path, qrName);
                List<File> fileList = new ArrayList<>(1);
                fileList.add(qrFile);
                try {
                    FtpUtils.uploadFile(fileList);
                } catch (IOException e) {
                    logger.error("上传二维码失败", e);
                }
                logger.info("qrPath:" + qrPath);
                map.put("qrPath", PropertiesUtil.getProperty("ftp.server.http.prefix")+qrName);
                return ServerResponse.createBySuccessData(map);
            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage("支付宝预下单失败!!!");
            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，预下单状态未知!!!");
            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }
    }
    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }


    @Override
    //该函数验证支付状态, 保存支付信息, 更新订单状态
    public ServerResponse alipayCallback(Map<String, String> params){
        Long orderNo = Long.parseLong(params.get("out_trade_no"));
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("订单不存在, 回到忽略");
        }
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()){ //订单已经付款, 属于重复验证
            return ServerResponse.createBySuccessMessage("订单已付款,重复验证");
        }
        if (Const.AlipayCallback.TRADE_SUATUS_TRADE_SUCCESS.equals(tradeStatus)){   //支付宝参数已付款,更新数据库
            //更新数据库
            order.setPaymentTime(DateTimeUtils.strToDate(params.get("gmt_payment")));
            order.setStatus(Const.OrderStatusEnum.PAID.getCode());
            orderMapper.updateByPrimaryKeySelective(order);
        }

        //支付信息的数据库创建, 等待支付状态也会保存支付信息, 支付宝支付状态tradeStatus有两种: TRADE_SUCCESS,等待支付;
        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);
        payInfoMapper.insert(payInfo);
        return ServerResponse.createBySuccess();
    }

    @Override
    public ServerResponse<Boolean> queryPaied(Integer  userId, Long orderId){
        Order order = orderMapper.selectOrderByUserIdOrderNo(userId, orderId);
        if (order == null){
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()){ //已支付
            return ServerResponse.createBySuccessData(true);
        }
        return ServerResponse.createBySuccessData(false);
    }
}
