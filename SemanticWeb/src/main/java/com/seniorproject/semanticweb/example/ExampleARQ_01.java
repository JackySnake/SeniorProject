/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.seniorproject.semanticweb.example;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

/**
 *
 * @author mtmmoei
 */
public class ExampleARQ_01 {
    public static void main(String[] args) {
        FileManager.get().addLocatorClassLoader(ExampleARQ_01.class.getClassLoader());
        Model model = FileManager.get().loadModel("data/data.ttl");

        String queryString = 
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
        		"SELECT ?name WHERE { " +
        		"    ?person foaf:mbox <mailto:alice@example.org> . " +
        		"    ?person foaf:name ?name . " +
        		"}";
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        try {
            ResultSet results = qexec.execSelect();
            while ( results.hasNext() ) {
                QuerySolution soln = results.nextSolution();
                Literal name = soln.getLiteral("name");
                System.out.println(name);
            }
        } finally {
            qexec.close();
        }

    }
}
