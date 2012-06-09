package org.deri.rdf.browser.model;

public class AnnotatedString {
	private int count;
	public final String value;
	public final int type;
	public AnnotatedString(int count, String v, int t){
		this.count = count;
		this.value = v;
		this.type = t;
	}

	public void setCount(int c){
		this.count = c;
	}
	
	public int getCount(){
		return count;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj.getClass().equals(this.getClass())){
			AnnotatedString other = (AnnotatedString) obj;
			if(this.value==null||other.value==null){
				return false;
			}
			return this.value.equals(other.value) && this.type==other.type;
		}
		
		return false;
	}
	@Override
	public int hashCode() {
		if(this.value==null){return type;}
		return this.value.hashCode() * this.type;
	}


	public static final int RESOURCE = 1;
	public static final int LITERAL = 2;
}
