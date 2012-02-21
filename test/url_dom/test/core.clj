(ns url_dom.test.core
  (:use [url_dom.core])
  (:use [clojure.test]))

;; Intern all private functions for testing
(doseq [[sym var] (ns-interns (find-ns 'url_dom.core))]
  (when (:private (meta var))
    (intern *ns* sym var)))

(deftest get-domain-labels-test
  (is (= ["bam" "foo"] (get-domain-labels "bam.foo"))))

(deftest parse-rule-line-test
  (is (= [] (parse-rule-line [] "   ")))
  (is (= [] (parse-rule-line [] "//foo.bar")))
  (is (= true (:exception? (first (parse-rule-line [] "!bam.foo")))))
  (is (= ["bam" "foo"] (:labels (first (parse-rule-line [] "!bam.foo"))))))

(deftest label-equal-test
  (is (label-equal? "com" "com"))
  (is (label-equal? "*" "fizz"))
  (is (not (label-equal? "com" "org"))))

(def rule-1
  {:exception? false
   :labels ["foo" "bar"]})

(deftest matches-rule-test
  (is (matches-rule? rule-1 "woah.foo.bar"))
  (is (matches-rule? rule-1 "holy.woah.foo.bar"))
  (is (not (matches-rule? rule-1 "hmm.bar.foo")))
  (is (not (matches-rule? rule-1 "foo.biz"))))

(deftest get-matching-rules-test
  (is (= (set [{:labels ["com" "ac"], :exception? false, :orig "com.ac"}
               {:labels ["ac"], :exception? false, :orig "ac"}])
         (set (get-matching-rules "bam.com.ac")))))

(deftest get-prevailing-rule-test
  (is (= {:labels ["com" "ac"], :exception? false, :orig "com.ac"}
         (get-prevailing-rule "mydomain.com.ac")))
  (is (= {:labels ["ac"], :exception? false, :orig "ac"}
         (get-prevailing-rule "mydomain.fizz.ac")))
  (is (= {:labels ["metro" "tokyo" "jp"], :exception? true, :orig "!metro.tokyo.jp"}
         (get-prevailing-rule "mydomain.metro.tokyo.jp"))))

(deftest parse-test
  (is (= "foo.biz" (:domain (parse "www.foo.biz"))))
  (is (= "foo.biz" (:domain (parse "www.sub.foo.biz"))))
  (is (= "example.com" (:domain (parse "example.com"))))
  (is (= "example.com" (:domain (parse "sub.example.com"))))
  (is (= "example.uk.com" (:domain (parse "example.uk.com"))))
  (is (= "example.uk.com" (:domain (parse "sub.example.uk.com"))))
  (is (= "test.ac.jp" (:domain (parse "test.ac.jp"))))
  (is (= "test.ac.jp" (:domain (parse "www.test.ac.jp"))))
  (is (nil? (parse "kyoto.jp")))
  (is (= "city.kyoto.jp" (:domain (parse "city.kyoto.jp"))))
  (is (= "city.kyoto.jp" (:domain (parse "www.city.kyoto.jp"))))
  (is (= "b.test.om" (:domain (parse "a.b.test.om")))))

(def URI-extension-test
  (is (= "zombo.com" (domain (URI. "http://www.zombo.com"))))
  (is (= "co.uk" (public-suffix (URI. "http://www.zombo.co.uk")))))