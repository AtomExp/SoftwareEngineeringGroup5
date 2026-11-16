package brilliantburger;
public class OrderService {private int nextOrderId=1;public Order createOrder(ShoppingCart cart){if(!validateCart(cart))throw new IllegalArgumentException("Empty cart");return new Order();}public boolean validateCart(ShoppingCart cart){return cart!=null&&cart.calculateTotal()>0;}}
