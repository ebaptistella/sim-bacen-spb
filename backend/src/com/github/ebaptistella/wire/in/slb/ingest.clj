(ns com.github.ebaptistella.wire.in.slb.ingest
  "Schema registry for SLB message ingestion."
  (:require [com.github.ebaptistella.wire.in.slb.slb0001 :refer [SLB0001Request]]
            [com.github.ebaptistella.wire.in.slb.slb0002 :refer [SLB0002Request]]
            [com.github.ebaptistella.wire.in.slb.slb0003 :refer [SLB0003Request]]
            [com.github.ebaptistella.wire.in.slb.slb0005 :refer [SLB0005Request]]
            [com.github.ebaptistella.wire.in.slb.slb0006 :refer [SLB0006Request]]
            [com.github.ebaptistella.wire.in.slb.slb0007 :refer [SLB0007Request]]
            [com.github.ebaptistella.wire.in.slb.slb0008 :refer [SLB0008Request]]))

(def schemas
  {"SLB0001" SLB0001Request
   "SLB0002" SLB0002Request
   "SLB0003" SLB0003Request
   "SLB0005" SLB0005Request
   "SLB0006" SLB0006Request
   "SLB0007" SLB0007Request
   "SLB0008" SLB0008Request})

(defn get-schema [msg-type]
  (get schemas msg-type))
