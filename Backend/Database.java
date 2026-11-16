package brilliantburger;
import java.sql.*;import java.nio.file.*;import java.util.*;
public final class Database {
 private final String url;
 public Database(Path file) throws Exception{Files.createDirectories(file.toAbsolutePath().getParent());url="jdbc:sqlite:"+file.toAbsolutePath();init();}
 public Connection connect() throws SQLException{Connection c=DriverManager.getConnection(url);try(Statement s=c.createStatement()){s.execute("PRAGMA foreign_keys=ON");s.execute("PRAGMA busy_timeout=5000");}return c;}
 private void init() throws SQLException{
  try(Connection c=connect();Statement s=c.createStatement()){
   s.executeUpdate("CREATE TABLE IF NOT EXISTS menu_items(id INTEGER PRIMARY KEY,name TEXT NOT NULL,description TEXT NOT NULL,price_cents INTEGER NOT NULL,calories INTEGER NOT NULL,category TEXT NOT NULL,image TEXT NOT NULL,tags_json TEXT NOT NULL,ingredients_json TEXT NOT NULL)");
   s.executeUpdate("CREATE TABLE IF NOT EXISTS orders(id INTEGER PRIMARY KEY AUTOINCREMENT,created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,subtotal_cents INTEGER NOT NULL,tax_cents INTEGER NOT NULL,total_cents INTEGER NOT NULL,status TEXT NOT NULL)");
   s.executeUpdate("CREATE TABLE IF NOT EXISTS order_items(id INTEGER PRIMARY KEY AUTOINCREMENT,order_id INTEGER NOT NULL REFERENCES orders(id),menu_item_id INTEGER NOT NULL REFERENCES menu_items(id),name TEXT NOT NULL,unit_price_cents INTEGER NOT NULL,quantity INTEGER NOT NULL,selected_options_json TEXT NOT NULL)");
   try(ResultSet r=s.executeQuery("SELECT COUNT(*) FROM menu_items")){if(r.next()&&r.getInt(1)>0)return;}
  }
  String[][] seed={
   {"Brilliant Burger","Beef patty, lettuce, tomato and house sauce","699","650","Burgers","burger,meals","Beef patty,Lettuce,Tomato,House sauce"},
   {"Double Brilliant Burger","Two beef patties with cheese and house sauce","899","890","Burgers","burger,meals","Beef patties,Cheese,House sauce"},
   {"Crispy Chicken Sandwich","Crispy chicken, pickles and mayo","749","720","Chicken","chicken,meals","Chicken,Pickles,Mayo"},
   {"Chicken Nuggets","Six crispy chicken nuggets","449","420","Chicken","chicken,sides","Chicken,Breading"},
   {"French Fries","Golden crispy fries","299","320","Sides","side","Potatoes,Salt"},
   {"Onion Rings","Crispy battered onion rings","349","390","Sides","side","Onion,Batter"},
   {"Chocolate Shake","Creamy chocolate milkshake","429","510","Drinks","drink,dessert","Milk,Chocolate"},
   {"Cola","Refreshing fountain cola","199","180","Drinks","drink","Carbonated water,Cola syrup"},
   {"Apple Pie","Warm apple pie","299","270","Desserts","dessert","Apple,Cinnamon,Pastry"},
   {"Vanilla Sundae","Vanilla soft serve with chocolate topping","329","340","Desserts","dessert","Milk,Vanilla,Chocolate"}
  };
  try(Connection c=connect();PreparedStatement p=c.prepareStatement("INSERT INTO menu_items(id,name,description,price_cents,calories,category,image,tags_json,ingredients_json) VALUES (?,?,?,?,?,?,?,?,?)")){
   for(int i=0;i<seed.length;i++){String[] x=seed[i];p.setInt(1,i+1);p.setString(2,x[0]);p.setString(3,x[1]);p.setInt(4,Integer.parseInt(x[2]));p.setInt(5,Integer.parseInt(x[3]));p.setString(6,x[4]);p.setString(7,"BrilliantBurgerLogo.png");p.setString(8,Json.stringify(Arrays.asList(x[5].split(","))));p.setString(9,Json.stringify(Arrays.asList(x[6].split(","))));p.executeUpdate();}
  }
 }
 public List<Map<String,Object>> menu() throws SQLException{List<Map<String,Object>> out=new ArrayList<>();try(Connection c=connect();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT * FROM menu_items ORDER BY id")){while(r.next())out.add(item(r));}return out;}
 private Map<String,Object> item(ResultSet r)throws SQLException {Map<String,Object> m=new LinkedHashMap<>();m.put("id",r.getInt("id"));m.put("name",r.getString("name"));m.put("description",r.getString("description"));m.put("price",r.getInt("price_cents")/100.0);m.put("caloriesPerServing",r.getInt("calories"));m.put("category",r.getString("category"));m.put("image",r.getString("image"));m.put("tags",Json.parse(r.getString("tags_json")));m.put("ingredients",Json.parse(r.getString("ingredients_json")));return m;}
 public Map<String,Object> menuItem(int id)throws SQLException{try(Connection c=connect();PreparedStatement p=c.prepareStatement("SELECT * FROM menu_items WHERE id=?")){p.setInt(1,id);try(ResultSet r=p.executeQuery()){return r.next()?item(r):null;}}}
 private record Entry(int id,int qty,List<String> options){}
 public Map<String,Object> createOrder(Map<String,Object> request)throws Exception{
  Object raw=request.get("items");if(!(raw instanceof List<?> list)||list.isEmpty()||list.size()>100)throw new IllegalArgumentException("Order needs 1-100 items");
  List<Entry> entries=new ArrayList<>();for(Object o:list){if(!(o instanceof Map<?,?> m))throw new IllegalArgumentException("Invalid item");int id=integer(m.get("id"));int qty=integer(m.get("quantity"));if(qty<1||qty>99)throw new IllegalArgumentException("Quantity must be 1-99");List<String> opts=new ArrayList<>();Object x=m.get("selectedOptions");if(x instanceof List<?> options){if(options.size()>20)throw new IllegalArgumentException("Too many options");for(Object v:options){if(!(v instanceof String str)||str.length()>100)throw new IllegalArgumentException("Invalid option");opts.add(str);}}entries.add(new Entry(id,qty,opts));}
  try(Connection c=connect()){
   c.setAutoCommit(false);try{
    int subtotal=0;List<Map<String,Object>> details=new ArrayList<>();
    try(PreparedStatement q=c.prepareStatement("SELECT name,price_cents FROM menu_items WHERE id=?")){
     for(Entry e:entries){q.setInt(1,e.id());try(ResultSet r=q.executeQuery()){if(!r.next())throw new IllegalArgumentException("Unknown menu item: "+e.id());int price=r.getInt("price_cents");subtotal=Math.addExact(subtotal,Math.multiplyExact(price,e.qty()));Map<String,Object> d=new LinkedHashMap<>();d.put("id",e.id());d.put("name",r.getString("name"));d.put("priceCents",price);d.put("quantity",e.qty());d.put("selectedOptions",e.options());details.add(d);}}}
    // Demonstration rate only, not a representation of actual local sales tax.
    int tax=Math.toIntExact(Math.round(subtotal*0.05));int total=Math.addExact(subtotal,tax);long id;
    try(PreparedStatement p=c.prepareStatement("INSERT INTO orders(subtotal_cents,tax_cents,total_cents,status) VALUES (?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){p.setInt(1,subtotal);p.setInt(2,tax);p.setInt(3,total);p.setString(4,"CONFIRMED");p.executeUpdate();try(ResultSet r=p.getGeneratedKeys()){if(!r.next())throw new SQLException("Missing order ID");id=r.getLong(1);}}
    try(PreparedStatement p=c.prepareStatement("INSERT INTO order_items(order_id,menu_item_id,name,unit_price_cents,quantity,selected_options_json) VALUES (?,?,?,?,?,?)")){for(Map<String,Object> d:details){p.setLong(1,id);p.setInt(2,(int)d.get("id"));p.setString(3,(String)d.get("name"));p.setInt(4,(int)d.get("priceCents"));p.setInt(5,(int)d.get("quantity"));p.setString(6,Json.stringify(d.get("selectedOptions")));p.executeUpdate();}}
    c.commit();return order(id);
   }catch(Exception e){c.rollback();throw e;}
  }
 }
 private static int integer(Object x){if(!(x instanceof Number n)||n.doubleValue()!=Math.rint(n.doubleValue()))throw new IllegalArgumentException("ID and quantity must be integers");return n.intValue();}
 public Map<String,Object> order(long id)throws SQLException{
  try(Connection c=connect();PreparedStatement p=c.prepareStatement("SELECT * FROM orders WHERE id=?")){p.setLong(1,id);try(ResultSet r=p.executeQuery()){if(!r.next())return null;Map<String,Object> out=new LinkedHashMap<>();out.put("orderId",r.getLong("id"));out.put("createdAt",r.getString("created_at"));out.put("status",r.getString("status"));out.put("subtotal",r.getInt("subtotal_cents")/100.0);out.put("taxRate",5.0);out.put("tax",r.getInt("tax_cents")/100.0);out.put("total",r.getInt("total_cents")/100.0);List<Map<String,Object>> items=new ArrayList<>();try(PreparedStatement q=c.prepareStatement("SELECT * FROM order_items WHERE order_id=?")){q.setLong(1,id);try(ResultSet x=q.executeQuery()){while(x.next()){Map<String,Object> m=new LinkedHashMap<>();m.put("id",x.getInt("menu_item_id"));m.put("name",x.getString("name"));m.put("price",x.getInt("unit_price_cents")/100.0);m.put("quantity",x.getInt("quantity"));m.put("selectedOptions",Json.parse(x.getString("selected_options_json")));items.add(m);}}}out.put("items",items);return out;}}
  }
}
