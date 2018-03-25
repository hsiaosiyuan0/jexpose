# jexpose

This project can expose class graph inside of jar.

```
jexpose classpath path-2-jar-dir com.example.Entry
```

Option | Meaning
-------|--------
path-2-jar-dir| path to a directory which is comprise of the target jar and all of it's dependencies
classpath | although you've specified the tar jar and all of it's dependencies the java runtime such as `java/lang/*` are still isolated, so you should indicate a path to tell jexpose where to find them
com.example.Entry| the entry point to start our exposing

After exposing, jexpose will produce two json files, one contains the structure of your entry point class(interface) another contains a
class pool which is comprise of all of your entry point references

## example

Below are a input/output pair for for a glance.

### input

```java
package com.hsiaosiyuan;

import org.apache.http.message.BasicHeader;

import java.util.*;

public interface Service {

  ArrayList<User> users = new ArrayList<User>();

  boolean login(String username, byte[] password);

  User getUser(String username);

  User getUser(int id);

  ArrayList<User> getUserFriends(User user);

  boolean doSomething1(ArrayList<BasicHeader> headers, int[][] a);

  ArrayList<User> doSomething2(HashMap<String, ArrayList<BasicHeader>> headersMap, int[][] a, ArrayList<Integer>[] b);

  Map doSomething3(TreeMap<String, User> map, int[][] a);

  int doSomething4(char p1, double p2, long p3, int p4, User[] p5);
}
```

### (1/2) output json for describing entry point structure

```json
{
  "binaryName": "com/hsiaosiyuan/Service",
  "fields": {
    "users": {
      "binaryName": "java/util/ArrayList",
      "subTypes": [],
      "typeArgs": [
        {
          "isWildcard": false,
          "type": {
            "binaryName": "com/hsiaosiyuan/User",
            "subTypes": [],
            "typeArgs": []
          }
        }
      ]
    }
  },
  "isInterface": true,
  "methods": {
    "getUser": {
      "exceptions": [],
      "params": [
        {
          "binaryName": "java/lang/Integer"
        }
      ],
      "ret": {
        "binaryName": "com/hsiaosiyuan/User",
        "subTypes": [],
        "typeArgs": []
      },
      "typeParams": []
    },
    "getUserFriends": {
      "exceptions": [],
      "params": [
        {
          "binaryName": "com/hsiaosiyuan/User",
          "subTypes": [],
          "typeArgs": []
        }
      ],
      "ret": {
        "binaryName": "java/util/ArrayList",
        "subTypes": [],
        "typeArgs": [
          {
            "isWildcard": false,
            "type": {
              "binaryName": "com/hsiaosiyuan/User",
              "subTypes": [],
              "typeArgs": []
            }
          }
        ]
      },
      "typeParams": []
    },
    "doSomething2": {
      "exceptions": [],
      "params": [
        {
          "binaryName": "java/util/HashMap",
          "subTypes": [],
          "typeArgs": [
            {
              "isWildcard": false,
              "type": {
                "binaryName": "java/lang/String",
                "subTypes": [],
                "typeArgs": []
              }
            },
            {
              "isWildcard": false,
              "type": {
                "binaryName": "java/util/ArrayList",
                "subTypes": [],
                "typeArgs": [
                  {
                    "isWildcard": false,
                    "type": {
                      "binaryName": "org/apache/http/message/BasicHeader",
                      "subTypes": [],
                      "typeArgs": []
                    }
                  }
                ]
              }
            }
          ]
        },
        {
          "elementType": {
            "elementType": {
              "binaryName": "java/lang/Integer"
            }
          }
        },
        {
          "elementType": {
            "binaryName": "java/util/ArrayList",
            "subTypes": [],
            "typeArgs": [
              {
                "isWildcard": false,
                "type": {
                  "binaryName": "java/lang/Integer",
                  "subTypes": [],
                  "typeArgs": []
                }
              }
            ]
          }
        }
      ],
      "ret": {
        "binaryName": "java/util/ArrayList",
        "subTypes": [],
        "typeArgs": [
          {
            "isWildcard": false,
            "type": {
              "binaryName": "com/hsiaosiyuan/User",
              "subTypes": [],
              "typeArgs": []
            }
          }
        ]
      },
      "typeParams": []
    }
  },
  "superClasses": [
    {
      "binaryName": "java/lang/Object",
      "subTypes": [],
      "typeArgs": []
    }
  ],
  "typeParams": []
}
```

### (2/2) output json for class pool

