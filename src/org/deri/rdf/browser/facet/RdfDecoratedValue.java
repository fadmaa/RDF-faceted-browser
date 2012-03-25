package org.deri.rdf.browser.facet;

public class RdfDecoratedValue {

	protected final Object value;
	protected final boolean literal;
	
	public RdfDecoratedValue(Object value, boolean literal) {
		this.value = value;
		this.literal = literal;
	}
	
	public boolean isLiteral(){
		return literal;
	}
	public Object getValue(){
		return value;
	}
}
