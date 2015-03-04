/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.seniorproject.semanticweb.services;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;
import java.io.ByteArrayOutputStream;
import org.springframework.stereotype.Service;

/**
 *
 * @author mtmmoei
 */
@Service
public class WebServices {
        public String queryJena(String queryString){
            System.out.println("1 test");
        FileManager.get().addLocatorClassLoader(WebServices.class.getClassLoader());
        System.out.println("2");
        
        long heapSize = Runtime.getRuntime().totalMemory();
         
        //Print the jvm heap size.
        System.out.println("Heap Size = " + heapSize);
//        Model model = FileManager.get().loadModel("data/linkedmdb-latest-dump.nt");
//        System.out.println("3");
//        String prefix = 
//                "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
//                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
//                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
//                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
//                "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
//                "PREFIX oddlinker: <http://data.linkedmdb.org/resource/oddlinker/> " +
//                "PREFIX map: <file:/C:/d2r-server-0.4/mapping.n3#> " +
//                "PREFIX db: <http://data.linkedmdb.org/resource/> " +
//                "PREFIX dbpedia: <http://dbpedia.org/property/> " +
//                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
//                "PREFIX dc: <http://purl.org/dc/terms/> " +
//                "PREFIX movie: <http://data.linkedmdb.org/resource/movie/> ";
        Model model = FileManager.get().loadModel("data/data.nt");
        String prefix = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> ";
        Query query = QueryFactory.create(prefix+queryString);
        System.out.println("6");
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        System.out.println("7");
        String out = "";
        try {
            ResultSetRewindable results = ResultSetFactory.makeRewindable(qexec.execSelect());
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ResultSetFormatter.outputAsJSON(bos, results);
            out = bos.toString();
            results.reset();
        } finally {
            qexec.close();
        }
        System.out.print(out);
        return out;
    }
        public String getPredicate(String keyword){
            String type="";
            FileManager.get().addLocatorClassLoader(WebServices.class.getClassLoader());
             Model model = FileManager.get().loadModel("data/data.nt");
        String prefix = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> ";
        String queryString = "SELECT ?s ?p ?o  WHERE { " +
                "?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> ." +
                "?s ?p ?o .}";
        Query query = QueryFactory.create(prefix+queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        String out = "";
        try {
            ResultSetRewindable results = ResultSetFactory.makeRewindable(qexec.execSelect());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ResultSetFormatter.outputAsJSON(bos, results);
            out = bos.toString();
            results.reset();
        } finally {
            qexec.close();
        }
        System.out.print(out);
            return out;
        }
}
 