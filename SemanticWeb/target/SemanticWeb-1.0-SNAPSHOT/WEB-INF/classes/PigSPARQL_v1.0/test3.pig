%default reducerNum '1';

REGISTER PigSPARQL_udf.jar;

-- load input data
indata = LOAD '$inputData' USING pigsparql.rdfLoader.ExNTriplesLoader(' ','expand') ;

-- BGP
f0 = FILTER indata BY p == '<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>' AND o == '<http://data.linkedmdb.org/resource/movie/actor>' ;
t0 = FOREACH f0 GENERATE s AS s ;
f1 = FILTER indata BY p == '<http://www.w3.org/2000/01/rdf-schema#label>' AND o == '"Fuzzy Knight (Actor)"' ;
t1 = FOREACH f1 GENERATE s AS s ;
t2 = FOREACH indata GENERATE s AS s, p AS p, o AS o ;
BGP1 = JOIN t0 BY s, t1 BY s, t2 BY s PARALLEL $reducerNum ;
BGP1 = FOREACH BGP1 GENERATE $0 AS s, $3 AS p, $4 AS o ;

-- BGP
BGP2 = FOREACH indata GENERATE s AS v, p AS p, o AS s ;

-- UNION
u1 = FOREACH BGP1 GENERATE s, p, o, null AS v ;
u2 = FOREACH BGP2 GENERATE s, p, null AS o, v ;
UNION1 = UNION u1, u2 ;

-- SM_Order
SM_Order = ORDER UNION1 BY p, o, v PARALLEL $reducerNum ;

-- SM_Project
SM_Project = FOREACH SM_Order GENERATE p, o, v ;

-- SM_Distinct
SM_Distinct = DISTINCT SM_Project PARALLEL $reducerNum ;

-- store results into output
STORE SM_Distinct INTO '$outputData' USING PigStorage(' ') ;
