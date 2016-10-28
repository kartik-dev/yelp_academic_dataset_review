CREATE KEYSPACE yelp_dataset WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
  
USE yelp_dataset;

CREATE TABLE reviews_fact(
  stars decimal PRIMARY KEY,
  stars_count decimal
);