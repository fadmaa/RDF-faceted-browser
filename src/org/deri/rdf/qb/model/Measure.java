package org.deri.rdf.qb.model;



public class Measure extends ComponentProperty{

	public Measure(String uri) {
		super(uri);
		
	}

	public Measure(String measureuri, String measureLabel,String measureValue) {
		super(measureuri);
		this.label=measureLabel;
		this.value=measureValue;
	}

}
