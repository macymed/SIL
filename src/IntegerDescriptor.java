
public class IntegerDescriptor {
	public String varName;
	public int value;
	public static final String type = "INTEGER";
	
	public IntegerDescriptor(String varName, int value){
		this.varName = varName;
		this.value = value;
	}
	
	public String toString(){
		return varName + ": " + value;
	}

}
