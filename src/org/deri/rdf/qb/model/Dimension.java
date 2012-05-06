package org.deri.rdf.qb.model;


public class Dimension extends ComponentProperty{

	public Dimension(String uri) {
		super(uri);
		
	}

	public Dimension(String uri, String dimLabel, String dimValue) {
		super(uri);
		this.label=dimLabel;
		this.value=dimValue;
	}

}
