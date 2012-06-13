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

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class FederatedRdfEngine {
	
	private static Logger logger = Logger.getLogger("org.deri.rdf.browser.FederatedRdfEngine");

	public List<AnnotatedResultItem> getPropertiesWithCount(String[] sparqls, String[] endpoints, boolean trackEndpoints) {
		List<AnnotatedResultItem> propsWithCount = new ArrayList<AnnotatedResultItem>();
		for(int i=0;i<endpoints.length;i++){
			String sparql = sparqls[i];
			String ep = endpoints[i];
			//execute sparql against ep
			ResultSet result = execSparql(sparql, ep);
			while(result.hasNext()){
				QuerySolution sol = result.next();
				RDFNode node = sol.get("v");
				String v = getString(node);
				if(v==null){
					continue;
				}
				int count = sol.getLiteral("count").getInt();
				int valueType = node.canAs(Literal.class)?RdfDecoratedValue.LITERAL:RdfDecoratedValue.RESOURCE;
				AnnotatedResultItem countedVal = new AnnotatedResultItem(count,v,valueType,new String[]{});
				if(trackEndpoints){
					countedVal.addEndpoint(ep);
				}
				if(propsWithCount.contains(countedVal)){
					//already added. update count and endpoints accordingly 
					AnnotatedResultItem existingVal = propsWithCount.get(propsWithCount.indexOf(countedVal));
					existingVal.setCount( existingVal.getCount() + sol.getLiteral("count").getInt());
					if(trackEndpoints){
						existingVal.addEndpoint(ep);
					}
				}else{
					propsWithCount.add(countedVal);
				}
			}
		}
		return propsWithCount;
	}

	public AnnotatedResultItem countResourcesMissingProperty(String[] sparqls, String[] endpoints) {
		int count = 0;
		for(int i=0;i<endpoints.length;i++){
			String sparql = sparqls[i];
			String ep = endpoints[i];
			//execute sparql against ep
			ResultSet result = execSparql(sparql, ep);
			while(result.hasNext()){
				QuerySolution sol = result.next();
				count += sol.getLiteral("count").getInt();
			}
		}
		return new AnnotatedResultItem(count);
	}
	
	public Set<String> getResources(String sparql, String endpoint) {
		Set<String> items = new HashSet<String>();
		
		ResultSet result = execSparql(sparql, endpoint);
		while(result.hasNext()){
			QuerySolution sol = result.next();
			String uri = sol.getResource("s").getURI();
			items.add(uri);
		}
		
		return items;
	}
	
	public Collection<RdfResource> getRdfResources(String sparql, String[] endpoints) {
		Map<String, RdfResource> resourcesMap = new HashMap<String, RdfResource>();
		for(int i=0; i<endpoints.length; i++){
			ResultSet res = execSparql(sparql, endpoints[i]);
			while(res.hasNext()){
				QuerySolution sol = res.next();
				String uri = sol.getResource("s").getURI();
				String p = sol.getResource("p").getURI();
				String v = getString(sol.get("o"));
				if(resourcesMap.containsKey(uri)){
					resourcesMap.get(uri).addProperty(p,v);
				}else{
					RdfResource item = new RdfResource(uri);
					item.addProperty(p,v);
					resourcesMap.put(uri,item);
				}
			}
		}
		return resourcesMap.values();
	}

	public long getResourcesCount(String sparql, String[] endpoints){
		ResultSet res = execSparql(sparql, endpoints[0]);
		if(res.hasNext()){
			QuerySolution sol = res.next();
			return sol.getLiteral("count").getLong();
		}else{
			return 0l;
		}
	}
	
	public void annotatePropertiesWithEndpoints(String[] propSparqls, String[] endpoints, List<AnnotatedResultItem> items) {
		for(int i=0;i<endpoints.length;i++){
			String sparql = propSparqls[i];
			String ep = endpoints[i];
			//execute sparql against ep
			ResultSet result = execSparql(sparql, ep);
			while(result.hasNext()){
				QuerySolution sol = result.next();
				RDFNode node = sol.get("v");
				String v = getString(node);
				int valueType = node.canAs(Literal.class)?RdfDecoratedValue.LITERAL:RdfDecoratedValue.RESOURCE;
				AnnotatedResultItem item = new AnnotatedResultItem(v,valueType);
				//if items contain v the add endpoint to its endpoints
				if(items.contains(item)){
					items.get(items.indexOf(item)).addEndpoint(ep);
				}
			}
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
		logger.debug("executing SPARQL query:\\n" + sparql);
		QueryEngineHTTP qExec = new QueryEngineHTTP(sparqlEndpointUrl, sparql);
		
		//qExec.addParam("apikey", "KASABI API KEY");
		ResultSet res = qExec.execSelect();
		return res;
	}

}
