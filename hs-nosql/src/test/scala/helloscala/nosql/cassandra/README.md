# hl-nosql-common

NoSQL公共代码

## Cassandra 

测试**keyspace**

```cql
CREATE KEYSPACE hl_test with replication = {'class':'SimpleStrategy', 'replication_factor':1};
CREATE TABLE news(
  key TEXT,
  period INT,
  url TEXT,
  date TIMESTAMP,
  title TEXT,
  summary TEXT,
  author TEXT,
  content TEXT,
  content_element TEXT,
  PRIMARY KEY ((key, period), url, date)
);
```
