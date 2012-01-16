package org.deri.rdf.browser.model;

public class AnnotatedString {
	public final int count;
	public final String value;
	public final int type;
	public AnnotatedString(int count, String v, int t){
		this.count = count;
		this.value = v;
		this.type = t;
	}

	public static final int RESOURCE = 1;
	public static final int LITERAL = 2;
}
