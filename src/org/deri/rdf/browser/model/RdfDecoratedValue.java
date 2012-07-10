package org.deri.rdf.browser.model;

public class RdfDecoratedValue {

	private final String value;
	private final byte type;
	public RdfDecoratedValue(String v, byte type){
		this.type = type;
		this.value = v;
	}
	
	public String getValue(){
		return value;
	}
	
	public boolean isLiteral(){
		return this.type == LITERAL;
	}

	public String inSparqlFilter(String varname) {
		if(type==RESOURCE){
			return "<" + value + ">";
		}else{
			return "?" + varname + ". FILTER(str(?" + varname + ")=\"" + value + "\")";
		}
	}
	
	public byte getType(){
		return type;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj==null || ! obj.getClass().equals(this.getClass())){
			return false;
		}
		RdfDecoratedValue other = (RdfDecoratedValue) obj;
		return this.value.equals(other.getValue()) && this.type==other.type;
	}

	@Override
	public int hashCode() {
		return this.value.hashCode() * this.type;
	}
	
	@Override
	public String toString(){
		return value;
	}

	public static final byte RESOURCE =1;
	public static final byte LITERAL = 3;
	public static final byte NULL =7;
}
