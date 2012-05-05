package org.deri.rdf.browser.model;

public class AnnotatedString {
	public final int count;
	public String label;
	public final String id;
	public final int type;
	public AnnotatedString(int count, String id, int t){
		this.count = count;
		this.type = t;
		this.id = id;
	}

	public String getLabel() {
		return label==null?id:label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}

	
	@Override
	public boolean equals(Object obj) {
		if(! obj.getClass().equals(this.getClass())){
			return false;
		}
		AnnotatedString other = (AnnotatedString) obj;
		if(other.id==null || this.id==null){
			return false;
		}
		return this.id.equals(other.id);
	}

	@Override
	public int hashCode() {
		if(id==null) return 111;
		return id.hashCode();
	}


	public static final int RESOURCE = 1;
	public static final int LITERAL = 2;
}