```json
{
  "org/apache/http/NameValuePair": {
    "binaryName": "org/apache/http/NameValuePair",
    "fields": {},
    "isInterface": true,
    "methods": {
      "getValue": {
        "exceptions": [],
        "params": [],
        "ret": {
          "binaryName": "java/lang/String",
          "subTypes": [],
          "typeArgs": []
        },
        "typeParams": []
      },
      "getName": {
        "exceptions": [],
        "params": [],
        "ret": {
          "binaryName": "java/lang/String",
          "subTypes": [],
          "typeArgs": []
        },
        "typeParams": []
      }
    },
    "superClasses": [
      {
        "binaryName": "java/lang/Object",
        "subTypes": [],
        "typeArgs": []
      }
    ],
    "typeParams": []
  },
  "com/hsiaosiyuan/Service": {
    "binaryName": "com/hsiaosiyuan/Service",
    "fields": {
      "users": {
        "binaryName": "java/util/ArrayList",
        "subTypes": [],
        "typeArgs": [
          {
            "isWildcard": false,
            "type": {
              "binaryName": "com/hsiaosiyuan/User",
              "subTypes": [],
              "typeArgs": []
            }
          }
        ]
      }
    },
    "isInterface": true,
    "methods": {
      "getUser": {
        "exceptions": [],
        "params": [
          {
            "binaryName": "java/lang/Integer"
          }
        ],
        "ret": {
          "binaryName": "com/hsiaosiyuan/User",
          "subTypes": [],
          "typeArgs": []
        },
        "typeParams": []
      },
      "getUserFriends": {
        "exceptions": [],
        "params": [
          {
            "binaryName": "com/hsiaosiyuan/User",
            "subTypes": [],
            "typeArgs": []
          }
        ],
        "ret": {
          "binaryName": "java/util/ArrayList",
          "subTypes": [],
          "typeArgs": [
            {
              "isWildcard": false,
              "type": {
                "binaryName": "com/hsiaosiyuan/User",
                "subTypes": [],
                "typeArgs": []
              }
            }
          ]
        },
        "typeParams": []
      },
      "doSomething3": {
        "exceptions": [],
        "params": [
          {
            "binaryName": "java/util/TreeMap",
            "subTypes": [],
            "typeArgs": [
              {
                "isWildcard": false,
                "type": {
                  "binaryName": "java/lang/String",
                  "subTypes": [],
                  "typeArgs": []
                }
              },
              {
                "isWildcard": false,
                "type": {
                  "binaryName": "com/hsiaosiyuan/User",
                  "subTypes": [],
                  "typeArgs": []
                }
              }
            ]
          },
          {
            "elementType": {
              "elementType": {
                "binaryName": "java/lang/Integer"
              }
            }
          }
        ],
        "ret": {
          "binaryName": "java/util/Map",
          "subTypes": [],
          "typeArgs": []
        },
        "typeParams": []
      },
      "doSomething4": {
        "exceptions": [],
        "params": [
          {
            "binaryName": "java/lang/Character"
          },
          {
            "binaryName": "java/lang/Double"
          },
          {
            "binaryName": "java/lang/Long"
          },
          {
            "binaryName": "java/lang/Integer"
          },
          {
            "elementType": {
              "binaryName": "com/hsiaosiyuan/User",
              "subTypes": [],
              "typeArgs": []
            }
          }
        ],
        "ret": {
          "binaryName": "java/lang/Integer"
        },
        "typeParams": []
      },
      "login": {
        "exceptions": [],
        "params": [
          {
            "binaryName": "java/lang/String",
            "subTypes": [],
            "typeArgs": []
          },
          {
            "elementType": {
              "binaryName": "java/lang/Byte"
            }
          }
        ],
        "ret": {
          "binaryName": "java/lang/Boolean"
        },
        "typeParams": []
      },
      "doSomething1": {
        "exceptions": [],
        "params": [
          {
            "binaryName": "java/util/ArrayList",
            "subTypes": [],
            "typeArgs": [
              {
                "isWildcard": false,
                "type": {
                  "binaryName": "org/apache/http/message/BasicHeader",
                  "subTypes": [],
                  "typeArgs": []
                }
              }
            ]
          },
          {
            "elementType": {
              "elementType": {
                "binaryName": "java/lang/Integer"
              }
            }
          }
        ],
        "ret": {
          "binaryName": "java/lang/Boolean"
        },
        "typeParams": []
      },
      "doSomething2": {
        "exceptions": [],
        "params": [
          {
            "binaryName": "java/util/HashMap",
            "subTypes": [],
            "typeArgs": [
              {
                "isWildcard": false,
                "type": {
                  "binaryName": "java/lang/String",
                  "subTypes": [],
                  "typeArgs": []
                }
              },
              {
                "isWildcard": false,
                "type": {
                  "binaryName": "java/util/ArrayList",
                  "subTypes": [],
                  "typeArgs": [
                    {
                      "isWildcard": false,
                      "type": {
                        "binaryName": "org/apache/http/message/BasicHeader",
                        "subTypes": [],
                        "typeArgs": []
                      }
                    }
                  ]
                }
              }
            ]
          },
          {
            "elementType": {
              "elementType": {
                "binaryName": "java/lang/Integer"
              }
            }
          },
          {
            "elementType": {
              "binaryName": "java/util/ArrayList",
              "subTypes": [],
              "typeArgs": [
                {
                  "isWildcard": false,
                  "type": {
                    "binaryName": "java/lang/Integer",
                    "subTypes": [],
                    "typeArgs": []
                  }
                }
              ]
            }
          }
        ],
        "ret": {
          "binaryName": "java/util/ArrayList",
          "subTypes": [],
          "typeArgs": [
            {
              "isWildcard": false,
              "type": {
                "binaryName": "com/hsiaosiyuan/User",
                "subTypes": [],
                "typeArgs": []
              }
            }
          ]
        },
        "typeParams": []
      },
      "<clinit>": {
        "exceptions": [],
        "params": [],
        "ret": {
          "binaryName": "java/lang/Void"
        },
        "typeParams": []
      }
    },
    "superClasses": [
      {
        "binaryName": "java/lang/Object",
        "subTypes": [],
        "typeArgs": []
      }
    ],
    "typeParams": []
  },
  "org/apache/http/message/BasicHeader": {
    "binaryName": "org/apache/http/message/BasicHeader",
    "fields": {
      "serialVersionUID": {
        "binaryName": "java/lang/Long"
      },
      "name": {
        "binaryName": "java/lang/String",
        "subTypes": [],
        "typeArgs": []
      },
      "EMPTY_HEADER_ELEMENTS": {
        "elementType": {
          "binaryName": "org/apache/http/HeaderElement",
          "subTypes": [],
          "typeArgs": []
        }
      },
      "value": {
        "binaryName": "java/lang/String",
        "subTypes": [],
        "typeArgs": []
      }
    },
    "isInterface": false,
    "methods": {
      "getValue": {
        "exceptions": [],
        "params": [],
        "ret": {
          "binaryName": "java/lang/String",
          "subTypes": [],
          "typeArgs": []
        },
        "typeParams": []
      },
      "getName": {
        "exceptions": [],
        "params": [],
        "ret": {
          "binaryName": "java/lang/String",
          "subTypes": [],
          "typeArgs": []
        },
        "typeParams": []
      },
      "getElements": {
        "exceptions": [],
        "params": [],
        "ret": {
          "elementType": {
            "binaryName": "org/apache/http/HeaderElement",
            "subTypes": [],
            "typeArgs": []
          }
        },
        "typeParams": []
      },
      "clone": {
        "exceptions": [],
        "params": [],
        "ret": {
          "binaryName": "java/lang/Object",
          "subTypes": [],
          "typeArgs": []
        },
        "typeParams": []
      },
      "toString": {
        "exceptions": [],
        "params": [],
        "ret": {
          "binaryName": "java/lang/String",
          "subTypes": [],
          "typeArgs": []
        },
        "typeParams": []
      },
      "<init>": {
        "exceptions": [],
        "params": [
          {
            "binaryName": "java/lang/String",
            "subTypes": [],
            "typeArgs": []
          },
          {
            "binaryName": "java/lang/String",
            "subTypes": [],
            "typeArgs": []
          }
        ],
        "ret": {
          "binaryName": "java/lang/Void"
        },
        "typeParams": []
      },
      "<clinit>": {
        "exceptions": [],
        "params": [],
        "ret": {
          "binaryName": "java/lang/Void"
        },
        "typeParams": []
      }
    },
    "superClasses": [
      {
        "binaryName": "java/lang/Object",
        "subTypes": [],
        "typeArgs": []
      }
    ],
    "typeParams": []
  },
  "org/apache/http/HeaderElement": {
    "binaryName": "org/apache/http/HeaderElement",
    "fields": {},
    "isInterface": true,
    "methods": {
      "getValue": {
        "exceptions": [],
        "params": [],
        "ret": {
          "binaryName": "java/lang/String",
          "subTypes": [],
          "typeArgs": []
        },
        "typeParams": []
      },
      "getName": {
        "exceptions": [],
        "params": [],
        "ret": {
          "binaryName": "java/lang/String",
          "subTypes": [],
          "typeArgs": []
        },
        "typeParams": []
      },
      "getParameterCount": {
        "exceptions": [],
        "params": [],
        "ret": {
          "binaryName": "java/lang/Integer"
        },
        "typeParams": []
      },
      "getParameters": {
        "exceptions": [],
        "params": [],
        "ret": {
          "elementType": {
            "binaryName": "org/apache/http/NameValuePair",
            "subTypes": [],
            "typeArgs": []
          }
        },
        "typeParams": []
      },
      "getParameterByName": {
        "exceptions": [],
        "params": [
          {
            "binaryName": "java/lang/String",
            "subTypes": [],
            "typeArgs": []
          }
        ],
        "ret": {
          "binaryName": "org/apache/http/NameValuePair",
          "subTypes": [],
          "typeArgs": []
        },
        "typeParams": []
      },
      "getParameter": {
        "exceptions": [],
        "params": [
          {
            "binaryName": "java/lang/Integer"
          }
        ],
        "ret": {
          "binaryName": "org/apache/http/NameValuePair",
          "subTypes": [],
          "typeArgs": []
        },
        "typeParams": []
      }
    },
    "superClasses": [
      {
        "binaryName": "java/lang/Object",
        "subTypes": [],
        "typeArgs": []
      }
    ],
    "typeParams": []
  },
  "com/hsiaosiyuan/Person": {
    "binaryName": "com/hsiaosiyuan/Person",
    "fields": {
      "gender": {
        "binaryName": "com/hsiaosiyuan/Gender",
        "subTypes": [],
        "typeArgs": []
      },
      "name": {
        "binaryName": "java/lang/String",
        "subTypes": [],
        "typeArgs": []
      },
      "weight": {
        "binaryName": "java/lang/Integer"
      },
      "age": {
        "binaryName": "java/lang/Integer",
        "subTypes": [],
        "typeArgs": []
      },
      "height": {
        "binaryName": "java/lang/Integer"
      }
    },
    "isInterface": false,
    "methods": {
      "<init>": {
        "exceptions": [],
        "params": [],
        "ret": {
          "binaryName": "java/lang/Void"
        },
        "typeParams": []
      }
    },
    "superClasses": [
      {
        "binaryName": "java/lang/Object",
        "subTypes": [],
        "typeArgs": []
      }
    ],
    "typeParams": []
  },
  "com/hsiaosiyuan/User": {
    "binaryName": "com/hsiaosiyuan/User",
    "fields": {
      "password": {
        "elementType": {
          "binaryName": "java/lang/Byte"
        }
      },
      "id": {
        "binaryName": "java/lang/Integer"
      },
      "friends": {
        "binaryName": "java/util/ArrayList",
        "subTypes": [],
        "typeArgs": [
          {
            "isWildcard": false,
            "type": {
              "binaryName": "com/hsiaosiyuan/User",
              "subTypes": [],
              "typeArgs": []
            }
          }
        ]
      },
      "username": {
        "binaryName": "java/lang/String",
        "subTypes": [],
        "typeArgs": []
      }
    },
    "isInterface": false,
    "methods": {
      "<init>": {
        "exceptions": [],
        "params": [],
        "ret": {
          "binaryName": "java/lang/Void"
        },
        "typeParams": []
      }
    },
    "superClasses": [
      {
        "binaryName": "com/hsiaosiyuan/Person",
        "subTypes": [],
        "typeArgs": []
      }
    ],
    "typeParams": []
  },
  "com/hsiaosiyuan/Gender": {
    "binaryName": "com/hsiaosiyuan/Gender",
    "fields": {
      "Male": {
        "binaryName": "com/hsiaosiyuan/Gender",
        "subTypes": [],
        "typeArgs": []
      },
      "Female": {
        "binaryName": "com/hsiaosiyuan/Gender",
        "subTypes": [],
        "typeArgs": []
      },
      "$VALUES": {
        "elementType": {
          "binaryName": "com/hsiaosiyuan/Gender",
          "subTypes": [],
          "typeArgs": []
        }
      }
    },
    "isInterface": false,
    "methods": {
      "valueOf": {
        "exceptions": [],
        "params": [
          {
            "binaryName": "java/lang/String",
            "subTypes": [],
            "typeArgs": []
          }
        ],
        "ret": {
          "binaryName": "com/hsiaosiyuan/Gender",
          "subTypes": [],
          "typeArgs": []
        },
        "typeParams": []
      },
      "values": {
        "exceptions": [],
        "params": [],
        "ret": {
          "elementType": {
            "binaryName": "com/hsiaosiyuan/Gender",
            "subTypes": [],
            "typeArgs": []
          }
        },
        "typeParams": []
      },
      "<init>": {
        "exceptions": [],
        "params": [],
        "ret": {
          "binaryName": "java/lang/Void"
        },
        "typeParams": []
      },
      "<clinit>": {
        "exceptions": [],
        "params": [],
        "ret": {
          "binaryName": "java/lang/Void"
        },
        "typeParams": []
      }
    },
    "superClasses": [
      {
        "binaryName": "java/lang/Enum",
        "subTypes": [],
        "typeArgs": [
          {
            "isWildcard": false,
            "type": {
              "binaryName": "com/hsiaosiyuan/Gender",
              "subTypes": [],
              "typeArgs": []
            }
          }
        ]
      }
    ],
    "typeParams": []
  }
}
```
