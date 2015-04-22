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
public class QueryJenaBenchmark {

    public static void main(String[] args) {
        long startLoadTime = System.nanoTime();
        FileManager fm = FileManager.get();
        fm.addLocatorClassLoader(QueryJena.class.getClassLoader());
        InputStream in = fm.open("data/University160-clean3.nt");

        Location location = Location.create("target/TDB");

        // Load some initial data
        TDBLoader.load(TDBInternal.getBaseDatasetGraphTDB(TDBFactory.createDatasetGraph(location)), in, false);
        String prefix
                = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#>\n"
                + "SELECT ?X ?Y ?Z\n"
                + "WHERE\n"
                + "{?X rdf:type ub:GraduateStudent .\n"
                + "  ?Y rdf:type ub:University .\n"
                + "  ?Z rdf:type ub:Department .\n"
                + "  ?X ub:memberOf ?Z .\n"
                + "  ?Z ub:subOrganizationOf ?Y .\n"
                + "  ?X ub:undergraduateDegreeFrom ?Y}";

        Scanner kb = new Scanner(System.in);
        Dataset dataset = TDBFactory.createDataset(location);

        long endLoadTime = System.nanoTime();
        long totalLoadTime = (endLoadTime - startLoadTime) / 1000000000;

        System.out.println("Total load time = " + totalLoadTime);

        long startQueryTime;
        long endQueryTime;
        long totalQueryTime;

        System.out.println("Result");

        String out = "";
        startQueryTime = System.nanoTime();
        dataset.begin(ReadWrite.READ);
        try {
            Query query = QueryFactory.create(prefix);
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
        endQueryTime = System.nanoTime();
        System.out.println(out);
        totalQueryTime = (endQueryTime - startLoadTime) / 1000000000;
        System.out.println("Total query time = " + totalQueryTime);

    }
}
