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
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
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
import javax.servlet.ServletContext;
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

    public String convertToJSON(String queryString) throws FileNotFoundException {
        System.out.println("convert");
        JsonArrayBuilder out = Json.createArrayBuilder();
        JsonArrayBuilder resultArray = Json.createArrayBuilder();

        String words[] = queryString.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            if (words[i].equalsIgnoreCase("select")) {
                int j = i + 1;
                while (!words[j].equalsIgnoreCase("where")) {
                    resultArray.add(words[j].substring(1));
                    j++;
                }
                break;
            }
        }
        out.add(resultArray);
        try (BufferedReader br = new BufferedReader(new FileReader(servletContext.getRealPath("/WEB-INF/classes/data/data.nt")))) {

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
        File file = new File(servletContext.getRealPath("/WEB-INF/resources/hadoop/"), "test1.sparql");

        if (file.createNewFile()) {
            System.out.println("File is created!");
        } else {
            System.out.println("File already exists.");
        }
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(prefix + queryString);
        bw.close();

        BufferedReader br = null;

        try {

            String sCurrentLine;

            br = new BufferedReader(new FileReader(servletContext.getRealPath("/WEB-INF/resources/hadoop/test1.sparql")));

            while ((sCurrentLine = br.readLine()) != null) {
                System.out.println(sCurrentLine);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        test();
        return "";
    }

    private void test() throws IOException, InterruptedException {
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
    }

    private void converSparql() throws IOException, InterruptedException {
        System.out.println("converSparql");
        File file = new File(servletContext.getRealPath("/WEB-INF/resources/hadoop/"), "test3.pig");
        if (file.createNewFile()) {
            System.out.println("File is created!");
        } else {
            System.out.println("File already exists.");
        }
//src/main/resources/PigSPARQL_v1.0/test1.sparql 
        //servletContext.getRealPath("/WEB-INF/resources/PigSPARQL_v1.0/PigSPARQL_main.jar")
        Process ps = Runtime.getRuntime().exec("java"
                + " -jar " + servletContext.getRealPath("/WEB-INF/resources/PigSPARQL_v1.0/PigSPARQL_main.jar") + "  -e "
                + "-i " + servletContext.getRealPath("/WEB-INF/resources/hadoop/test1.sparql")
                + " -o " + servletContext.getRealPath("/WEB-INF/resources/hadoop/test3.pig") + " -opt");
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
        String sReadFileName = servletContext.getRealPath("/WEB-INF/resources/hadoop/test3.pig");
        File file = new File(servletContext.getRealPath("/WEB-INF/resources/hadoop/"), "test4.pig");
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
        Process ps2 = Runtime.getRuntime().exec("pig -param inputData='/user/admin/SeniorData/linkedmdb-latest-dump.nt' -param outputData='/user/admin/SeniorData/out4' -param reducerNum='12' src/main/resources/PigSPARQL_v1.0/test4.pig");
        ps2.waitFor();
        java.io.InputStream is2 = ps2.getInputStream();
        byte b2[] = new byte[is2.available()];
        is2.read(b2, 0, b2.length);
        System.out.println(new String(b2));

    }

    private void mergeHadoopFile() throws IOException, InterruptedException {
        System.out.println("merge");
        File file = new File(servletContext.getRealPath("/WEB-INF/resources/hadoop/"), "output.txt");
        if (file.createNewFile()) {
            System.out.println("File is created!");
        } else {
            System.out.println("File already exists.");
        }
        Process ps = Runtime.getRuntime().exec("hadoop fs -getmerge /user/admin/SeniorData/out4 " + servletContext.getRealPath("/WEB-INF/resources/hadoop/output,txt"));
        // Then retreive the process output
        //     InputStream in = proc.getInputStream();
        //   InputStream err = proc.getErrorStream();
        ps.waitFor();
        java.io.InputStream is = ps.getInputStream();
        byte b[] = new byte[is.available()];
        is.read(b, 0, b.length);
        System.out.println(new String(b));
    }
}
