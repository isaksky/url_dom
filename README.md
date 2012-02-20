# url_dom

This simple library gives you the domain for a hostname. It is based on the list of domains from http://publicsuffix.org.

For example:

```clojure
(ns my-ns
 (:require [url_dom.core :as u]))

(u/parse "sub1.sub2.domain.co.uk")
;=> {:public-suffix "co.uk", :domain "domain.co.uk", :rule-used "*.uk"}
```

