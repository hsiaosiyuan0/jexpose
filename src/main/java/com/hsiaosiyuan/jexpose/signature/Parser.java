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
package com.hsiaosiyuan.jexpose.signature;

import com.hsiaosiyuan.jexpose.signature.node.*;
import com.hsiaosiyuan.jexpose.signature.node.ClassTypeSignature.Sub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Parser {
  private static final HashMap<String, String> primitives = new HashMap<String, String>() {{
    put("B", "java/lang/Byte");
    put("C", "java/lang/Character");
    put("D", "java/lang/Double");
    put("F", "java/lang/Float");
    put("I", "java/lang/Integer");
    put("J", "java/lang/Long");
    put("S", "java/lang/Short");
    put("Z", "java/lang/Boolean");
    put("V", "java/lang/Void");
  }};

  private static final int EOF = -1;

  private String raw;
  private int i;
  private int mrk;

  public Parser(String signature) {
    raw = signature;
  }

  public TypeSignature parseTypeSignature() {
    if (aheadIsPrimitive())
      return parsePrimitive();

    return parseFieldTypeSignature();
  }

  public MethodTypeSignature parseMethodTypeSignature() {
    MethodTypeSignature node = new MethodTypeSignature();
    node.typeParams.addAll(parseTypeParams());
    node.params.addAll(parseParams());
    node.ret = parseReturn();
    node.exceptions = parseExceptions();
    return node;
  }

  public ClassSignature parseClassSignature() {
    ClassSignature node = new ClassSignature();
    node.typeParams.addAll(parseTypeParams());
    node.superClasses.addAll(parseClassTypeSignatures());
    return node;
  }

  private ArrayList<Node> parseExceptions() {
    ArrayList<Node> nodes = new ArrayList<>();
    if (!peek().equals("^")) return nodes;

    while (peek().equals("^")) {
      read();
      nodes.add(parseException());
    }
    return nodes;
  }

  private Node parseException() {
    if (peek().equals("L"))
      return parseClassTypeSignature();

    if (peek().equals("T"))
      return parseTypeVar();

    return null;
  }

  private TypeSignature parseReturn() {
    if (peek().equals("V")) {
      read();
      return new Primitive(primitives.get("V"));
    }
    return parseTypeSignature();
  }

  private ArrayList<TypeSignature> parseParams() {
    read(); // consume '('
    ArrayList<TypeSignature> params = new ArrayList<>();
    while (true) {
      TypeSignature node = parseTypeSignature();
      if (node == null) break;

      params.add(node);
    }
    read(); // consume ')'
    return params;
  }

  private ArrayList<TypeParam> parseTypeParams() {
    ArrayList<TypeParam> typeParams = new ArrayList<>();
    if (!peek().equals("<")) return typeParams;
    read(); // consume '<'
    while (!peek().equals(">")) {
      typeParams.add(parseTypeParam());
    }
    read(); // consume '>'
    return typeParams;
  }

  private TypeParam parseTypeParam() {
    TypeParam node = new TypeParam();
    node.name = parseId();
    node.types = new ArrayList<>();
    while (peek().equals(":")) {
      read();
      FieldTypeSignature n = parseFieldTypeSignature();
      if (n != null)
        node.types.add(n);
    }
    return node;
  }

  private FieldTypeSignature parseFieldTypeSignature() {
    String p = peek();
    if (p.equals("L"))
      return parseClassTypeSignature();

    if (p.equals("["))
      return parseArrayTypeSignature();

    if (p.equals("T"))
      return parseTypeVar();

    return null;
  }

  private FieldTypeSignature parseTypeVar() {
    read(); // consume 'T'
    TypeVar node = new TypeVar(parseId());
    read(); // consume ';'
    return node;
  }

  private FieldTypeSignature parseArrayTypeSignature() {
    read(); // consume '['
    ArrayTypeSignature node = new ArrayTypeSignature();
    node.elementType = parseTypeSignature();
    return node;
  }

  private ArrayList<ClassTypeSignature> parseClassTypeSignatures() {
    ArrayList<ClassTypeSignature> nodes = new ArrayList<>();
    while (peek().equals("L")) {
      nodes.add(parseClassTypeSignature());
    }
    return nodes;
  }

  private ClassTypeSignature parseClassTypeSignature() {
    read(); // consume 'L';
    ClassTypeSignature node = new ClassTypeSignature();
    node.binaryName = parseBinaryName();
    node.typeArgs.addAll(parseTypeArgs());
    node.subTypes.addAll(parseSubTypes());
    read(); // consume ';'
    return node;
  }

  private LinkedList<Sub> parseSubTypes() {
    LinkedList<Sub> subs = new LinkedList<>();
    while (true) {
      String next = peek();
      if (!next.equals("."))
        break;

      read();
      Sub sub = new Sub();
      sub.name = parseId();
      sub.typeArgs.addAll(parseTypeArgs());
      subs.add(sub);
    }
    return subs;
  }

  private LinkedList<TypeArg> parseTypeArgs() {
    LinkedList<TypeArg> args = new LinkedList<>();
    if (!ahead("<"))
      return args;

    read(); // consume '<'
    while (!ahead(">")) {
      args.add(parseTypeArg());
    }
    read(); // consume '>'
    return args;
  }

  private TypeArg parseTypeArg() {
    String c = peek();
    if (c.equals("*")) {
      read(); // consume '*'
      return TypeArg.wildcard;
    }

    TypeArg arg = new TypeArg();
    if (c.equals("+") || c.equals("-")) {
      read(); // consume '+' or '-'
      arg.prefix = c;
    }

    arg.type = parseFieldTypeSignature();
    return arg;
  }

  private String parseBinaryName() {
    StringBuilder name = new StringBuilder();
    while (true) {
      String n = parseId();
      if (n == null)
        break;
      name.append(n);
      if (peek().equals("/")) {
        name.append("/");
        read();
      }
    }
    return name.toString();
  }

  private String parseId() {
    String c = peek();
    if (!isIdStartChar(c))
      return null;

    StringBuilder s = new StringBuilder();
    while (isIdChar(c)) {
      s.append(c);
      read();
      c = peek();
    }
    return s.toString();
  }

  private Primitive parsePrimitive() {
    String c = read();
    return new Primitive(primitives.get(c));
  }

  private void mark() {
    mrk = i;
  }

  private void unmark() {
    i = mrk;
  }

  private String read(int cnt) {
    if (i == EOF)
      return "\0";

    int start = i;
    int end = Math.min(i + cnt, raw.length());
    if (end == raw.length()) {
      i = EOF;
    } else {
      i = end;
    }
    return raw.substring(start, end);
  }

  private String read() {
    return read(1);
  }

  private String peek(int cnt) {
    mark();
    String ret = read(cnt);
    unmark();
    return ret;
  }

  private String peek() {
    return peek(1);
  }

  private boolean ahead(String c) {
    return peek().equals(c);
  }

  private boolean aheadIsPrimitive() {
    return primitives.containsKey(peek());
  }

  private static boolean isIdStartChar(String c) {
    return Character.isLetter(c.charAt(0)) || c.equals("_") || c.equals("$");
  }

  private boolean isIdChar(String c) {
    return isIdStartChar(c) || Character.isDigit(c.charAt(0));
  }
}