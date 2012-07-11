package org.deri.rdf.browser.sparql;

import java.util.Collection;

import org.deri.rdf.browser.model.Facet;
import org.deri.rdf.browser.model.MainFilter;
import org.deri.rdf.browser.model.RdfDecoratedValue;

public class NaiveFederatedSparqlEngine{

	public String getFocusItemsSparql(String[] endpoints, MainFilter mainFilter, Collection<Facet> facets, int start, int length){
		StringBuilder builder = new StringBuilder("SELECT DISTINCT ?");
		builder.append(mainFilter.getVarname()).append(" WHERE{");
		builder.append(getWhereClause(endpoints, mainFilter, facets));
		builder.append("} ORDER BY ?").append(mainFilter.getVarname()).append(" OFFSET ").append(start).append(" LIMIT ").append(length);
		return builder.toString();
	}
	
	private String getWhereClause(String[] endpoints, MainFilter mainFilter, Collection<Facet> facets) {
		StringBuilder builder = new StringBuilder();
		for(String ep:endpoints){
			builder.append("{").append("SERVICE <").append(ep).append(">{");
			builder.append(mainFilter.getSparqlPattern());
			builder.append("}}UNION");
		}
		//get rid of the last UNION
		int lngth = builder.length();
		builder.delete(lngth-5, lngth);//5 = "UNION".length()
		
		for(Facet f:facets){
			String filter = getFacetFilter(endpoints, f,mainFilter.getFacetsVarname());
			builder.append(filter);
		}
		return builder.toString();
	}
	
	private String getFacetFilter(String[] endpoints, Facet f, String varname){
		if(!f.hasSelection()){
			return "";
		}
		StringBuilder filter = new StringBuilder();
		for(String ep:endpoints){
			filter.append("{").append("SERVICE <").append(ep).append(">{");
			for(RdfDecoratedValue v:f.getSelections()){
				filter.append("{?").append(varname).append(" ").append(f.getFilter().getPattern()).append(" ").append(v.inSparqlFilter(f.getVarname())).append(" .}").append("UNION");
			}
			if(f.missingValueSelected()){
				filter.append("{?").append(varname).append(" ").append(f.getFilter().getPattern()).append(" ?").append(f.getVarname()).append(DISAMBIGUATION_SUFFIX).append(" .}").append("UNION");
			}
			//get rid of the last UNION
			int lngth = filter.length();
			filter.delete(lngth-5, lngth);//5 = "UNION".length()
			filter.append("}}UNION");	
		}
		//get rid of the last UNION
		int lngth = filter.length();
		filter.delete(lngth-5, lngth);//5 = "UNION".length()

		if(f.missingValueSelected()){
			//left join
			filter.replace(0, 0, "OPTIONAL");
			filter.append("} FILTER(!bound(?").append(f.getVarname()).append(DISAMBIGUATION_SUFFIX).append(")) .");
		}
		return filter.toString();
	}
	
	private static final String DISAMBIGUATION_SUFFIX = "_v";
}
