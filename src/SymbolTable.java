import java.util.Map;
import java.util.HashMap;

public class SymbolTable {
	public static Map<String, IntegerDescriptor> integers = new HashMap<String, IntegerDescriptor>();
	
	public SymbolTable(){
		
	}
	
	public void add(IntegerDescriptor id){
		integers.put(id.varName, id);
	}
	
	public void update(IntegerDescriptor id, int value){
		integers.remove(id);
		integers.put(id.varName, new IntegerDescriptor(id.varName, value));
	}
	
	public String toString(){
		return integers.toString();
	}
}
