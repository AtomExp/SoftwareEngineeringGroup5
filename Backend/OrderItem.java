package brilliantburger;
import java.util.*;
public class OrderItem {private String itemName;private double unitPrice;private int quantity;private List<String> selectedOptions;public OrderItem(String name,double price,int qty,List<String> options){itemName=name;unitPrice=price;quantity=qty;selectedOptions=options;}public double getSubtotal(){return unitPrice*quantity;}}
