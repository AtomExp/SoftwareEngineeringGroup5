package brilliantburger;
import java.util.*;
public class ShoppingCart {private int cartId;private List<CartItem> items=new ArrayList<>();public void addItem(CartItem item){items.add(item);}public void removeItem(int itemId){items.removeIf(x->x.getItemId()==itemId);}public void updateQuantity(int itemId,int qty){for(CartItem x:items)if(x.getItemId()==itemId)x.setQuantity(qty);}public double calculateTotal(){return items.stream().mapToDouble(CartItem::getSubtotal).sum();}}
