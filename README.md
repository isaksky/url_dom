# url_dom

This simple library gives you the domain for a hostname. It is based on the list of domains from http://publicsuffix.org.

For example:

```clojure
(ns my-ns
 (:require [url_dom.core :as u]))

(u/parse "sub1.sub2.domain.co.uk")
;=> {:public-suffix "co.uk", :domain "domain.co.uk", :rule-used "*.uk"}
```

Or, using the java.net.URI class:

```clojure
(ns my-ns
  (:import (java.net URI)))

(domain (URI. "http://www.subA.subB.zombo.co.uk")) ;=> "zombo.co.uk"
(public-suffix (URI. "http://www.subA.subB.zombo.co.uk")) ;=> "co.uk"
```

If you are trying to follow the code, look at this page:

http://publicsuffix.org/list/

The code uses the same terminology. I don't use algorithm specified on that site, as it doesn't make all the tests pass.

**Things I'll do next:**

* Upload to clojars. make sure installation and usage is straightforward.

