/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.seniorproject.semanticweb.services;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.servlet.ServletContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author mtmmoei
 */
@Service
public class WebServices {

    @Autowired
    ServletContext servletContext;

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
                out = bos.toString();
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

    public String convertToJSON(String queryString, ArrayList<String> results) throws FileNotFoundException {
        System.out.println("convert");
        JsonArrayBuilder out = Json.createArrayBuilder();
        JsonArrayBuilder resultArray = Json.createArrayBuilder();

        String words[] = queryString.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            if (words[i].equalsIgnoreCase("select")) {
                int j = i + 1;
                if (words[j].equalsIgnoreCase("distinct")) {
                    j++;
                }
                while (!words[j].equalsIgnoreCase("where")) {
                    resultArray.add(words[j].substring(1));
                    j++;
                }
                break;
            }
        }
        out.add(resultArray);

        for (String result : results) {
            List<String> matchList = new ArrayList<String>();
            Pattern regex = Pattern.compile("[^\\s\"']+|\"[^\"]*\"|'[^']*'");
            Matcher regexMatcher = regex.matcher(result);
            while (regexMatcher.find()) {
                matchList.add(regexMatcher.group());
            }
            for (int i = 0; i < matchList.size(); i++) {
                resultArray.add(matchList.get(i));
            }
            out.add(resultArray);
        }
        return out.build().toString();
    }

    public ArrayList<String> getCategories() {
//        System.out.println("getCategories");
        ArrayList<String> categories = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(servletContext.getRealPath("/WEB-INF/classes/hadoop/dictionary.txt")))) {

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                List<String> matchList = new ArrayList<>();
                Pattern regex = Pattern.compile("[^\\s\"']+|\"[^\"]*\"|'[^']*'");
                Matcher regexMatcher = regex.matcher(sCurrentLine);
                while (regexMatcher.find()) {
                    matchList.add(regexMatcher.group());
                }
                if (matchList.size() > 0) {
                    categories.add(matchList.get(0));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        };
        return categories;
    }

    public ArrayList<String> getProperties(String category) {
        ArrayList<String> properties = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(servletContext.getRealPath("/WEB-INF/classes/hadoop/modified_propertyQuery/modified_propertyQuery_" + category + ".txt")))) {

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                properties.add(sCurrentLine);
            }

        } catch (IOException e) {
            e.printStackTrace();
        };
        try (BufferedReader br = new BufferedReader(new FileReader(servletContext.getRealPath("/WEB-INF/classes/hadoop/modified_isValueOfQuery/modified_isValueOfQuery_" + category + ".txt")))) {

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                properties.add("is " + sCurrentLine + " of");
            }

        } catch (IOException e) {
            e.printStackTrace();
        };
        return properties;
    }

    public String addPropertySparqlGenerator(String category, String property, String selectedValues) {
        String iri = getIRI(category);
        String queryString = "SELECT ?o ?label WHERE { ";
        queryString += "?s rdf:type " + iri + " . ";
        if (selectedValues.length() > 0) {
            JsonParser parser = Json.createParser(new StringReader(selectedValues));

            Event event = parser.next();// START_OBJECT
            while ((event = parser.next()) != Event.END_OBJECT) {
                if (parser.getString().substring(0, 2).equalsIgnoreCase("is")) {

                    String[] parts = parser.getString().split(" ");
                    event = parser.next();
                    queryString += convertToNoPrefix(parser.getString()) + " " + parts[1] + " ?s .";
                } else {
                    queryString += "?s " + parser.getString() + " ";
                    event = parser.next();
                    queryString += convertToNoPrefix(parser.getString()) + ". ";
                }
            }
        }
        if (property.substring(0, 2).equalsIgnoreCase("is")) {
            String[] parts = property.split(" ");
            queryString += "?o " + parts[1] + " ?s . ";
        } else {
            queryString += "?s " + property + " ?o . ";
        }
        queryString += "OPTIONAL {?o rdfs:label ?label. }} ORDER BY ?o";
        return queryString;
    }

    public String getIRI(String category) {
        try (BufferedReader br = new BufferedReader(new FileReader(servletContext.getRealPath("/WEB-INF/classes/hadoop/dictionary.txt")))) {

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                List<String> matchList = new ArrayList<>();
                Pattern regex = Pattern.compile("[^\\s\"']+|\"[^\"]*\"|'[^']*'");
                Matcher regexMatcher = regex.matcher(sCurrentLine);
                while (regexMatcher.find()) {
                    matchList.add(regexMatcher.group());
                }
                if (matchList.size() > 0 && matchList.get(0).equalsIgnoreCase(category)) {
                    return matchList.get(1);
                }
            }

        } catch (IOException e) {
        };
        return "";
    }

    public String selectValueSparqlGenerator(String json, String category) {
        JsonParser parser = Json.createParser(new StringReader(json));
        Event event = parser.next();// START_OBJECT
        String iri = getIRI(category);
        String queryString = "SELECT ?s ?label WHERE { ?s rdf:type " + iri + " . ";
        while ((event = parser.next()) != Event.END_OBJECT) {
            if (parser.getString().substring(0, 2).equalsIgnoreCase("is")) {
                String[] parts = parser.getString().split(" ");
                event = parser.next();
                String value = parser.getString();
                value = convertToNoPrefix(value);
                queryString += value + " " + parts[1] + " ?s .";
            } else {
                queryString += "?s " + parser.getString() + " ";
                event = parser.next();
                String value = parser.getString();
                value = convertToNoPrefix(value);
                queryString += value + ". ";
            }
        }
        queryString += "?s rdfs:label ?label. } ORDER BY ?label";
        return queryString;
    }

    public String convertToNoPrefix(String str) {
        try (BufferedReader br = new BufferedReader(new FileReader(servletContext.getRealPath("/WEB-INF/classes/hadoop/prefix.txt")))) {
            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                List<String> matchList = new ArrayList<String>();
                Pattern regex = Pattern.compile("[^\\s\"']+|\"[^\"]*\"|'[^']*'");
                Matcher regexMatcher = regex.matcher(sCurrentLine);
                while (regexMatcher.find()) {
                    matchList.add(regexMatcher.group());
                }
                if (matchList.size() > 0) {
                    str = str.replace(matchList.get(0), matchList.get(1));
                }
            }
            if (str.charAt(0) == '<') {
                str += ">";
            }
        } catch (IOException e) {
            e.printStackTrace();
        };
        return str;
    }

    public String selectResultSparqlGenerator(String result) {
        String subject = convertToNoPrefix(result);
        String queryString = "SELECT DISTINCT ?p ?o ?v ?label WHERE { ";

        queryString += "{" + subject + " ?p ?o. OPTIONAL {?o rdfs:label ?label.}}";
        queryString += "UNION { ";
        queryString += "?v ?p " + subject + " . OPTIONAL {?v rdfs:label ?label.} } }";
        return queryString;
    }

    public ArrayList<String> readFile(String filepath) throws IOException {
        File file = new File(filepath);
        LineIterator it = FileUtils.lineIterator(file, "UTF-8");
        ArrayList<String> results = new ArrayList<>();
        try {
            while (it.hasNext()) {
                String content = it.nextLine();
                if (content.length() > 0 && !content.equals("\"\"")) {
                    results.add(content);
                }
            }
        } finally {
            LineIterator.closeQuietly(it);
        }

        return results;
    }

    public String readFileToJSON(ArrayList<String> data, String category) throws FileNotFoundException {
        System.out.println("readFileToJSON");
        ArrayList<String> result = new ArrayList<>();
        JsonArrayBuilder out = Json.createArrayBuilder();
        JsonObjectBuilder resultObject = Json.createObjectBuilder();
        for (String line : data) {
            List<String> matchList = new ArrayList<>();
            Pattern regex = Pattern.compile("[^\\s\"']+|\"[^\"]*\"|'[^']*'");
            Matcher regexMatcher = regex.matcher(line);
            while (regexMatcher.find()) {
                matchList.add(regexMatcher.group());
                // System.out.println(regexMatcher.group());
            }
            if (matchList.size() >= 2) {
                try (BufferedReader br = new BufferedReader(new FileReader(servletContext.getRealPath("/WEB-INF/classes/hadoop/modified_isValueOfQuery/modified_isValueOfQuery_" + category + ".txt")))) {

                    String sCurrentLine;

                    while ((sCurrentLine = br.readLine()) != null) {
                        if (matchList.get(0).equalsIgnoreCase(sCurrentLine)) {
                            matchList.set(0, "is " + matchList.get(0) + " of");
                            
                            break;
                        }
                    }
                    resultObject.add("value", matchList.get(0));
                    if (matchList.size() >= 2) {
                        resultObject.add("head", matchList.get(1).replace("\"", ""));
                    } else {
                        resultObject.add("head", matchList.get(0).replace("\"", ""));
                    }

                    //  System.out.println(convertToNoPrefix(matchList.get(1)));
                } catch (IOException e) {
                    e.printStackTrace();
                };
            }
            out.add(resultObject);
        }
        return out.build().toString();
    }

    public String readFileToJSON2(ArrayList<String> data, String category) throws FileNotFoundException {
        System.out.println("readFileToJSON");
        ArrayList<String> result = new ArrayList<>();
        JsonArrayBuilder out = Json.createArrayBuilder();
        JsonObjectBuilder resultObject = Json.createObjectBuilder();
        for (String line : data) {
            List<String> matchList = new ArrayList<>();
            Pattern regex = Pattern.compile("[^\\s\"']+|\"[^\"]*\"|'[^']*'");
            Matcher regexMatcher = regex.matcher(line);
            while (regexMatcher.find()) {
                matchList.add(regexMatcher.group());
                // System.out.println(regexMatcher.group());
            }
            //  System.out.println(line);
            //        System.out.println(matchList.size());
            if (matchList.size() >= 2) {
                try (BufferedReader br = new BufferedReader(new FileReader(servletContext.getRealPath("/WEB-INF/classes/hadoop/modified_isValueOfQuery/modified_isValueOfQuery_" + category + ".txt")))) {

                    String sCurrentLine;

                    while ((sCurrentLine = br.readLine()) != null) {

                        if (matchList.get(0).equalsIgnoreCase(sCurrentLine)) {
                            matchList.set(0, "is " + matchList.get(0) + " of");
                            break;
                        }
                    }
                    resultObject.add("name", matchList.get(0));
                    resultObject.add("label", matchList.get(1).replace("\"", ""));

                    if (matchList.size() >= 3) {
                        resultObject.add("url", convertToNoPrefix(matchList.get(2)).replace("<", "").replace(">", ""));
                    } else {
                       //  System.out.println(convertToNoPrefix(matchList.get(1)));
                       // System.out.println(convertToNoPrefix(matchList.get(1)).replace("<", "").replace(">", ""));
                        resultObject.add("url", convertToNoPrefix(matchList.get(1)).replace("<", "").replace(">", ""));
                    }

                    //  System.out.println(convertToNoPrefix(matchList.get(1)));
                } catch (IOException e) {
                    e.printStackTrace();
                };
            }
            out.add(resultObject);
        }
        return out.build().toString();
    }

    public ArrayList<String> replaceString(String filepath) throws IOException {
        File file = new File(filepath);
        LineIterator it = FileUtils.lineIterator(file, "UTF-8");
        ArrayList<String> results = new ArrayList<>();
        try {
            while (it.hasNext()) {
                String content = it.nextLine();
                content = content.replace("^^<http://www.w3.org/2001/XMLSchema#int>", "");
                content = content.replace("<http://xmlns.com/foaf/0.1/page>\n", "");
                content = content.replace("<http://www.w3.org/2002/07/owl#sameAs>\n", "");
                content = content.replace("<http://www.w3.org/2000/01/rdf-schema#label>\n", "");
                content = content.replace("<http://dbpedia.org/property/hasPhotoCollection>\n", "");
                content = content.replace("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>\n", "");
                content = content.replace("<http://www.w3.org/2002/07/owl#", "owl:");
                content = content.replace("<http://www.w3.org/2001/XMLSchema#", "xsd:");
                content = content.replace("<http://www.w3.org/2000/01/rdf-schema#", "rdfs:");
                content = content.replace("<http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:");
                content = content.replace("<http://xmlns.com/foaf/0.1/", "foaf:");
                content = content.replace("<http://data.linkedmdb.org/resource/oddlinker/", "oddlinker:");
                content = content.replace("<file:/C:/d2r-server-0.4/mapping.n3#", "map:");
                content = content.replace("<http://data.linkedmdb.org/resource/movie/", "movie:");
                content = content.replace("<http://data.linkedmdb.org/resource/", "db:");
                content = content.replace("<http://dbpedia.org/property/", "dbpedia:");
                content = content.replace("<http://www.w3.org/2004/02/skos/core#", "skos:");
                content = content.replace("<http://purl.org/dc/terms/", "dc:");
                content = content.replace(">", "");
                content = content.replace("<", "");
                results.add(content);
            }
        } finally {
            LineIterator.closeQuietly(it);
        }
        return results;
    }

    public String countValue(ArrayList<String> in) throws IOException {
        Multiset<String> multiset = HashMultiset.create();
        for (int i = 0; i < in.size(); i++) {
            if (in.get(i).length() > 0 && !in.get(i).equals("\"\"")) {
                multiset.add(in.get(i));
            }
        }
        JsonArrayBuilder out = Json.createArrayBuilder();
        JsonObjectBuilder resultObject = Json.createObjectBuilder();
        for (Multiset.Entry<String> entry : multiset.entrySet()) {
            List<String> matchList = new ArrayList<>();
            Pattern regex = Pattern.compile("[^\\s\"']+|\"[^\"]*\"|'[^']*'");
            Matcher regexMatcher = regex.matcher(entry.getElement());
            while (regexMatcher.find()) {
                matchList.add(regexMatcher.group());
            }
            //System.out.println(matchList.get(0));
            resultObject.add("elem", matchList.get(0));
            resultObject.add("count", entry.getCount());
            if (matchList.size() >= 2) {
                String label = "";
                for (int j = 1; j < matchList.size(); j++) {
                    label += matchList.get(j);
                }
                resultObject.add("label", label);
            } else {
                resultObject.add("label", "");
            }
            out.add(resultObject);
        }

        return out.build().toString();
    }
}
