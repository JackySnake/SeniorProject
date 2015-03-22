===================================================================================
   PigSPARQL v1.0  -  A SPARQL query processor for MapReduce based on Apache Pig
===================================================================================

PigSPARQL is a translation from SPARQL 1.0 to Pig Latin,
which allows to execute SPARQL queries on large RDF graphs with MapReduce.
More information: http://dl.acm.org/authorize?432963

Project website:
http://dbis.informatik.uni-freiburg.de/PigSPARQL

For more information about Pig Latin visit:
http://pig.apache.org/

NOTE: The optional vertical partitioning feature is currently not available!
We are currently working on an update to fully automate the partitioning process.
We expect the update to be available at the end of 08/2013.
Check the project website for updates.

If you have any questions, feel free to contact us:
schaetzle@informatik.uni-freiburg.de


Instructions:
#############
The current version of PigSPARQL uses a two step process to execute a SPARQL query.

(1) The query must be translated into a Pig Latin script.
    You can do this on any machine where Java 1.6 or higher is installed.

usage: PigSPARQL [-d <value>] [-e] [-h] -i <file> [-o <file>] [-opt]
 -d,--delimiter <value>   delimiter used in RDF triples if not whitespace
 -e,--expand              expand URI prefixes
 -h,--help                print this message
 -i,--input <file>        SPARQL query file to translate
 -o,--output <file>       Pig output script file
 -opt,--optimize          turn on SPARQL algebra optimization
 
The option -i is required, all other options are optional.
If -o is not specified, the output script is created as <input>.pig.
 
Example:  java -jar PigSPARQL_main.jar -i ./q1.sparql -o ./q1.pig


(2) The generated Pig Latin script is executed on the MapReduce cluster using Pig.
    Please note that Pig must be installed to execute the query!
	We recommend CDH4 and the version of Pig shipped with CDH4.
	For more information visit: http://www.cloudera.com/downloads/
	 
You must specify the input and output path in HDFS and optionally the number of reducers to use.
If the number of reducers to use is not given, only one reducer is used by default.
The optimal number of reducers depends on the cluster size and cluster hardware.
A general rule of thumb is (#nodes * #cores_per_node).

Example:  pig -param inputData='dblp10K.n3' -param outputData='out' -param reducerNum='18' q1.pig

Please make sure that runtime library PigSPARQL_udf.jar is in the same folder as the Pig Latin script!
All other libraries and also PigSPARQL_main.jar is not used at query runtime.