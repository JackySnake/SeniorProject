/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.seniorproject.semanticweb.example;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.util.FileManager;

/**
 *
 * @author mtmmoei
 */
public class ExampleARQ_03 {
    public static void main(String[] args) {
        FileManager.get().addLocatorClassLoader(ExampleARQ_01.class.getClassLoader());
        String apikey = System.getenv("KASABI_API_KEY");
        
        String queryString = 
        		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + 
        		"PREFIX italy: <http://data.kasabi.com/dataset/italy/schema/>" +
        		"SELECT ?region WHERE { " +
        		"	?region rdf:type italy:Region" +
        		"}";
        Query query = QueryFactory.create(queryString);
        QueryEngineHTTP qexec = (QueryEngineHTTP)QueryExecutionFactory.createServiceRequest("http://api.kasabi.com/dataset/italy/apis/sparql", query);
        qexec.addParam("apikey", apikey);
        try {
            ResultSet results = qexec.execSelect();
            while ( results.hasNext() ) {
                QuerySolution soln = results.nextSolution();
                Resource region = soln.getResource("region");
                System.out.println(region.getURI());
            }
        } finally {
            qexec.close();
        }
	}
}
