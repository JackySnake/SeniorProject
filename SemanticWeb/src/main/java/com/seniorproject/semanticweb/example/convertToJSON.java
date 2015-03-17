/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.seniorproject.semanticweb.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

/**
 *
 * @author mtmmoei
 */
public class convertToJSON {

    public static void main(String[] args) {
        String queryString
                = "SELECT ?s ?p ?o  "
                + "WHERE { ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> . "
                + "?s ?p ?o .}";
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
        System.out.println(out.build().toString());
    }

}
