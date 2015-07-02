# sql-to-yesql
Creates CRUD YeSQL queries from SQL CREATE TABLE statements

# Usage
Load sql-to-yesql.clj up in your Clojure REPL instance and define two variables:

```
(def input-directory "/Users/sharms/Projects/clj/techstack/resources/migrations")
(def output-directory "/Users/sharms/Projects/clj/techstack/resources/sql")
```

The script will then take anything with 'create' in the name that ends in 'sql', and search for "CREATE TABLE"
statements.  If it finds them, it will output CRUD YeSQL queries in output-directory
