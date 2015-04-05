/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.seniorproject.semanticweb.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author root
 */
@Service
public class HadoopService {
      @Autowired
    ServletContext servletContext;
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

}
