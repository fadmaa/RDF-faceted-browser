package org.deri.rdf.browser;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.deri.rdf.browser.model.AnnotatedResultItem;
import org.deri.rdf.browser.model.RdfDecoratedValue;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class TradedoffRdfEngine {
	private static Logger logger = Logger.getLogger("org.deri.rdf.browser.TradedoffRdfEngine");
	
	public long getResourcesCount(String[] sparqls, String endpoint){
		long count = 0l;
		for(String sparql:sparqls){
			QueryExecution qExec = execSparql(sparql, endpoint);
			ResultSet res = qExec.execSelect();
			if(res.hasNext()){
				QuerySolution sol = res.next();
				count += sol.getLiteral("count").getLong();
			}
			qExec.close();
		}
		return count;
	}
	
	public List<AnnotatedResultItem> getPropertiesWithCount(String[] sparqls, String endpoint, String varname) {
		List<AnnotatedResultItem> propertiesList = new ArrayList<AnnotatedResultItem>();
		for(String sparql:sparqls){
			QueryExecution qExec = execSparql(sparql, endpoint);
			ResultSet res = qExec.execSelect();
			while(res.hasNext()){
				QuerySolution sol = res.next();
				RDFNode node = sol.get(varname);
				String v = getString(node);
				if(v==null){					
					continue;
				}
				int count = sol.getLiteral("count").getInt();
				byte valueType = node.canAs(Literal.class)?RdfDecoratedValue.LITERAL:RdfDecoratedValue.RESOURCE;
				AnnotatedResultItem item = new AnnotatedResultItem(count,v,valueType);
				if(propertiesList.contains(item)){
					AnnotatedResultItem existingItem = propertiesList.get(propertiesList.indexOf(item));
					existingItem.setCount(existingItem.getCount()+count);
				}else{
					propertiesList.add(item);
				}
			}
			qExec.close();
		}
		
		return propertiesList;
	}
	
	public AnnotatedResultItem countResourcesMissingProperty(String[] sparqls, String endpoint) {
		int count = 0;
		for(String sparql:sparqls){
			QueryExecution qExec = execSparql(sparql, endpoint);
			ResultSet res = qExec.execSelect();
			if(res.hasNext()){
				QuerySolution sol = res.next();
				count += sol.getLiteral("count").getInt();
			}
			qExec.close();
		}
		return new AnnotatedResultItem(count);
	}
	
	private QueryExecution execSparql(String sparql, String sparqlEndpointUrl) {
		//we use QueryEngineHTTP to skip query validation as Virtuoso needs non-standardised extensions and will not pass ARQ validation
		
		logger.debug("executing SPARQL query:\n" + sparql + "\nagainst " + sparqlEndpointUrl);
		Query query = QueryFactory.create(sparql,Syntax.syntaxARQ);
		QueryExecution qExec = QueryExecutionFactory.sparqlService(sparqlEndpointUrl, query);
		
		return qExec;
	}
	
	protected String getString(RDFNode node){
		if(node==null) return null;
		if(node.canAs(Literal.class)){
			return node.asLiteral().getString();
		}else{
			return node.asResource().getURI();
		}
	}
}
