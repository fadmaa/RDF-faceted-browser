package org.deri.rdf.browser.experiment;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;

public class ExperimentExecutorOpt {
	public void run(String filename, String sparqlEndpointUrl,boolean withAnnotationQueries) throws Exception {
		FileInputStream fstream = new FileInputStream(filename);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String sparql;
		while ((sparql = br.readLine()) != null) {
			if(sparql.startsWith("SELECT DISTINCT ?") && !"?s ".equals(sparql.substring(16, 19)) && !withAnnotationQueries){
				continue;
			}
			System.out.println(sparql);
			Query query = QueryFactory.create(sparql,Syntax.syntaxARQ);
			QueryExecution qExec = QueryExecutionFactory.sparqlService(sparqlEndpointUrl, query);
			long start = System.currentTimeMillis();
			ResultSet res = qExec.execSelect();
			long end = System.currentTimeMillis();
			System.out.println("time taken: " + (end-start));
			while(res.hasNext()){
				res.next();
			}
			
			Thread.sleep(1000*10);
		}
		in.close();
	}
	
	public static void main(String[] args) throws Exception{
		ExperimentExecutorOpt executor = new ExperimentExecutorOpt();
		executor.run("optimised/format-rdf-format-rdfa-ontology-opendata-linkedData-2", "http://localhost:3032/test/query",false);
	}
}
