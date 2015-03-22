package com.seniorproject.semanticweb.example;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class QueryHadoop {
	
	public static void main(String[] args) throws IOException, InterruptedException {
                System.out.println("Working Directory = " +
              System.getProperty("user.dir"));
                String sparqlFileName = "test1";
                String pigFileName = "test3";
                String modifiedPigFileName = "test4";
                String outputFileName = "src/main/resources/PigSPARQL_v1.0/out";
		// convert sparql file into pig calling pigsparql main file
                    converSparql(sparqlFileName,pigFileName);
		
		        
		// modifield text file so it can running because when using prefix pigsparql will bug
			modifiedPig(pigFileName,modifiedPigFileName);
			
		//delete folder before running pig
       			deleteFolderFromHadoop();

	    //Then call pig file and query on HDFS using pigsparql 
	      //  Process
		runningPig(modifiedPigFileName);
                
            //merge file back into local
                
                mergeHadoopFile(outputFileName);
		            
	}

	private static void deleteFolderFromHadoop() throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		Process ps2 = Runtime.getRuntime().exec("hadoop fs -rm -r /user/admin/SeniorData/out4");
        ps2.waitFor();
        java.io.InputStream is2=ps2.getInputStream();
        byte b2[]=new byte[is2.available()];
        is2.read(b2,0,b2.length);
        System.out.println(new String(b2));
	}

	private static void runningPig(String modifiedPigFileName) throws InterruptedException, IOException {
		// TODO Auto-generated method stub
		
		Process ps2 = Runtime.getRuntime().exec("pig -param inputData='/user/admin/SeniorData/linkedmdb-latest-dump.nt' -param outputData='/user/admin/SeniorData/out4' -param reducerNum='12' src/main/resources/PigSPARQL_v1.0/"+modifiedPigFileName+".pig");
        ps2.waitFor();
        java.io.InputStream is2=ps2.getInputStream();
        byte b2[]=new byte[is2.available()];
        is2.read(b2,0,b2.length);
        System.out.println(new String(b2));
		
	}

	private static void modifiedPig(String pigFileName,String modifiedPigFileName) {
		String sReadFileName = "src/main/resources/PigSPARQL_v1.0/"+pigFileName+".pig";
		String sWriteFileName = "src/main/resources/PigSPARQL_v1.0/"+modifiedPigFileName+".pig";
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

	            while((sReadLine = bufferedReader.readLine()) != null) {
	                System.out.println(sReadLine);
	                if(sReadLine.equals("indata = LOAD '$inputData' USING pigsparql.rdfLoader.ExNTriplesLoader(' ','expand') ;")){
	                	bufferedWriter.write("indata = LOAD '$inputData' USING pigsparql.rdfLoader.ExNTriplesLoader(' ','expand') as (s,p,o);");
	                }
	                else {
	                bufferedWriter.write(sReadLine);
	                }
	                bufferedWriter.newLine();
	                
	            }    

	            // Always close files.
	            bufferedReader.close();
	            bufferedWriter.close();
	        }
	        catch(FileNotFoundException ex) {
	            System.out.println(
	                "Unable to open file '" + 
	                		sReadFileName + "'");                
	        }
	        catch(IOException ex) {
	            System.out.println(
	                "Error reading file '" 
	                + sReadFileName + "'");                   
	            // Or we could just do this: 
	            // ex.printStackTrace();
	        }
	}



	private static void converSparql(String sparqlFileName, String pigFileName) throws IOException, InterruptedException {
		
		Process ps = Runtime.getRuntime().exec("java -jar src/main/resources/PigSPARQL_v1.0/PigSPARQL_main.jar -e -i src/main/resources/PigSPARQL_v1.0/"+sparqlFileName+".sparql -o src/main/resources/PigSPARQL_v1.0/"+pigFileName+".pig -opt");
	     // Then retreive the process output
	   //     InputStream in = proc.getInputStream();
	     //   InputStream err = proc.getErrorStream();
			 	ps.waitFor();
		        java.io.InputStream is=ps.getInputStream();
		        byte b[]=new byte[is.available()];
		        is.read(b,0,b.length);
		        System.out.println(new String(b));
		
	}

    private static void mergeHadoopFile(String outputFileName) throws IOException, InterruptedException {
        Process ps = Runtime.getRuntime().exec("hadoop fs -getmerge /user/admin/SeniorData/out4 "+outputFileName+".txt");
	     // Then retreive the process output
	   //     InputStream in = proc.getInputStream();
	     //   InputStream err = proc.getErrorStream();
			 	ps.waitFor();
		        java.io.InputStream is=ps.getInputStream();
		        byte b[]=new byte[is.available()];
		        is.read(b,0,b.length);
		        System.out.println(new String(b));
    }
        
      
	
	

}
