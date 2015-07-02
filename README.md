# sql-to-yesql
Creates CRUD YeSQL queries from SQL CREATE TABLE statements

# What is this?
If you create a website using the [Luminus Web Framework](http://www.luminusweb.net), you will probably want a database to make your website interactive.  YeSQL is the default way to create easy to use functions to access the database.  This framework takes the migrations you create in /resources/migrations and will generate simple queries for CRUD operations for you.

# Usage
Load sql-to-yesql.clj up in your Clojure REPL instance and define two variables:

```
(def input-directory "/Users/sharms/Projects/clj/techstack/resources/migrations")
(def output-directory "/Users/sharms/Projects/clj/techstack/resources/sql")
```

The script will take anything with 'create' in the name that ends in 'sql', and search for "CREATE TABLE"
statements.  If it finds them, it will output CRUD YeSQL queries in output-directory

To run it just call the create-yesql-queries function:
```
(create-yesql-queries)
```

When you check out output-directory, it will contain YeSQL formatted crud operations like:

```
-- name: create-business!
-- create a new business
INSERT INTO business (id, description, variance_id) VALUES (:id, :description, :variance_id)

-- name: get-business
SELECT * FROM business
WHERE id = :id

-- name: get-all-business
SELECT * FROM business

-- name: update-business!
UPDATE business SET
description = :description, variance_id = :variance_id
WHERE id = :id

-- name: delete-business!
DELETE FROM business WHERE id = :id
```
