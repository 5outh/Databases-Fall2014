The A Team : Project 2 (Database Management)

Members:
- Ben Kovach
- Will Pickard
- Will Speegle
- Deborah Brown

to compile and run: 

```
> javac *.java
> java MovieDB
```

Table.java currently uses BpTreeMap as its underlying indexing mechanism. We implemented this and ExtHashMap. To use ExtHashMap instead, change lines 90 and 111 to the following:

```java
index = new ExtHashMap <> (KeyType.class, Comparable [].class, DEFAULT_BUCKETS);
```