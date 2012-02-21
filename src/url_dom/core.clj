(ns url_dom.core
  (:import (java.io BufferedReader FileReader))
  (:require [clojure.string :as str])
  (:import (java.net URI)))

(defn- working-dir-path [path]
  (str (. System getProperty "user.dir") path ))

(def rule-file-path
  (working-dir-path "/resources/effective_tld_names.txt"))

(defn- get-domain-labels [domain]
  (str/split domain #"\."))

(defn- parse-rule-line [acc line]
  (cond
   (str/blank? line) acc
   (.startsWith line "//") acc
   :else (let [exception-rule? (.startsWith line "!")
               clean-line (str/replace-first line #"\!" "")]
           (conj acc {:labels (get-domain-labels clean-line)
                      :exception? exception-rule?
                      :orig line}))))

(def rules
  (with-open [rdr (BufferedReader. (FileReader. rule-file-path))]
    (reduce parse-rule-line
            []
            (line-seq rdr))))

(defn- label-equal? [rule-label domain-label]
  {:pre [(not-any? #(str/blank? %) [rule-label domain-label])]}
  (or (= rule-label domain-label)
      (= rule-label "*")))

(defn- matches-rule? [rule domain]
  (let [rev-rule-labels (reverse (:labels rule))
        rule-labels-count (count rev-rule-labels)
        rev-domain-labels (reverse (get-domain-labels domain))
        domain-labels-count (count rev-domain-labels)]
    (every? (fn [n] (label-equal? (nth rev-rule-labels n)
                                 (nth rev-domain-labels n)) )
            (range 0 (min rule-labels-count domain-labels-count)))))

(defn- get-matching-rules [domain]
  (filter (fn [rule] (matches-rule? rule domain))
          rules))

(defn- get-prevailing-rule [domain]
  (first (sort-by  (fn [rule] [(not (:exception? rule))
                              (- (count (:labels rule)))])
                   (get-matching-rules domain))))

(defn- apply-rule [rule domain]
  (let [domain-labels (get-domain-labels domain)
        rule-labels-count (count (:labels rule))]
    (cond (< (count domain-labels) rule-labels-count) nil
          :else {:public-suffix (str/join "."
                                          (take-last rule-labels-count domain-labels))
                 :domain (str/join "."
                                   (take-last (if (:exception? rule)
                                                rule-labels-count
                                                (inc rule-labels-count))
                                              domain-labels))
                 :rule-used (:orig rule)})))

(defn parse [domain]
  (let [domain (str/lower-case domain)]
    (let [rule (get-prevailing-rule domain)]
      (apply-rule rule domain))))

(defprotocol domain-info
  (domain [s])
  (public-suffix [s]))

;;; Makes it so you can do:
;;; (domain (URI. "http://www.zombo.com")) or
;;; (public-suffix (URI. "http://www.zombo.com"))
(extend java.net.URI
  domain-info
  {:domain (fn [uri] (:domain (parse (.getHost uri))))
   :public-suffix (fn [uri] (:public-suffix (parse (.getHost uri))))})
