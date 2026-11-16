package brilliantburger;
import java.util.*;
public class Order {private int orderId;private List<OrderItem> items=new ArrayList<>();private double total;private String status="PENDING";public double calculateTotal(){total=items.stream().mapToDouble(OrderItem::getSubtotal).sum();return total;}public void confirmOrder(){status="CONFIRMED";}}
