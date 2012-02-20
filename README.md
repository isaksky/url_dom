# url_dom

This simple library gives you the domain for a hostname. It is based on the list of domains from http://publicsuffix.org.

For example:

```clojure
(ns my-ns
 (:require [url_dom.core :as u]))

(u/parse "sub1.sub2.domain.co.uk")
;=> {:public-suffix "co.uk", :domain "domain.co.uk", :rule-used "*.uk"}
```

If you are trying to follow the code, look at this page:

http://publicsuffix.org/list/

I used the same exact terminology in my code.

## A couple of things I'll probably add soon:

* Way more tests. Will probably come from rake task that converts all of these tests they provide: http://publicsuffix.org/list/test.txt
* If java has a built in URI class, add some methods, and/or accept it as an argument.

