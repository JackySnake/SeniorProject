%default reducerNum '1';

REGISTER PigSPARQL_udf.jar;

-- load input data
indata = LOAD '$inputData' USING pigsparql.rdfLoader.ExNTriplesLoader(' ','expand') as (s,p,o);

-- BGP
f0 = FILTER indata BY p == '<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>' AND o == '<http://data.linkedmdb.org/resource/movie/music_contributor>' ;
t0 = FOREACH f0 GENERATE s AS s ;
f1 = FILTER indata BY p == '<http://data.linkedmdb.org/resource/movie/music_contributor_name>' AND o == '"Zakir Hussain"' ;
t1 = FOREACH f1 GENERATE s AS s ;
f2 = FILTER indata BY p == '<http://www.w3.org/2000/01/rdf-schema#label>' ;
t2 = FOREACH f2 GENERATE s AS s, o AS label ;
BGP1 = JOIN t0 BY s, t1 BY s, t2 BY s PARALLEL $reducerNum ;
BGP1 = FOREACH BGP1 GENERATE $0 AS s, $3 AS label ;

-- store results into output
STORE BGP1 INTO '$outputData' USING PigStorage(' ') ;
