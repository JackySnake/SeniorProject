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
import static java.awt.event.KeyEvent.VK_ESCAPE;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Scanner;

/**
 *
 * @author mtmmoei
 */
public class QueryJenaBenchmark {

    public static void main(String[] args) {
        long startLoadTime = System.nanoTime();
        FileManager fm = FileManager.get();
        fm.addLocatorClassLoader(QueryJena.class.getClassLoader());
        InputStream in = fm.open("data/University80-clean2.nt");
//        InputStream in = fm.open("data/data.nt");
//        Location location = new Location("target/TDB");
        Location location = Location.create("target/TDB");

        // Load some initial data
        TDBLoader.load(TDBInternal.getBaseDatasetGraphTDB(TDBFactory.createDatasetGraph(location)), in, false);
        String prefix
                = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
"PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>\n" +
"SELECT ?X ?Y ?Z\n" +
"WHERE\n" +
"{?X rdf:type ub:GraduateStudent .\n" +
"  ?Y rdf:type ub:University .\n" +
"  ?Z rdf:type ub:Department .\n" +
"  ?X ub:memberOf ?Z .\n" +
"  ?Z ub:subOrganizationOf ?Y .\n" +
"  ?X ub:undergraduateDegreeFrom ?Y}";
//String prefix = "";
        Scanner kb = new Scanner(System.in);
        Dataset dataset = TDBFactory.createDataset(location);

//         while(getKeyCode()!=VK_ESCAPE){
//             
//         }
        long endLoadTime = System.nanoTime();
        long totalLoadTime = (endLoadTime - startLoadTime)/1000000000;

        System.out.println("Total load time = " + totalLoadTime);

        long startQueryTime;
        long endQueryTime;
        long totalQueryTime;

        
          //  System.out.println("Enter SPARQL");
//         String queryString = "";
//         while(kb.hasNext()){
           // String queryString = kb.nextLine();
//         }

            System.out.println("Result");

//        String queryString = "SELECT ?resource WHERE { ?resource movie:filmid ?uri. ?resource dc:title "Forrest Gump" .} ORDER BY ?resource ";
            String out = "";
            startQueryTime = System.nanoTime();
            dataset.begin(ReadWrite.READ);
            try {
                Query query = QueryFactory.create(prefix);
                QueryExecution qexec = QueryExecutionFactory.create(query, dataset);
                try {
                    ResultSetRewindable results = ResultSetFactory.makeRewindable(qexec.execSelect());
//                ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                ResultSetFormatter.outputAsJSON(bos, results);
//                  out=bos.toString();
                    out = ResultSetFormatter.asText(results);
                    results.reset();
                } finally {
                    qexec.close();
                }
            } finally {
                dataset.end();
            }
            endQueryTime = System.nanoTime();
            System.out.println(out);
            totalQueryTime = (endQueryTime - startLoadTime)/1000000000;
            System.out.println("Total query time = " + totalQueryTime);

        }
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
