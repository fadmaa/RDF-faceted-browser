package org.deri.rdf.browser.model;

public class RdfDecoratedValue {

	protected final Object value;
	protected final int type;

	public RdfDecoratedValue(Object value, int type) {
		this.value = value;
		this.type = type;
	}

	public Object getValue(){
		return value;
	}
	
	public int getType(){
		return type;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this.getClass().equals(obj.getClass())){
			RdfDecoratedValue other = (RdfDecoratedValue) obj;
			if(this.value==null||other.value==null){
				return false;
			}
			return this.value.equals(other.value) && this.type==other.type;
		}else{
			return false;
		}
	}

	@Override
	public int hashCode() {
		if(this.value==null){return type;}
		return this.value.hashCode() * this.type;
	}

	
	@Override
	public String toString() {
		return value.toString();
	}


	public static final int RESOURCE = 1;
	public static final int LITERAL = 2;
	public static final int NULL = 3;
}