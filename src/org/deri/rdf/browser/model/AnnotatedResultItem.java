package org.deri.rdf.browser.model;

public class AnnotatedResultItem {

	private int count;
	public final String value;
	public final int type;
	//TODO change endpoints to LinkedList
	private String[] endpoints;
	
	public AnnotatedResultItem(int count, String v, int t, String[] eps){
		this.count = count;
		this.value = v;
		this.type = t;
		this.endpoints = eps;
	}
	
	public AnnotatedResultItem(String v,int type){
		this(0,v,type,new String[]{});
	}

	public AnnotatedResultItem(int count) {
		this(count,"missing value",NULL,new String[]{});
	}

	public void setCount(int c){
		this.count = c;
	}
	
	public int getCount(){
		return count;
	}
	
	public String[] getEndpoints(){
		return endpoints;
	}
	
	public void addEndpoint(String ep){
		String[] newEndpoints = new String[endpoints.length + 1];
		for(int i=0;i<endpoints.length;i++){
			newEndpoints[i] = endpoints[i];
		}
		newEndpoints[endpoints.length] = ep;
		this.endpoints = newEndpoints;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj.getClass().equals(this.getClass())){
			AnnotatedResultItem other = (AnnotatedResultItem) obj;
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

	@Override
	public String toString() {
		return value + "(" + count + ")";
	}



	public static final int RESOURCE = 1;
	public static final int LITERAL = 2;
	public static final int NULL = 3;
}
