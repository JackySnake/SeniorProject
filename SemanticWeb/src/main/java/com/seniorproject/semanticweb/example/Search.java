/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.seniorproject.semanticweb.example;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.TDBLoader;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.sys.TDBInternal;
import com.hp.hpl.jena.util.FileManager;
import java.io.InputStream;

/**
 *
 * @author mtmmoei
 */
public class Search {
    public static void main(String[] args) {
        FileManager fm = FileManager.get();
        fm.addLocatorClassLoader(Search.class.getClassLoader());
        InputStream in = fm.open("data/linkedmdb-latest-dump.nt");

        Location location = new Location ("target/TDB");

        // Load some initial data
        TDBLoader.load(TDBInternal.getBaseDatasetGraphTDB(TDBFactory.createDatasetGraph(location)), in, false);
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
        String queryString = 
           "SELECT ?resource \n" +
"WHERE { ?resource movie:filmid ?uri.\n" +
"?resource dc:title \"Forrest Gump\" .\n" +
"}\n" +
"ORDER BY ?resource ";
          String out = "";
        Dataset dataset = TDBFactory.createDataset(location);
        dataset.begin(ReadWrite.READ);
        try {
            Query query = QueryFactory.create(prefix+queryString);
            QueryExecution qexec = QueryExecutionFactory.create(query, dataset);
            try {
                 ResultSetRewindable results = ResultSetFactory.makeRewindable(qexec.execSelect());
            out = ResultSetFormatter.asText(results);
            results.reset();
            } finally {
                qexec.close();
            }
        } finally {
            dataset.end();
        }
        System.out.println(out);
    }
    
    
//        public static void main(String[]args){
//            String queryString="SELECT ?resource \n" +
//"WHERE { ?resource movie:filmid ?uri.\n" +
//"?resource dc:title \"Forrest Gump\" .\n" +
//"}\n" +
//"ORDER BY ?resource ";
////            String queryString = "SELECT ?name WHERE { \n" +
////"        		   ?person foaf:mbox <mailto:alice@example.org> .  \n" +
////"        		    ?person foaf:name ?name . \n" +
////"        		}";
//            String assemblerFile = "data/linkedmdb-latest-dump.nt" ;
//  Dataset dataset = TDBFactory.assembleDataset(assemblerFile) ;
//  dataset.begin(ReadWrite.READ) ;
//  // Get model inside the transaction
//  Model model = dataset.getDefaultModel() ;
//  dataset.end() ;
//        FileManager.get().addLocatorClassLoader(Search.class.getClassLoader());
//        
//        long heapSize = Runtime.getRuntime().totalMemory();
//         
//        //Print the jvm heap size.
//        System.out.println("Heap Size = " + heapSize);
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
////        Model model = FileManager.get().loadModel("data/data.nt");
////        String prefix = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> ";
////        String queryString="SELECT ?s ?p ?o " +
////"WHERE { ?s ?o ?p .}" ;
////        Model model = FileManager.get().loadModel("data/University20-clean2.nt");
//
//        Query query = QueryFactory.create(prefix+queryString);
//        QueryExecution qexec = QueryExecutionFactory.create(query, model);
//        String out = "";
//        try {
//            ResultSetRewindable results = ResultSetFactory.makeRewindable(qexec.execSelect());
//            out = ResultSetFormatter.asText(results);
//            results.reset();
//        } finally {
//            qexec.close();
//        }
//        System.out.println(out);
//    }
}
