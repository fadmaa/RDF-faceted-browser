package org.deri.rdf.browser.sparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.deri.rdf.browser.facet.RdfFacet;
import org.deri.rdf.browser.model.AnnotatedString;
import org.deri.rdf.browser.model.RdfResource;
import org.deri.rdf.browser.util.ParsingUtilities;

import com.google.common.collect.SetMultimap;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class QueryEngine {

	public Collection<RdfResource> getResources(String sparqlEndpoint,String filter, SetMultimap<RdfFacet, String> filters, int offset, int limit){
		//get the resources
		//TODO support blank node
		//FIXME support blank node. currently, silently ignored
		String sparql = "SELECT DISTINCT ?x WHERE { ?x " + filter + getFilter("x",filter,filters) + " FILTER (isIRI(?x)). } ORDER BY ?x LIMIT " + limit + " OFFSET " + offset;
		Query query = QueryFactory.create(sparql);
		QueryExecution qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, query);
		ResultSet results = qe.execSelect();
		Set<String> resources = new HashSet<String>(limit);
		Map<String,RdfResource> resourcesMap = new HashMap<String, RdfResource>();
		while(results.hasNext()){
			QuerySolution sol = results.next();
			//DOC limitation: resources can't be blank nodes
			String uri = sol.getResource("x").getURI();
			resources.add(uri);
			resourcesMap.put(uri, new RdfResource(uri));
		}
		
		if(resources.isEmpty()){
			return new HashSet<RdfResource>();
		}
		//get properties of resources
		//TODO make this configurable.... currently get *all* properties
		sparql = "SELECT ?x ?p ?o WHERE { ?x ?p ?o. ?x " + filter + getOrClause("x",resources) + " }";
		query = QueryFactory.create(sparql);
		qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, query);
		results = qe.execSelect();
		while(results.hasNext()){
			QuerySolution sol = results.next();
			resourcesMap.get(sol.getResource("x").getURI()).getProperties().put(sol.getResource("p").getURI(),getString(sol.get("o")));
		}
		return resourcesMap.values();
	}
	
	public int getResourcesCount(String sparqlEndpoint, String filter, SetMultimap<RdfFacet, String> filters){
		String sparql = "SELECT (COUNT(DISTINCT(?x)) AS ?count) WHERE { ?x " + filter + getFilter("x",filter,filters) + " FILTER (isIRI(?x)). } ";
		Query query = QueryFactory.create(sparql, Syntax.syntaxSPARQL_11);
		QueryExecution qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, query);
		ResultSet results = qe.execSelect();
		if(results.hasNext()){
			QuerySolution sol = results.next();
			return sol.getLiteral("count").getInt();
		}else{
			return 0;
		}
	}
	
	public List<AnnotatedString> getPropertiesWithCount(String sparqlEndpoint, String property, String filter, SetMultimap<RdfFacet, String> filters){
		String sparql = "SELECT DISTINCT ?v (COUNT(DISTINCT(?x)) AS ?count) WHERE{ ?x " + property + " ?v. ?x " + filter + getFilter("x",filter,filters) + " } GROUP BY (?v)";
		Query query = QueryFactory.create(sparql, Syntax.syntaxSPARQL_11);
		QueryExecution qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, query);
		ResultSet results = qe.execSelect();
		List<AnnotatedString> values = new ArrayList<AnnotatedString>(); 
		while(results.hasNext()){
			QuerySolution sol = results.next();
			RDFNode node = sol.get("v");
			String v = getString(node);
			if(v==null){
				continue;
			}
			values.add(new AnnotatedString(sol.getLiteral("count").getInt(),v,node.canAs(Literal.class)?AnnotatedString.LITERAL:AnnotatedString.RESOURCE));
		}
		//now see if there are blank values
		
		sparql = "SELECT (COUNT(DISTINCT(?x)) AS ?count) WHERE{ ?x " + filter + "OPTIONAL{ ?x " + property + " ?v.}. FILTER(!bound(?v)). " + getFilter("x",filter,filters) + " }";
		query = QueryFactory.create(sparql, Syntax.syntaxSPARQL_11);
		qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, query);
		results = qe.execSelect();
		if(results.hasNext()){
			QuerySolution sol = results.next();
			int count = sol.getLiteral("count").getInt();
			if(count>0){
				values.add(new AnnotatedString(count, null,0));
			}
		}
		return values;
	}
	
	protected String getFilter(String varname, String mainSelector, SetMultimap<RdfFacet, String> propertyFilters){
		if(propertyFilters.isEmpty()){
			return "";
		}
		StringBuilder builder = new StringBuilder("");
		int j = 1;
		for(Entry<RdfFacet, Collection<String>> pv:propertyFilters.asMap().entrySet()){
			Collection<String> values = pv.getValue();
			builder.append("{");
			//iterate through values
			int i = 1;
			Iterator<String> valIter = values.iterator();
			String auxVarname = "?" + ParsingUtilities.varname(pv.getKey().getName()) + "_vvv";
			while(i<values.size()){
				String val = valIter.next();
				if(val==null){
					//blank selected
					//FIXME if two blanks are selected we are screwed... variable name will be used twice
					String localVarname = ParsingUtilities.varname(pv.getKey().getName()) + varname;
					builder.append("{").append("?").append(localVarname).append(" ").append(mainSelector).append(" OPTIONAL{ ").append(pv.getKey().getResourceSparqlSelector(localVarname, auxVarname)).append(" . } FILTER(! bound(").append(auxVarname).append(") ) } UNION ");
				}else{
					
					//literals should be handled different than resources
					if(val.startsWith("<")){
						builder.append("{ ").append(pv.getKey().getResourceSparqlSelector(varname,val)).append(" . } UNION ");
					}else{
						builder.append("{ ").append(pv.getKey().getLiteralSparqlSelector(varname,varname+ i + "_"+ j+ "_lit",val)).append(" } UNION ");
					}
				}
				i+=1;
			}
			//the last value
			String val = valIter.next();
			if(val==null){
				//blank selected
				//FIXME if two blanks are selected we are screwed... variable name will be used twice
				String localVarname = ParsingUtilities.varname(pv.getKey().getName()) + varname;
				builder.append("{").append("?").append(localVarname).append(" ").append(mainSelector).append(" OPTIONAL{ ").append(pv.getKey().getResourceSparqlSelector(localVarname, auxVarname)).append(" . } FILTER(! bound(").append(auxVarname).append(") ) } ");
			}else{
				//literals should be handled different than resources
				if(val.startsWith("<")){
					builder.append("{ ").append(pv.getKey().getResourceSparqlSelector(varname,val)).append(" . } ");
				}else{
					builder.append("{ ").append(pv.getKey().getLiteralSparqlSelector(varname,varname+ i + "_"+ j+ "_lit",val)).append(") } ");
				}
			}
			builder.append("}");
			j +=1;
		}
		return builder.toString();
	}
	
	protected String getOrClause(String varname, Set<String> resources) {
		if(resources.isEmpty()){
			return "";
		}
		StringBuilder builder = new StringBuilder("FILTER ( ");
		for(String r:resources){
			builder.append("?").append(varname).append(" = <").append(r).append("> || ");
		}
		//get rid of the last ||
		return builder.substring(0,builder.length()-4) + ").";
	}
	
	protected String getString(RDFNode node){
		if(node==null) return null;
		if(node.canAs(Literal.class)){
			return node.asLiteral().getString();
		}else{
			return node.asResource().getURI();
		}
	}
	
	protected String getVarnames(int numLabels){
		String vars = "";
		for(int i=0;i<numLabels;i++){
			vars += " ?o" + String.valueOf(i); 
		}
		return vars + "";
	}
	
	protected String getTriplePatterns(String varname, String[] labelProps){
		String patterns = "";
		String pref = "?" + varname + " <";
		for(int i=0;i<labelProps.length;i++){
			patterns += pref + labelProps[i] + "> ?o" + String.valueOf(i) + ". ";
		}
		return patterns;
	}
}
