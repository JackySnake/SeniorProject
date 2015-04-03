%default reducerNum '1';

REGISTER PigSPARQL_udf.jar;

-- load input data
indata = LOAD '$inputData' USING pigsparql.rdfLoader.ExNTriplesLoader(' ','expand') as (s,p,o);

-- BGP
f0 = FILTER indata BY s == '<http://data.linkedmdb.org/resource/film/79746>' AND p == '<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>' AND o == '<http://data.linkedmdb.org/resource/movie/film>' ;
f1 = FILTER indata BY s == '<http://data.linkedmdb.org/resource/film/79746>' ;
t1 = FOREACH f1 GENERATE p AS p, o AS o ;
BGP1 = CROSS t0, t1 PARALLEL $reducerNum ;

-- SM_Project
SM_Project = FOREACH BGP1 GENERATE p, o, v ;

-- SM_Distinct
SM_Distinct = DISTINCT SM_Project PARALLEL $reducerNum ;

-- store results into output
STORE SM_Distinct INTO '$outputData' USING PigStorage(' ') ;
