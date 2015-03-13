/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.seniorproject.semanticweb.services;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.TDBLoader;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.sys.TDBInternal;
import com.hp.hpl.jena.util.FileManager;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.springframework.stereotype.Service;

/**
 *
 * @author mtmmoei
 */
@Service
public class WebServices {

    public String queryJena(String queryString) {
        FileManager fm = FileManager.get();
        fm.addLocatorClassLoader(WebServices.class.getClassLoader());
        InputStream in = fm.open("data/linkedmdb-latest-dump.nt");
//        Location location = new Location("target/TDB");
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

        String out = "";
        Dataset dataset = TDBFactory.createDataset(location);
        dataset.begin(ReadWrite.READ);
        try {
            Query query = QueryFactory.create(prefix + queryString);
            QueryExecution qexec = QueryExecutionFactory.create(query, dataset);
            try {
                ResultSetRewindable results = ResultSetFactory.makeRewindable(qexec.execSelect());
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ResultSetFormatter.outputAsJSON(bos, results);
                out=bos.toString();
                results.reset();
            } finally {
                qexec.close();
            }
        } finally {
            dataset.end();
        }
        System.out.println(out);
        return out;
    }
        
    public String genSparql(String keyword, String type) {
        String queryString = "";

        switch (type) {
            case "Person":
                queryString = "SELECT * WHERE { \n"
                        + "?person <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> foaf:Person . \n"
                        + "?person foaf:name '" + keyword + "' . "
                        + "?person ?predicate ?object .}";
                break;
            case "Mailbox":
                queryString = "SELECT * WHERE { \n"
                        + "?person foaf:mbox <" + keyword + "> . "
                        + "?person foaf:mbox ?mailbox .}";
                break;
            default:
                queryString = "Invalid";
                break;
        }
        return queryString;
    }
    
    public String convertToJSON(String queryString){
        System.out.println("convert");
//        String queryString
//                = "SELECT ?s ?p ?o  "
//                + "WHERE { ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> . "
//                + "?s ?p ?o .}";
//        String[] tokens = queryString.split("(\\s|^)select(\\s)|");
//    JsonArrayBuilder buildingsArrBuilder = Json.createArrayBuilder();
//        JsonArrayBuilder varsArrBuilder = Json.createArrayBuilder();
//
//        JsonObjectBuilder out = Json.createObjectBuilder()
//        .add("head",Json.createObjectBuilder()
//                .add("vars", varsArrBuilder))
//        .add("results",Json.createObjectBuilder()
//                .add("bindings",buildingsArrBuilder));
        JsonArrayBuilder out = Json.createArrayBuilder();
String filePath = new File("").getAbsolutePath();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath.concat("/src/main/resources/data/data.nt")))) {

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                List<String> matchList = new ArrayList<String>();
                Pattern regex = Pattern.compile("[^\\s\"']+|\"[^\"]*\"|'[^']*'");
                Matcher regexMatcher = regex.matcher(sCurrentLine);
                while (regexMatcher.find()) {
                    matchList.add(regexMatcher.group());
                }
                JsonArrayBuilder resultArray = Json.createArrayBuilder();

                for (int i = 0; i < matchList.size(); i++) {
                    resultArray.add(matchList.get(i));
                }
                out.add(resultArray);
                
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        ;
        System.out.println(out.build());
        String result = out.build().toString();
        return result;
    }
}
 