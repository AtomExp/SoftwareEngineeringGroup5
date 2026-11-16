package brilliantburger;
import java.util.*;
public class Menu {private List<MenuItem> items=new ArrayList<>();public List<MenuItem> getItems(){return items;}public MenuItem findItem(int id){return items.stream().filter(x->x.getId()==id).findFirst().orElse(null);}}
