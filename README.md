# jexpose

This project can expose class graph inside of jar.

```
jexpose -entry com.qianmi -entry-jar /w/qm/uc.jar -lib /w/qm/uc/dependencies -provider-suffix Provider -exclude ".*Score.*"
```

Option | Meaning
-------|--------
entry| the entry point to start our exposing
entry-jar | path of target jar
lib| path of directory which is comprise of dependencies of target jar
provider-suffix| suffix of provider name
include | regexp for including files
exclude | regexp for excluding files

```
is_filename_match = (provider-suffix(filename) || include(filename)) && !exclude(filename)
```

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
3. `jexpose com.example PROJ_ROOT/target/example-x.x-SNAPSHOT.jar PROJ_ROOT/target/dependency`