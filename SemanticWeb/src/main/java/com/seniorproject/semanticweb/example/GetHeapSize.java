/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.seniorproject.semanticweb.example;

/**
 *
 * @author mtmmoei
 */

public class GetHeapSize {
    public static void main(String[]args){
         
        //Get the jvm heap size.
        long heapSize = Runtime.getRuntime().totalMemory();
         
        //Print the jvm heap size.
        System.out.println("Heap Size = " + heapSize);
    }
}  

