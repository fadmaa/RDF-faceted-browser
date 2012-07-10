package org.deri.rdf.browser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.deri.rdf.browser.model.AnnotatedResultItem;
import org.deri.rdf.browser.model.RdfDecoratedValue;
import org.deri.rdf.browser.model.RdfResource;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class RdfEngine {
	
	private static Logger logger = Logger.getLogger("org.deri.rdf.browser.RdfEngine");

	public List<AnnotatedResultItem> getPropertiesWithCount(String sparql, String endpoint, String varname) {
		List<AnnotatedResultItem> propsWithCount = new ArrayList<AnnotatedResultItem>();
		ResultSet result = execSparql(sparql, endpoint);
		while(result.hasNext()){
			QuerySolution sol = result.next();
			RDFNode node = sol.get(varname);
			String v = getString(node);
			if(v==null){					
				continue;
			}
			int count = sol.getLiteral("count").getInt();
			byte valueType = node.canAs(Literal.class)?RdfDecoratedValue.LITERAL:RdfDecoratedValue.RESOURCE;
			propsWithCount.add(new AnnotatedResultItem(count,v,valueType));
		}
		return propsWithCount;
	}

	public AnnotatedResultItem countResourcesMissingProperty(String sparql, String endpoint) {
		ResultSet result = execSparql(sparql, endpoint);
		QuerySolution sol = result.next();
		int count = sol.getLiteral("count").getInt();
		return new AnnotatedResultItem(count);
	}
	
	public Set<RdfDecoratedValue> getResources(String sparql, String endpoint, String varname) {
		Set<RdfDecoratedValue> items = new HashSet<RdfDecoratedValue>();
		
		ResultSet result = execSparql(sparql, endpoint);
		while(result.hasNext()){
			QuerySolution sol = result.next();
			items.add(getRdfDecoratedValue(sol,varname));
		}
		
		return items;
	}
	
	public Collection<RdfResource> getRdfResources(String sparql, String endpoint) {
		Map<String, RdfResource> resourcesMap = new HashMap<String, RdfResource>();
		ResultSet res = execSparql(sparql, endpoint);
		while(res.hasNext()){
			QuerySolution sol = res.next();
			String uri = sol.getResource("s").getURI();
			String p = sol.getResource("p").getURI();
			String v = getString(sol.get("o"));
			if(resourcesMap.containsKey(uri)){
				resourcesMap.get(uri).addProperty(p,v);
			}else{
				RdfResource item = new RdfResource(new RdfDecoratedValue(uri, RdfDecoratedValue.RESOURCE));
				item.addProperty(p,v);
				resourcesMap.put(uri,item);
			}
		}
		//Map.values() gives immutable collection
		return new HashSet<RdfResource>(resourcesMap.values());
	}

	public long getResourcesCount(String sparql, String endpoint){
		ResultSet res = execSparql(sparql, endpoint);
		if(res.hasNext()){
			QuerySolution sol = res.next();
			return sol.getLiteral("count").getLong();
		}else{
			return 0l;
		}
	}
	
	protected String getString(RDFNode node){
		if(node==null) return null;
		if(node.canAs(Literal.class)){
			return node.asLiteral().getString();
		}else{
			return node.asResource().getURI();
		}
	}
	
	private ResultSet execSparql(String sparql, String sparqlEndpointUrl) {
		//we use QueryEngineHTTP to skip query validation as Virtuoso needs non-standardised extensions and will not pass ARQ validation
		logger.debug("executing SPARQL query:\n" + sparql + "\nagainst " + sparqlEndpointUrl);
		Query query = QueryFactory.create(sparql,Syntax.syntaxARQ);
		/*QueryEngineHTTP qExec = new QueryEngineHTTP(sparqlEndpointUrl, query);
		
//		qExec.addParam("apikey", "KASAPI API KEY");
		ResultSet res = qExec.execSelect();*/
		QueryExecution qExec = QueryExecutionFactory.sparqlService(sparqlEndpointUrl, query);
		ResultSet res = qExec.execSelect();
		return res;
	}
	
	private RdfDecoratedValue getRdfDecoratedValue(QuerySolution sol, String varname) {
		RDFNode node = sol.get(varname);
		if(node.canAs(Literal.class)){
			return new RdfDecoratedValue(node.asLiteral().getString(), RdfDecoratedValue.LITERAL);
		}else{
			return new RdfDecoratedValue(node.asResource().getURI(), RdfDecoratedValue.RESOURCE);
		}
	}

}
