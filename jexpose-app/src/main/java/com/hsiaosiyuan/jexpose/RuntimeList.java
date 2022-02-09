/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hsiaosiyuan.jexpose;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Objects;

public class RuntimeList {
  public static final HashMap<String, Boolean> v7 = new HashMap<>();
  public static final HashMap<String, Boolean> v8 = new HashMap<>();
  public static final HashMap<String, Boolean> v9 = new HashMap<>();
  public static final HashMap<String, Boolean> v10 = new HashMap<>();

  static {
    resolve("7");
    resolve("8");
    resolve("9");
    resolve("10");
  }

  static void resolve(String v) {
    HashMap<String, Boolean> map = new HashMap<>();
    switch (v) {
      case "7": {
        map = v7;
        break;
      }
      case "8": {
        map = v8;
        break;
      }
      case "9": {
        map = v9;
        break;
      }
      case "10": {
        map = v10;
        break;
      }
    }
    try {
      ClassLoader classLoader = RuntimeList.class.getClassLoader();
      InputStreamReader is = new InputStreamReader(classLoader.getResourceAsStream("rt-list/" + v + ".txt"));
      BufferedReader bufferedReader = new BufferedReader(is);
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        map.put(line, true);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  static boolean has(String bn) {
    return v7.containsKey(bn) || v8.containsKey(bn) || v9.containsKey(bn) || v10.containsKey(bn);
  }
}
