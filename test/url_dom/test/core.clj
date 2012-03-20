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

(deftest URI-extension-test
  (is (= "zombo.com" (domain (java.net.URI. "http://www.zombo.com"))))
  (is (= "co.uk" (public-suffix (java.net.URI. "http://www.zombo.co.uk")))))

(deftest stolen-tests
  ;; Stole these tests by parsing
  ;; http://publicsuffix.org/list/test.txt
  ;; See /resources/steal_tests.rake
  
  ;; Any copyright is dedicated to the Public Domain.
  ;; http://creativecommons.org/publicdomain/zero/1.0/
  ;; NULL input.
  (is (= nil (:domain (parse nil))))
  ;; Mixed case.
  (is (= nil (:domain (parse "COM"))))
  (is (= "example.com" (:domain (parse "example.COM"))))
  (is (= "example.com" (:domain (parse "WwW.example.COM"))))
  ;; Leading dot.
  (is (= nil (:domain (parse ".com"))))
  (is (= nil (:domain (parse ".example"))))
  (is (= nil (:domain (parse ".example.com"))))
  (is (= nil (:domain (parse ".example.example"))))
  ;; Unlisted TLD.
  (is (= nil (:domain (parse "example"))))
  (is (= nil (:domain (parse "example.example"))))
  (is (= nil (:domain (parse "b.example.example"))))
  (is (= nil (:domain (parse "a.b.example.example"))))
  ;; Listed, but non-Internet, TLD.
  ;;checkPublicSuffix('local', NULL);
  ;;checkPublicSuffix('example.local', NULL);
  ;;checkPublicSuffix('b.example.local', NULL);
  ;;checkPublicSuffix('a.b.example.local', NULL);
  ;; TLD with only 1 rule.
  (is (= nil (:domain (parse "biz"))))
  (is (= "domain.biz" (:domain (parse "domain.biz"))))
  (is (= "domain.biz" (:domain (parse "b.domain.biz"))))
  (is (= "domain.biz" (:domain (parse "a.b.domain.biz"))))
  ;; TLD with some 2-level rules.
  (is (= nil (:domain (parse "com"))))
  (is (= "example.com" (:domain (parse "example.com"))))
  (is (= "example.com" (:domain (parse "b.example.com"))))
  (is (= "example.com" (:domain (parse "a.b.example.com"))))
  (is (= nil (:domain (parse "uk.com"))))
  (is (= "example.uk.com" (:domain (parse "example.uk.com"))))
  (is (= "example.uk.com" (:domain (parse "b.example.uk.com"))))
  (is (= "example.uk.com" (:domain (parse "a.b.example.uk.com"))))
  (is (= "test.ac" (:domain (parse "test.ac"))))
  ;; TLD with only 1 (wildcard) rule.
  (is (= nil (:domain (parse "cy"))))
  (is (= nil (:domain (parse "c.cy"))))
  (is (= "b.c.cy" (:domain (parse "b.c.cy"))))
  (is (= "b.c.cy" (:domain (parse "a.b.c.cy"))))
  ;; More complex TLD.
  (is (= nil (:domain (parse "jp"))))
  (is (= "test.jp" (:domain (parse "test.jp"))))
  (is (= "test.jp" (:domain (parse "www.test.jp"))))
  (is (= nil (:domain (parse "ac.jp"))))
  (is (= "test.ac.jp" (:domain (parse "test.ac.jp"))))
  (is (= "test.ac.jp" (:domain (parse "www.test.ac.jp"))))
  (is (= nil (:domain (parse "kyoto.jp"))))
  (is (= nil (:domain (parse "c.kyoto.jp"))))
  (is (= "b.c.kyoto.jp" (:domain (parse "b.c.kyoto.jp"))))
  (is (= "b.c.kyoto.jp" (:domain (parse "a.b.c.kyoto.jp"))))
  (is (= "pref.kyoto.jp" (:domain (parse "pref.kyoto.jp"))))
  (is (= "pref.kyoto.jp" (:domain (parse "www.pref.kyoto.jp"))))
  (is (= "city.kyoto.jp" (:domain (parse "city.kyoto.jp"))))
  (is (= "city.kyoto.jp" (:domain (parse "www.city.kyoto.jp"))))
  ;; TLD with a wildcard rule and exceptions.
  (is (= nil (:domain (parse "om"))))
  (is (= nil (:domain (parse "test.om"))))
  (is (= "b.test.om" (:domain (parse "b.test.om"))))
  (is (= "b.test.om" (:domain (parse "a.b.test.om"))))
  (is (= "songfest.om" (:domain (parse "songfest.om"))))
  (is (= "songfest.om" (:domain (parse "www.songfest.om"))))
  ;; US K12.
  (is (= nil (:domain (parse "us"))))
  (is (= "test.us" (:domain (parse "test.us"))))
  (is (= "test.us" (:domain (parse "www.test.us"))))
  (is (= nil (:domain (parse "ak.us"))))
  (is (= "test.ak.us" (:domain (parse "test.ak.us"))))
  (is (= "test.ak.us" (:domain (parse "www.test.ak.us"))))
  (is (= nil (:domain (parse "k12.ak.us"))))
  (is (= "test.k12.ak.us" (:domain (parse "test.k12.ak.us"))))
  (is (= "test.k12.ak.us" (:domain (parse "www.test.k12.ak.us")))))