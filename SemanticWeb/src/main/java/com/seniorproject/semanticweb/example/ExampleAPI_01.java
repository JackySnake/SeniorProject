/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.seniorproject.semanticweb.example;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

/**
 *
 * @author mtmmoei
 */
public class ExampleAPI_01 {
     public static void main(String[] args) {
        FileManager.get().addLocatorClassLoader(ExampleAPI_01.class.getClassLoader());
        Model model = FileManager.get().loadModel("data/data.nt", null, "N-TRIPLES");

        StmtIterator iter = model.listStatements();
        try {
            while ( iter.hasNext() ) {
                Statement stmt = iter.next();
                
                Resource s = stmt.getSubject();
                Resource p = stmt.getPredicate();
                RDFNode o = stmt.getObject();
                
                if ( s.isURIResource() ) {
                    System.out.print("URI");
                } else if ( s.isAnon() ) {
                    System.out.print("blank");
                }
                
                if ( p.isURIResource() ) 
                    System.out.print(" URI ");
                
                if ( o.isURIResource() ) {
                    System.out.print("URI");
                } else if ( o.isAnon() ) {
                    System.out.print("blank");
                } else if ( o.isLiteral() ) {
                    System.out.print("literal");
                }
                
                System.out.println();                
            }
        } finally {
            if ( iter != null ) iter.close();
        }
    }
}


