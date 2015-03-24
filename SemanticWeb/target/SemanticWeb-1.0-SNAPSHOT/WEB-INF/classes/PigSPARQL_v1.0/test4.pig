%default reducerNum '1';

REGISTER PigSPARQL_udf.jar;

-- load input data
indata = LOAD '$inputData' USING pigsparql.rdfLoader.ExNTriplesLoader(' ','expand') as (s,p,o) ;

-- BGP
f0 = FILTER indata BY p == '<http://data.linkedmdb.org/resource/movie/filmid>' ;
t0 = FOREACH f0 GENERATE s AS resource, o AS uri ;
f1 = FILTER indata BY p == '<http://purl.org/dc/terms/title>' AND o == '"Forrest Gump"' ;
t1 = FOREACH f1 GENERATE s AS resource ;
BGP1 = JOIN t0 BY resource, t1 BY resource PARALLEL $reducerNum ;
BGP1 = FOREACH BGP1 GENERATE $0 AS resource, $1 AS uri ;

-- SM_Order
SM_Order = ORDER BGP1 BY resource PARALLEL $reducerNum ;

-- SM_Project
SM_Project = FOREACH SM_Order GENERATE resource ;

-- store results into output
STORE SM_Project INTO '$outputData' USING PigStorage(' ') ;
