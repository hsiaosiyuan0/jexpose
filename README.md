# jexpose

This project can expose class graph inside of jar.

```
jexpose entry entryJarPath libDirPath jrtPath
```

Option | Meaning
-------|--------
entry| the entry point to start our exposing
entryJarPath | path of target jar
libDirPath| path of directory which is comprise of dependencies of target jar
jrtPath| although you've specified the tar jar and all of it's dependencies the java runtime such as `java/lang/*` are still isolated, so you should indicate a path to tell jexpose where to find them

After exposing, jexpose will produce a json file, content of it looks like:

```json
{
  "classes": {
    "com.hsiaosiyuan.ServiceProvider": { ... }
    ...
  },
  "providers": [
    "com.hsiaosiyuan.ServiceProvider"
  ]
}
```

## How to use

1. `mvn package` to produce your target jar which is located at `PROJ_ROOT/target/xxx.jar`
2. `mvn install dependency:copy-dependencies` to copy dependencies of the target jar into `PROJ_ROOT/target/dependency`
3. `jexpose com.example PROJ_ROOT/target/example-x.x-SNAPSHOT.jar PROJ_ROOT/target/dependency PATH_TO_JRT`