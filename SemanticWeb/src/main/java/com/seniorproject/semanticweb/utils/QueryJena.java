/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.seniorproject.semanticweb.utils;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.TDBLoader;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.sys.TDBInternal;
import com.hp.hpl.jena.util.FileManager;
import java.io.InputStream;
import java.util.Scanner;

/**
 *
 * @author mtmmoei
 */
public class QueryJena {

    public static void main(String[] args) {
        long startLoadTime = System.currentTimeMillis();
        FileManager fm = FileManager.get();
        fm.addLocatorClassLoader(QueryJena.class.getClassLoader());
        InputStream in = fm.open("data/linkedmdb-latest-dump.nt");
        Location location = Location.create("target/TDB");

        // Load some initial data
        TDBLoader.load(TDBInternal.getBaseDatasetGraphTDB(TDBFactory.createDatasetGraph(location)), in, false);
        String prefix
                = "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                + "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                + "PREFIX oddlinker: <http://data.linkedmdb.org/resource/oddlinker/> "
                + "PREFIX map: <file:/C:/d2r-server-0.4/mapping.n3#> "
                + "PREFIX db: <http://data.linkedmdb.org/resource/> "
                + "PREFIX dbpedia: <http://dbpedia.org/property/> "
                + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "
                + "PREFIX dc: <http://purl.org/dc/terms/> "
                + "PREFIX movie: <http://data.linkedmdb.org/resource/movie/> ";
        Scanner kb = new Scanner(System.in);
        Dataset dataset = TDBFactory.createDataset(location);

        long endLoadTime = System.currentTimeMillis();
        long totalLoadTime = endLoadTime - startLoadTime;

        System.out.println("Total load time = " + totalLoadTime);

        long startQueryTime;
        long endQueryTime;
        long totalQueryTime;

        while (true) {
            System.out.println("Enter SPARQL");
            String queryString = kb.nextLine();

            System.out.println("Result");

           String out = "";
            startQueryTime = System.currentTimeMillis();
            dataset.begin(ReadWrite.READ);
            try {
                Query query = QueryFactory.create(prefix + queryString);
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
            endQueryTime = System.currentTimeMillis();
            System.out.println(out);
            totalQueryTime = endQueryTime - startQueryTime;
            System.out.println("Total query time = " + totalQueryTime);

        }
    }
}
