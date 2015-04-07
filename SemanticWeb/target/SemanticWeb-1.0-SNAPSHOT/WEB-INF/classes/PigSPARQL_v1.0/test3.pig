%default reducerNum '1';

REGISTER PigSPARQL_udf.jar;

-- load input data
indata = LOAD '$inputData' USING pigsparql.rdfLoader.ExNTriplesLoader(' ','expand') ;

-- BGP
f0 = FILTER indata BY s == '<http://data.linkedmdb.org/resource/film/23483>' ;
BGP1 = FOREACH f0 GENERATE p AS property, o AS value ;

-- BGP
f0 = FILTER indata BY p == '<http://www.w3.org/2000/01/rdf-schema#label>' ;
BGP2 = FOREACH f0 GENERATE s AS value, o AS label ;

-- OPTIONAL
lj = JOIN BGP1 BY value LEFT OUTER, BGP2 BY value PARALLEL $reducerNum ;
OPTIONAL1 = FOREACH lj GENERATE BGP1::property AS property, BGP1::value AS value, BGP2::label AS label ;

-- BGP
f0 = FILTER indata BY o == '<http://data.linkedmdb.org/resource/film/23483>' ;
BGP3 = FOREACH f0 GENERATE s AS isValueOf, p AS property ;

-- BGP
f0 = FILTER indata BY p == '<http://www.w3.org/2000/01/rdf-schema#label>' ;
BGP4 = FOREACH f0 GENERATE s AS isValueOf, o AS label ;

-- OPTIONAL
lj = JOIN BGP3 BY isValueOf LEFT OUTER, BGP4 BY isValueOf PARALLEL $reducerNum ;
OPTIONAL2 = FOREACH lj GENERATE BGP3::isValueOf AS isValueOf, BGP3::property AS property, BGP4::label AS label ;

-- UNION
u1 = FOREACH OPTIONAL1 GENERATE property, label, value, null AS isValueOf ;
u2 = FOREACH OPTIONAL2 GENERATE property, label, null AS value, isValueOf ;
UNION1 = UNION u1, u2 ;

-- SM_Distinct
SM_Distinct = DISTINCT UNION1 PARALLEL $reducerNum ;

-- store results into output
STORE SM_Distinct INTO '$outputData' USING PigStorage(' ') ;
