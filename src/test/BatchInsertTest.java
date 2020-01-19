import com.gogotao.dao.OrderItemMapper;
import com.gogotao.pojo.OrderItem;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;

public class BatchInsertTest {

    @Test
    public void test(){
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) applicationContext.getBean("sqlSessionFactory");
        OrderItemMapper orderItemMapper = sqlSessionFactory.openSession().getMapper(OrderItemMapper.class);
        List<OrderItem> orderItemList = new ArrayList<>();
        OrderItem orderItem = new OrderItem();
        orderItem.setUserId(222);
        OrderItem orderItem1 = new OrderItem();
        orderItem1.setUserId(333);
        OrderItem orderItem2 = new OrderItem();
        orderItem2.setUserId(444);
        orderItemList.add(orderItem);
        orderItemList.add(orderItem1);
        orderItemList.add(orderItem2);
        System.out.println("orderItemList : " + orderItemList);
        orderItemMapper.batchInsert(orderItemList);
    }
}
