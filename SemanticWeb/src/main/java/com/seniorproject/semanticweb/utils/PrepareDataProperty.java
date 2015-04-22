/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.seniorproject.semanticweb.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author mtmmoei
 */
public class PrepareDataProperty {
    public static String prefix
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
    public static void main(String[] args) throws IOException {
        getPropertyFromHadoop();
        modifyProperty();
    }

    private static void getPropertyFromHadoop() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/hadoop/dictionary.txt"))) {
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                List<String> matchList = new ArrayList<String>();
                Pattern regex = Pattern.compile("[^\\s\"']+|\"[^\"]*\"|'[^']*'");
                Matcher regexMatcher = regex.matcher(sCurrentLine);
                while (regexMatcher.find()) {
                    matchList.add(regexMatcher.group());
                }
                String category="";
                if (matchList.size() > 0) {
                    File file = new File("src/main/resources/hadoop/propertyQuery_/", "propertyQuery_" + matchList.get(0) + ".sparql");
                    String queryString = "SELECT DISTINCT ?property "
                            + "WHERE {"
                            + "?subject rdf:type " + category + "."
                            + "?subject ?property ?value. }";
                    FileWriter fw = new FileWriter(file.getAbsoluteFile());
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write(prefix + queryString);
                    bw.close();
                    
                    file = new File("src/main/resources/hadoop/isValueOfQuery/", "isValueOfQuery_" + matchList.get(0) + ".sparql");
                    queryString = "SELECT DISTINCT ?property "
                            + "WHERE {"
                            + "?subject rdf:type " + category + "."
                            + "?isValueOf ?property ?subject. }";
                    fw = new FileWriter(file.getAbsoluteFile());
                    bw = new BufferedWriter(fw);
                    bw.write(prefix + queryString);
                    bw.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        };

    }

    private static void modifyProperty() throws IOException {
        File folder = new File("src/main/resources/hadoop/propertyQuery");
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            Path path = Paths.get("src/main/resources/hadoop/propertyQuery/" + listOfFiles[i].getName());
            Charset charset = StandardCharsets.UTF_8;

            String content = new String(Files.readAllBytes(path), charset);
            content = content.replaceAll("<http://xmlns.com/foaf/0.1/page>\n", "");
            content = content.replaceAll("<http://www.w3.org/2002/07/owl#sameAs>\n", "");
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

            Files.write(Paths.get("src/main/resources/hadoop/modified_propertyQuery/modified_" + listOfFiles[i].getName()), content.getBytes(charset));
        }
    }

}
