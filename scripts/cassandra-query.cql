CREATE KEYSPACE yelp_dataset WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
  
USE yelp_dataset;

CREATE TABLE reviews_fact(
  stars decimal PRIMARY KEY,
  stars_count decimal
);

CREATE TABLE top_10restaurants(
  name text PRIMARY KEY,
  state text,
  city text,
  review_count decimal,
  cat text
);