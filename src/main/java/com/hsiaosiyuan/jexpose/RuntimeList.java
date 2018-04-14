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
