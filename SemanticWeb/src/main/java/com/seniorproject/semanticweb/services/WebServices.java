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

    public String genSparql(String keyword, String type) {
        String queryString = "";

        switch (type) {
            case "Movie":
                queryString = "SELECT ?movie WHERE { "
                        + "?movie rdf:type movie:film . "
                        + "?movie dc:title '" + keyword + "' .}";
                break;
            case "Mailbox":
                queryString = "SELECT * WHERE { "
                        + "?person foaf:mbox <" + keyword + "> . "
                        + "?person foaf:mbox ?mailbox .}";
                break;
            default:
                queryString = "Invalid";
                break;
        }
        return queryString;
    }

    public String convertToJSON(String queryString, String filepath) throws FileNotFoundException {
        System.out.println("convert");
        JsonArrayBuilder out = Json.createArrayBuilder();
        JsonArrayBuilder resultArray = Json.createArrayBuilder();

        String words[] = queryString.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            if (words[i].equalsIgnoreCase("select")) {
                int j = i + 1;
                if(words[j].equalsIgnoreCase("distinct")) j++;
                while (!words[j].equalsIgnoreCase("where")) {
                    resultArray.add(words[j].substring(1));
                    j++;
                }
                break;
            }
        }
        out.add(resultArray);
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                List<String> matchList = new ArrayList<String>();
                Pattern regex = Pattern.compile("[^\\s\"']+|\"[^\"]*\"|'[^']*'");
                Matcher regexMatcher = regex.matcher(sCurrentLine);
                while (regexMatcher.find()) {
                    matchList.add(regexMatcher.group());
                }

                for (int i = 0; i < matchList.size(); i++) {
                    resultArray.add(matchList.get(i));
                }
                out.add(resultArray);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        ;
        return out.build().toString();
    }

    public String queryHadoop(String queryString) throws IOException, InterruptedException {
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
        File file = new File(servletContext.getRealPath("/WEB-INF/classes/PigSPARQL_v1.0/"), "test1.sparql");

        if (file.createNewFile()) {
            System.out.println("File is created!");
        } else {
            System.out.println("File already exists.");
        }
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(prefix + queryString);
        bw.close();
//
//        BufferedReader br = null;
//
//        try {
//
//            String sCurrentLine;
//
//            br = new BufferedReader(new FileReader(servletContext.getRealPath("/WEB-INF/classes/hadoop/test1.sparql")));
//
//            while ((sCurrentLine = br.readLine()) != null) {
//                System.out.println(sCurrentLine);
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (br != null) {
//                    br.close();
//                }
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//        }
        String filePath = test();
        return filePath;
    }

    private String test() throws IOException, InterruptedException {
        System.out.println("Working Directory = "
                + System.getProperty("user.dir"));
        // convert sparql file into pig calling pigsparql main file
        converSparql();

        // modifield text file so it can running because when using prefix pigsparql will bug
        modifiedPig();

        //delete folder before running pig
        deleteFolderFromHadoop();

        //Then call pig file and query on HDFS using pigsparql 
        //  Process
        runningPig();

        //merge file back into local
        mergeHadoopFile();
        return servletContext.getRealPath("/WEB-INF/classes/PigSPARQL_v1.0/output.txt");
    }

    private void converSparql() throws IOException, InterruptedException {
        System.out.println("converSparql");
        File file = new File(servletContext.getRealPath("/WEB-INF/classes/PigSPARQL_v1.0/"), "test3.pig");
        if (file.createNewFile()) {
            System.out.println("File is created!");
        } else {
            System.out.println("File already exists.");
        }
//src/main/resources/PigSPARQL_v1.0/test1.sparql 
        //servletContext.getRealPath("/WEB-INF/resources/PigSPARQL_v1.0/PigSPARQL_main.jar")
        System.out.println("java"
                + " -jar " + servletContext.getRealPath("/WEB-INF/classes/PigSPARQL_v1.0/PigSPARQL_main.jar") + "  -e "
                + "-i " + servletContext.getRealPath("/WEB-INF/classes/PigSPARQL_v1.0/test1.sparql")
                + " -o " + servletContext.getRealPath("/WEB-INF/classes/PigSPARQL_v1.0/test3.pig") + " -opt");
        Process ps = Runtime.getRuntime().exec("java"
                + " -jar " + servletContext.getRealPath("/WEB-INF/classes/PigSPARQL_v1.0/PigSPARQL_main.jar") + "  -e "
                + "-i " + servletContext.getRealPath("/WEB-INF/classes/PigSPARQL_v1.0/test1.sparql")
                + " -o " + servletContext.getRealPath("/WEB-INF/classes/PigSPARQL_v1.0/test3.pig") + " -opt");
        // Then retreive the process output
        //     InputStream in = proc.getInputStream();
        //   InputStream err = proc.getErrorStream();
        ps.waitFor();
        java.io.InputStream is = ps.getInputStream();
        byte b[] = new byte[is.available()];
        is.read(b, 0, b.length);
        System.out.println(new String(b));

//BufferedReader br = null;
// 
//		try {
// System.out.println(servletContext.getRealPath("/WEB-INF/resources/PigSPARQL_v1.0/PigSPARQL_main.jar"));
//			String sCurrentLine;
// 
//			br = new BufferedReader(new FileReader(servletContext.getRealPath("/WEB-INF/resources/hadoop/test3.pig")));
// 
//			while ((sCurrentLine = br.readLine()) != null) {
//				System.out.println(sCurrentLine);
//			}
// 
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				if (br != null)br.close();
//			} catch (IOException ex) {
//				ex.printStackTrace();
//			}
//		}
    }

    private void modifiedPig() throws IOException {
        System.out.println("modifiedPig");
        String sReadFileName = servletContext.getRealPath("/WEB-INF/classes/PigSPARQL_v1.0/test3.pig");
        File file = new File(servletContext.getRealPath("/WEB-INF/classes/PigSPARQL_v1.0/"), "test4.pig");
        if (file.createNewFile()) {
            System.out.println("File is created!");
        } else {
            System.out.println("File already exists.");
        }
        String sWriteFileName = file.toString();

        String sReplaceText = "indata = LOAD '$inputData' USING pigsparql.rdfLoader.ExNTriplesLoader(' ','expand') as (s,p,o)";
        String sReadLine = null;

        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = new FileReader(sReadFileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            FileWriter fileWriter = new FileWriter(sWriteFileName);

            // Always wrap FileWriter in BufferedWriter.
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            while ((sReadLine = bufferedReader.readLine()) != null) {
                System.out.println(sReadLine);
                if (sReadLine.equals("indata = LOAD '$inputData' USING pigsparql.rdfLoader.ExNTriplesLoader(' ','expand') ;")) {
                    bufferedWriter.write("indata = LOAD '$inputData' USING pigsparql.rdfLoader.ExNTriplesLoader(' ','expand') as (s,p,o);");
                } else {
                    bufferedWriter.write(sReadLine);
                }
                bufferedWriter.newLine();

            }

            // Always close files.
            bufferedReader.close();
            bufferedWriter.close();
        } catch (FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '"
                    + sReadFileName + "'");
        } catch (IOException ex) {
            System.out.println(
                    "Error reading file '"
                    + sReadFileName + "'");
            // Or we could just do this: 
            // ex.printStackTrace();
        }
    }

    private void deleteFolderFromHadoop() throws IOException, InterruptedException {
        System.out.println("delete");
        // TODO Auto-generated method stub
        Process ps2 = Runtime.getRuntime().exec("hadoop fs -rm -r /user/admin/SeniorData/out4");
        ps2.waitFor();
        java.io.InputStream is2 = ps2.getInputStream();
        byte b2[] = new byte[is2.available()];
        is2.read(b2, 0, b2.length);
        System.out.println(new String(b2));
    }

    private void runningPig() throws InterruptedException, IOException {
        // TODO Auto-generated method stub
        System.out.println("runningPig");
        System.out.println(servletContext.getRealPath("/WEB-INF/classes/PigSPARQL_v1.0/test4.pig"));
        Process ps2 = Runtime.getRuntime().exec("pig -param inputData='/user/admin/SeniorData/linkedmdb-latest-dump.nt' "
                + "-param outputData='/user/admin/SeniorData/out4' -param reducerNum='12' "
                + servletContext.getRealPath("/WEB-INF/classes/PigSPARQL_v1.0/test4.pig"));
        ps2.waitFor();
        java.io.InputStream is2 = ps2.getInputStream();
        byte b2[] = new byte[is2.available()];
        is2.read(b2, 0, b2.length);
        System.out.println(new String(b2));

    }

    private void mergeHadoopFile() throws IOException, InterruptedException {
        System.out.println("merge");
        File file = new File(servletContext.getRealPath("/WEB-INF/classes/PigSPARQL_v1.0/"), "output.txt");
        if (file.createNewFile()) {
            System.out.println("File is created!");
        } else {
            System.out.println("File already exists.");
        }
        Process ps = Runtime.getRuntime().exec("hadoop fs -getmerge /user/admin/SeniorData/out4 " + servletContext.getRealPath("/WEB-INF/classes/PigSPARQL_v1.0/output.txt"));
        // Then retreive the process output
        //     InputStream in = proc.getInputStream();
        //   InputStream err = proc.getErrorStream();
        ps.waitFor();
        java.io.InputStream is = ps.getInputStream();
        byte b[] = new byte[is.available()];
        is.read(b, 0, b.length);
        System.out.println(new String(b));
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

    public String selectValueSparqlGenerator(String json,String category) {
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
            if(str.charAt(0)=='<'){
            str+=">";    
            }
        } catch (IOException e) {
            e.printStackTrace();
        };
        return str;
    }

    public String selectResultSparqlGenerator(String result,String category) {
        String subject = convertToNoPrefix(result);
        String iri = getIRI(category);
        String queryString = "SELECT DISTINCT ?p ?o ?v WHERE { "+subject+" rdf:type " + iri + " . ";
   
        queryString += "{"+subject+" ?p ?o. }";
        queryString += "UNION { ";
        queryString += "?v ?p "+subject+" .}}";
        queryString += "ORDER BY ?p ?o ?v";
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
            }
            if (matchList.size() >= 2) {
                try (BufferedReader br = new BufferedReader(new FileReader(servletContext.getRealPath("/WEB-INF/classes/hadoop/modified_isValueOfQuery/modified_isValueOfQuery_" + category + ".txt")))) {

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                if(matchList.get(0).equalsIgnoreCase(sCurrentLine)){
                    matchList.set(0, "is "+matchList.get(0)+" of");
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        };
                resultObject.add("name", matchList.get(0));
                resultObject.add("value", matchList.get(1).replace("\"", ""));
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
                content = content.replaceAll("^^<http://www.w3.org/2001/XMLSchema#int>", "");
                content = content.replaceAll("<http://xmlns.com/foaf/0.1/page>\n", "");
                content = content.replaceAll("<http://www.w3.org/2002/07/owl#sameAs>\n", "");
                content = content.replaceAll("<http://www.w3.org/2000/01/rdf-schema#label>\n", "");
                content = content.replaceAll("<http://dbpedia.org/property/hasPhotoCollection>\n", "");
                content = content.replaceAll("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>\n", "");
                content = content.replaceAll("<http://www.w3.org/2002/07/owl#", "owl:");
                content = content.replaceAll("<http://www.w3.org/2001/XMLSchema#", "xsd:");
                content = content.replaceAll("<http://www.w3.org/2000/01/rdf-schema#", "rdfs:");
                content = content.replaceAll("<http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:");
                content = content.replaceAll("<http://xmlns.com/foaf/0.1/", "foaf:");
                content = content.replaceAll("<http://data.linkedmdb.org/resource/oddlinker/", "oddlinker:");
                content = content.replaceAll("<file:/C:/d2r-server-0.4/mapping.n3#", "map:");
                content = content.replaceAll("<http://data.linkedmdb.org/resource/movie/", "movie:");
                content = content.replaceAll("<http://data.linkedmdb.org/resource/", "db:");
                content = content.replaceAll("<http://dbpedia.org/property/", "dbpedia:");
                content = content.replaceAll("<http://www.w3.org/2004/02/skos/core#", "skos:");
                content = content.replaceAll("<http://purl.org/dc/terms/", "dc:");
                content = content.replaceAll(">", "");
                content = content.replaceAll("<", "");
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
            resultObject.add("elem", entry.getElement());
            resultObject.add("count", entry.getCount());
            // System.out.println("elem : "+entry.getElement()+" count : "+entry.getCount());
            out.add(resultObject);
        }

        return out.build().toString();
    }
}
