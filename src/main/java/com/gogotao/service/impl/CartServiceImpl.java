package com.gogotao.service.impl;

import com.gogotao.common.Const;
import com.gogotao.common.ResponseCode;
import com.gogotao.common.ServerResponse;
import com.gogotao.dao.CartMapper;
import com.gogotao.dao.ProductMapper;
import com.gogotao.pojo.Cart;
import com.gogotao.pojo.Product;
import com.gogotao.service.ICartService;
import com.gogotao.utils.BigDecimalUtil;
import com.gogotao.utils.PropertiesUtil;
import com.gogotao.vo.CartProductVo;
import com.gogotao.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service("iCartService")
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;

    @Override
    public ServerResponse<CartVo> list(Integer userId) {
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccessData(cartVo);
    }

    @Override
    public ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count){
        if (productId == null || count == null){
            return ServerResponse.createByErrorCode(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId);
        if (cart == null){  //购物车不存在该商品
            Cart cartItem = new Cart();
            cartItem.setQuantity(count);
            cartItem.setChecked(Const.Cart.CHECKED);
            cartItem.setUserId(userId);
            cartItem.setProductId(productId);
            cartMapper.insert(cartItem);
        }else{  //购物车已经存在该商品, 更新该商品的数量
            count = cart.getQuantity() + count;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        return this.list(userId);
    }

    @Override
    public ServerResponse<CartVo> update(Integer userId, Integer productId, Integer count){
        if (productId == null || count == null){
            return ServerResponse.createByErrorCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId);
        if (cart != null){
            cart.setQuantity(count);
        }
        cartMapper.updateByPrimaryKeySelective(cart);
        return this.list(userId);
    }

    @Override
    public ServerResponse<CartVo> deleteProducts(Integer userId, String productIds){
        if (productIds == null){
            return ServerResponse.createByErrorCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String[] productStrs = productIds.split(",");
        List<String> productList = new ArrayList<>();
        for (String strItem : productStrs){
            productList.add(strItem);
        }
        cartMapper.deleteByUserIdProductIds(userId, productList);
        return this.list(userId);
    }

    @Override
    public ServerResponse<CartVo> selectOrUnSelect(Integer userId, Integer productId, Integer checked){
        if (checked == null){
            return ServerResponse.createByErrorCode(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.updateCheckedByUserIdProductId(userId, productId, checked);
        return this.list(userId);
    }

    @Override
    public ServerResponse<Integer> getCartProductCount(Integer userId){
        if (userId == null){
            return ServerResponse.createBySuccessData(0);
        }
        return ServerResponse.createBySuccessData(cartMapper.selectProductCountByUserId(userId));
    }

    private CartVo getCartVoLimit(Integer userId){
        CartVo cartVo = new CartVo();
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        List<CartProductVo> cartProductVoList = new ArrayList<>(cartList.size());
        BigDecimal cartTotalPrice = new BigDecimal("0");

        if (cartList != null){
            for (Cart cartItem : cartList){
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(cartItem.getUserId());
                cartProductVo.setProductId(cartItem.getProductId());
                cartProductVo.setQuantity(cartItem.getQuantity());
                cartProductVo.setProductChecked(cartItem.getChecked());
                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                if (product != null){
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());
                    cartProductVo.setProductStatus(product.getStatus());
                    if (product.getStock() >= cartItem.getQuantity()){  //判断库存与需要购买产品的数量,超过:quantity设置为库存,并且修正数据库产品数量
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    }else{  //设置quantity为库存, 修正数据库产品数量
                        cartProductVo.setQuantity(product.getStock());
                        Cart newCart = new Cart();
                        newCart.setId(cartItem.getId());
                        newCart.setQuantity(product.getStock());
                        cartMapper.updateByPrimaryKeySelective(newCart);
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                    }
                    //计算该商品总价
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(cartItem.getQuantity().doubleValue(), product.getPrice().doubleValue()));
                }
                if (cartItem.getChecked() == Const.Cart.CHECKED){ //如果选中累加商品价格,作为购物车总价格
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(), cartProductVo.getProductTotalPrice().doubleValue());
                }
                cartProductVoList.add(cartProductVo);
            } //所有的cartItem(每个商品订单项)处理完毕
        }
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setAllChecked(getAllChecked(userId));
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return cartVo;
    }
    private boolean getAllChecked(Integer userId){  //如果没有 没有被选中(即全选中)的该用户订单项赶回true
        return cartMapper.selectCheckedByUserId(userId) == 0;
    }
}
