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
import org.deri.rdf.browser.model.Facet;
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
		QueryExecution qExec = execSparql(sparql, endpoint);
		ResultSet result = qExec.execSelect();
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
		qExec.close();
		return propsWithCount;
	}

	public AnnotatedResultItem countResourcesMissingProperty(String sparql, String endpoint) {
		QueryExecution qExec = execSparql(sparql, endpoint);
		ResultSet res = qExec.execSelect();
		QuerySolution sol = res.next();
		int count = sol.getLiteral("count").getInt();
		qExec.close();
		return new AnnotatedResultItem(count);
	}
	
	public Set<RdfDecoratedValue> getResources(String sparql, String endpoint, String varname) {
		Set<RdfDecoratedValue> items = new HashSet<RdfDecoratedValue>();
		
		QueryExecution qExec = execSparql(sparql, endpoint);
		ResultSet result = qExec.execSelect();
		while(result.hasNext()){
			QuerySolution sol = result.next();
			items.add(getRdfDecoratedValue(sol,varname));
		}
		qExec.close();
		return items;
	}
	
	public Collection<RdfResource> getRdfResources(String sparql, String endpoint) {
		Map<String, RdfResource> resourcesMap = new HashMap<String, RdfResource>();
		QueryExecution qExec = execSparql(sparql, endpoint);
		ResultSet res = qExec.execSelect();
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
		qExec.close();
		//Map.values() gives immutable collection
		return new HashSet<RdfResource>(resourcesMap.values());
	}

	public long getResourcesCount(String sparql, String endpoint){
		long count;
		QueryExecution qExec = execSparql(sparql, endpoint);
		ResultSet res = qExec.execSelect();
		if(res.hasNext()){
			QuerySolution sol = res.next();
			count = sol.getLiteral("count").getLong();
		}else{
			count = 0l;
		}
		qExec.close();
		return count;
	}
	
	public void annotatePropertiesWithEndpoints(String[] propSparqls, String[] endpoints, List<AnnotatedResultItem> items, Facet facet) {
		for(int i=0;i<endpoints.length;i++){
			String sparql = propSparqls[i];
			String ep = endpoints[i];
			//execute sparql against ep
			QueryExecution qExec = execSparql(sparql, ep);
			ResultSet result = qExec.execSelect();
			while(result.hasNext()){
				QuerySolution sol = result.next();
				RDFNode node = sol.get(facet.getVarname());
				String v = getString(node);
				byte valueType = node.canAs(Literal.class)?RdfDecoratedValue.LITERAL:RdfDecoratedValue.RESOURCE;
				AnnotatedResultItem item = new AnnotatedResultItem(v,valueType);
				//if items contain v the add endpoint to its endpoints
				if(items.contains(item)){
					items.get(items.indexOf(item)).addEndpoint(ep);
				}
			}
			qExec.close();
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
	
	private QueryExecution execSparql(String sparql, String sparqlEndpointUrl) {
		//we use QueryEngineHTTP to skip query validation as Virtuoso needs non-standardised extensions and will not pass ARQ validation
		
		logger.debug("executing SPARQL query:\n" + sparql + "\nagainst " + sparqlEndpointUrl);
		Query query = QueryFactory.create(sparql,Syntax.syntaxARQ);
		/*QueryEngineHTTP qExec = new QueryEngineHTTP(sparqlEndpointUrl, query);
		
//		qExec.addParam("apikey", "KASAPI API KEY");
		ResultSet res = qExec.execSelect();*/
		QueryExecution qExec = QueryExecutionFactory.sparqlService(sparqlEndpointUrl, query);
		
		return qExec;
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
