import com.gogotao.pojo.Order;
import com.gogotao.utils.DateTimeUtils;
import org.junit.Test;

import java.util.Date;

public class PaymentTimeTest {


    @Test
    public void test(){
        String str = "2020-01-15 20:45:33";
        Date date = DateTimeUtils.strToDate(str);
        Order order = new Order();
        order.setPaymentTime(date);
        System.out.println(order.toString());
    }
}
