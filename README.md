# jexpose

This project can expose class graph inside of jar.

You should provide a target jar file which to be exposed and
a full qualified name of class(or interface) as a entry point,
also as development is at demo state you always need to specific a classpath.

```
jexpose classpath target-jar com.example.Entry
```

`target-jar` must copy all of it's dependencies.