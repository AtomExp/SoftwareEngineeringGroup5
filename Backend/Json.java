package brilliantburger;
import java.util.*;
/** Minimal JSON handling for this class project; no third-party JSON library required. */
public final class Json {
 private Json(){}
 public static String stringify(Object v){
  if(v==null)return "null";
  if(v instanceof String s)return "\""+s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r").replace("\t","\\t")+"\"";
  if(v instanceof Number || v instanceof Boolean)return v.toString();
  if(v instanceof Map<?,?> m){List<String> a=new ArrayList<>();for(var e:m.entrySet())a.add(stringify(String.valueOf(e.getKey()))+":"+stringify(e.getValue()));return "{"+String.join(",",a)+"}";}
  if(v instanceof Iterable<?> it){List<String>a=new ArrayList<>();for(Object o:it)a.add(stringify(o));return "["+String.join(",",a)+"]";}
  throw new IllegalArgumentException("Unsupported JSON type: "+v.getClass());
 }
 public static Object parse(String s){Parser p=new Parser(s);Object x=p.value();p.ws();if(p.i!=s.length())throw new IllegalArgumentException("Trailing JSON input");return x;}
 @SuppressWarnings("unchecked") public static Map<String,Object> object(String s){Object o=parse(s);if(!(o instanceof Map))throw new IllegalArgumentException("Expected JSON object");return (Map<String,Object>)o;}
 private static class Parser{final String s;int i;Parser(String s){this.s=s;}void ws(){while(i<s.length()&&Character.isWhitespace(s.charAt(i)))i++;}char peek(){ws();if(i>=s.length())throw new IllegalArgumentException("Unexpected end of JSON");return s.charAt(i);}void eat(char c){if(peek()!=c)throw new IllegalArgumentException("Expected "+c);i++;}
  Object value(){char c=peek();if(c=='"')return string();if(c=='{')return obj();if(c=='[')return arr();if(c=='t'&&s.startsWith("true",i)){i+=4;return true;}if(c=='f'&&s.startsWith("false",i)){i+=5;return false;}if(c=='n'&&s.startsWith("null",i)){i+=4;return null;}if(c=='-'||Character.isDigit(c))return number();throw new IllegalArgumentException("Invalid JSON value");}
  String string(){eat('"');StringBuilder b=new StringBuilder();while(i<s.length()){char c=s.charAt(i++);if(c=='"')return b.toString();if(c=='\\'){if(i>=s.length())throw new IllegalArgumentException("Bad escape");char e=s.charAt(i++);switch(e){case '"'->b.append('"');case '\\'->b.append('\\');case '/'->b.append('/');case 'n'->b.append('\n');case 'r'->b.append('\r');case 't'->b.append('\t');case 'b'->b.append('\b');case 'f'->b.append('\f');case 'u'->{if(i+4>s.length())throw new IllegalArgumentException("Bad unicode");b.append((char)Integer.parseInt(s.substring(i,i+4),16));i+=4;}default->throw new IllegalArgumentException("Bad escape");}}else b.append(c);}throw new IllegalArgumentException("Unterminated JSON string");}
  Map<String,Object> obj(){eat('{');Map<String,Object> m=new LinkedHashMap<>();if(peek()=='}'){i++;return m;}while(true){String k=string();eat(':');m.put(k,value());char c=peek();i++;if(c=='}')return m;if(c!=',')throw new IllegalArgumentException("Expected comma");}}
  List<Object> arr(){eat('[');List<Object>a=new ArrayList<>();if(peek()==']'){i++;return a;}while(true){a.add(value());char c=peek();i++;if(c==']')return a;if(c!=',')throw new IllegalArgumentException("Expected comma");}}
  Number number(){int start=i;if(s.charAt(i)=='-')i++;while(i<s.length()&&Character.isDigit(s.charAt(i)))i++;if(i<s.length()&&s.charAt(i)=='.'){i++;while(i<s.length()&&Character.isDigit(s.charAt(i)))i++;}if(i<s.length()&&(s.charAt(i)=='e'||s.charAt(i)=='E')){i++;if(i<s.length()&&(s.charAt(i)=='+'||s.charAt(i)=='-'))i++;while(i<s.length()&&Character.isDigit(s.charAt(i)))i++;}String n=s.substring(start,i);return n.contains(".")||n.contains("e")||n.contains("E")?Double.valueOf(n):Long.valueOf(n);}
 }
}
