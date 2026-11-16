package brilliantburger;
import java.util.*;
public class MenuItem {private int itemId;private String name;private double price;private String description;private List<String> customizations=new ArrayList<>();public MenuItem(int id,String name,double price,String description){itemId=id;this.name=name;this.price=price;this.description=description;}public int getId(){return itemId;}public double getPrice(){return price;}public List<String> getCustomizations(){return customizations;}}
