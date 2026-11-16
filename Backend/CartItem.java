package brilliantburger;
import java.util.*;
public class CartItem {private MenuItem menuItem;private int quantity;private List<String> selectedOptions;public CartItem(MenuItem item,int qty,List<String> options){menuItem=item;quantity=qty;selectedOptions=options;}public int getItemId(){return menuItem.getId();}public int getQuantity(){return quantity;}public void setQuantity(int qty){quantity=qty;}public double getSubtotal(){return menuItem.getPrice()*quantity;}}
