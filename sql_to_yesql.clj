(ns techstack.util.sql-to-yesql
  (:require [clojure.java.io :as io]))

(def input-directory "/Users/sharms/Projects/clj/techstack/resources/migrations")
(def output-directory "/Users/sharms/Projects/clj/techstack/resources/sql")
(def create-regex #"\s*CREATE\s+")
(def fields-regex #"\s*(\w+)\s+((?i:SERIAL|BOOLEAN|VARCHAR|TEXT|INTEGER|DATE|TIME))(\s+|,)")

(defn get-table-name [q]
  (first (last (re-seq #"(\w+)" q))))

(defn get-table-fields [q]
  (let [[_ field type] (first (re-seq fields-regex q))]
    { (keyword field) type }
))

(defn get-fields [schema]
  (map #(clojure.string/replace-first % ":" "")
          (->> schema :fields (map #(first %)))))

(defn crud-create [wrtr schema]
  (let [table-name (:table-name schema)
        insert-fields (apply str (interpose ", " (get-fields schema)))
        keyword-fields (apply str (interpose ", " (map keyword (get-fields schema))))]
    (.write wrtr (apply str 
                        (str "-- name: create-" table-name "!\n")
                        (str "-- create a new " table-name "\n")
                        (str "INSERT INTO " table-name " (" insert-fields)
                        (str ") VALUES (" keyword-fields)
                        (str ")\n\n")))))

(defn crud-read [wrtr schema]
  (let [table-name (:table-name schema)]
    (.write wrtr  (apply str 
                         (str "-- name: get-" table-name "\n")
                         (str "SELECT * FROM " table-name "\n")
                         (str "WHERE id = :id\n\n")))))

(defn crud-read-all [wrtr schema]
  (let [table-name (:table-name schema)]
    (.write wrtr  (apply str 
                         (str "-- name: get-all-" table-name "\n")
                         (str "SELECT * FROM " table-name "\n\n")))))

(defn crud-update [wrtr schema]
  (let [table-name (:table-name schema)
        fields (dissoc (:fields schema) :id)
        field-names (map first fields)
        field-names-no-colon (map name field-names)
        fields-joined (map vector field-names-no-colon field-names)
        fields-assigned (map #(interpose " = " %) fields-joined)
        updates-line (apply str (flatten (interpose ", " fields-assigned)))]
    (.write wrtr  (apply str 
                         (str "-- name:update-" table-name "!\n")
                         (str "UPDATE " table-name " SET\n")
                         (str updates-line "\n")
                         (str "WHERE id = :id\n\n")))))

(defn crud-delete [wrtr schema]
  (let [table-name (:table-name schema)]
    (.write wrtr  (apply str (str "-- name:delete-" table-name "!\n")
                         (str "DELETE FROM " table-name " WHERE id = :id\n\n")))))

(defn parse-line [sql line]
  (cond
   (re-find create-regex line) (merge sql {:table-name (get-table-name line)})
   (re-find fields-regex line) (assoc-in sql [:fields] (merge (:fields sql) (get-table-fields line))) :else sql))

(defn parse-sql [file]
  (with-open [rdr (io/reader file)]
    (reduce parse-line {} (line-seq rdr))))

(defn create-yesql-queries-file [filename schema]
  (with-open [wrtr (io/writer filename)]
    (let [create-yesql (crud-create wrtr schema)
          read-yesql (crud-read wrtr schema)
          read-all-yesql (crud-read-all wrtr schema)
          update-yesql (crud-update wrtr schema)
          delete-yesql (crud-delete wrtr schema)])
    ))

(defn process-file [filename]
  (let [schema (parse-sql filename)
        filename (str output-directory "/" (:table-name schema) ".sql")]
    (create-yesql-queries-file filename schema)))

(defn create-yesql-queries []
  (let [directory (io/file input-directory)
        files (file-seq directory)
        creates (filter #(re-matches #".*migrations\/[0-9]+-create.*\.up\.sql$" %) (map str files))]
    (map #(process-file %) creates)))

(defn -main [& args]
  (println args))

