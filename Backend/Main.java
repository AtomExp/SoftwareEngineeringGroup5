package brilliantburger;
import com.sun.net.httpserver.*;import java.io.*;import java.net.*;import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.util.*;
public class Main {
 public static void main(String[] args)throws Exception{
  Path base=Paths.get("").toAbsolutePath();Path frontend=base.resolve("frontend").normalize();Database db=new Database(base.resolve("data/brilliantburger.db"));
  HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",8080),0);
  server.createContext("/",x->{try{handle(x,db,frontend);}catch(IllegalArgumentException e){json(x,400,Map.of("error",e.getMessage()));}catch(Exception e){e.printStackTrace();json(x,500,Map.of("error","Internal server error"));}});
  server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(8));server.start();System.out.println("Brilliant Burger at http://localhost:8080 (SQLite: data/brilliantburger.db)");
 }
 private static void handle(HttpExchange x,Database db,Path frontend)throws Exception{
  String path=x.getRequestURI().getPath(),method=x.getRequestMethod();
  if(path.startsWith("/api/")){
   x.getResponseHeaders().set("Cache-Control","no-store");
   if(method.equals("GET")&&path.equals("/api/menu")){json(x,200,Map.of("recipes",db.menu()));return;}
   if(method.equals("GET")&&path.matches("/api/menu/[0-9]+")){Map<String,Object> item=db.menuItem(Integer.parseInt(path.substring(10)));json(x,item==null?404:200,item==null?Map.of("error","Not found"):item);return;}
   if(method.equals("POST")&&path.equals("/api/orders")){
    String raw=new String(x.getRequestBody().readNBytes(100_001),StandardCharsets.UTF_8);if(raw.length()>100_000)throw new IllegalArgumentException("Request too large");
    Map<String,Object> order=db.createOrder(Json.object(raw));json(x,201,order);return;
   }
   if(method.equals("GET")&&path.matches("/api/orders/[0-9]+")){Map<String,Object> order=db.order(Long.parseLong(path.substring(12)));json(x,order==null?404:200,order==null?Map.of("error","Not found"):order);return;}
   if(method.equals("GET")&&path.equals("/api/health")){json(x,200,Map.of("status","ok"));return;}
   json(x,404,Map.of("error","Unknown endpoint"));return;
  }
  if(!method.equals("GET")){json(x,405,Map.of("error","Method not allowed"));return;}
  if(path.equals("/"))path="/index.html";
  Path file=frontend.resolve(path.substring(1)).normalize();if(!file.startsWith(frontend)||!Files.isRegularFile(file)){text(x,404,"File not found","text/plain");return;}
  String type=path.endsWith(".html")?"text/html":path.endsWith(".js")?"text/javascript":path.endsWith(".css")?"text/css":path.endsWith(".png")?"image/png":"application/octet-stream";
  byte[] bytes=Files.readAllBytes(file);x.getResponseHeaders().set("Content-Type",type+"; charset=utf-8");x.sendResponseHeaders(200,bytes.length);try(OutputStream out=x.getResponseBody()){out.write(bytes);}
 }
 private static void json(HttpExchange x,int code,Object obj)throws IOException{text(x,code,Json.stringify(obj),"application/json");}
 private static void text(HttpExchange x,int code,String body,String type)throws IOException{byte[] b=body.getBytes(StandardCharsets.UTF_8);x.getResponseHeaders().set("Content-Type",type+"; charset=utf-8");x.sendResponseHeaders(code,b.length);try(OutputStream out=x.getResponseBody()){out.write(b);}}
}
