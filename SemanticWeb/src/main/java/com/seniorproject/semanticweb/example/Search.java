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
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

/**
 *
 * @author mtmmoei
 */
public class Search {
        public static void main(String[]args){
            String queryString="SELECT ?resource ?value\n" +
"WHERE { ?resource movie:filmid ?uri.\n" +
"?resource dc:title \"Forrest Gump\" .\n" +
"}\n" +
"ORDER BY ?resource ?value";
//            String queryString = "SELECT ?name WHERE { \n" +
//"        		   ?person foaf:mbox <mailto:alice@example.org> .  \n" +
//"        		    ?person foaf:name ?name . \n" +
//"        		}";
            System.out.println("1 test");
        FileManager.get().addLocatorClassLoader(Search.class.getClassLoader());
        System.out.println("2");
        
        long heapSize = Runtime.getRuntime().totalMemory();
         
        //Print the jvm heap size.
        System.out.println("Heap Size = " + heapSize);
        Model model = FileManager.get().loadModel("data/linkedmdb-latest-dump.nt");
        System.out.println("3");
        String prefix = 
                "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
                "PREFIX oddlinker: <http://data.linkedmdb.org/resource/oddlinker/> " +
                "PREFIX map: <file:/C:/d2r-server-0.4/mapping.n3#> " +
                "PREFIX db: <http://data.linkedmdb.org/resource/> " +
                "PREFIX dbpedia: <http://dbpedia.org/property/> " +
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
                "PREFIX dc: <http://purl.org/dc/terms/> " +
                "PREFIX movie: <http://data.linkedmdb.org/resource/movie/> ";
        System.out.println("4");
//        Model model = FileManager.get().loadModel("data/data.nt");
//        String prefix = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> ";
        System.out.println("5");
        Query query = QueryFactory.create(prefix+queryString);
        System.out.println("6");
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        System.out.println("7");
        String out = "";
        System.out.println("8");
        try {
            ResultSetRewindable results = ResultSetFactory.makeRewindable(qexec.execSelect());
            out = ResultSetFormatter.asText(results);
            results.reset();
        } finally {
            qexec.close();
        }
        System.out.println(out);
    }
}
