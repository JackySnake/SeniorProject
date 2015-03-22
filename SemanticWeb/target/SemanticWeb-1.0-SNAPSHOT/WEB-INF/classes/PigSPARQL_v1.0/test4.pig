%default reducerNum '1';

REGISTER PigSPARQL_udf.jar;

-- load input data
indata = LOAD '$inputData' USING pigsparql.rdfLoader.ExNTriplesLoader(' ','expand') as (s,p,o);

-- BGP
f0 = FILTER indata BY p == '<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>' AND o == '<http://data.linkedmdb.org/resource/movie/actor>' ;
t0 = FOREACH f0 GENERATE s AS s ;
f1 = FILTER indata BY p == '<http://data.linkedmdb.org/resource/movie/actor_name>' ;
t1 = FOREACH f1 GENERATE s AS s, o AS o ;
BGP1 = JOIN t0 BY s, t1 BY s PARALLEL $reducerNum ;
BGP1 = FOREACH BGP1 GENERATE $0 AS s, $2 AS o ;

-- SM_Project
SM_Project = FOREACH BGP1 GENERATE o ;

-- store results into output
STORE SM_Project INTO '$outputData' USING PigStorage(' ') ;
