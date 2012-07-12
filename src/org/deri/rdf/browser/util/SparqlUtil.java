package org.deri.rdf.browser.util;

public class SparqlUtil {

	public static void getRidOfLastUnion(StringBuilder builder){
		int lngth = builder.length();
		builder.delete(lngth-5, lngth);//5 = "UNION".length()
	}
}
