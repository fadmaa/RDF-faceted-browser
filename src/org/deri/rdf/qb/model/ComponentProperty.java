package org.deri.rdf.qb.model;

public class ComponentProperty {

	protected final String uri;
	protected String label;
	protected String value;
	public ComponentProperty(String uri){
		this.uri=uri;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getUri() {
		return uri;
	}
}
