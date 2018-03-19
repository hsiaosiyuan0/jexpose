package com.hsiaosiyuan.jexpose.signature;

import java.util.Map;

import com.hsiaosiyuan.jexpose.signature.node.*;
import com.hsiaosiyuan.jexpose.signature.node.ClassTypeSignature.Sub;

import static java.util.Map.entry;

import java.util.ArrayList;
import java.util.LinkedList;

public class Parser {
  private static final Map<String, String> primitives = Map.ofEntries(
    entry("B", "java/lang/Byte"), entry("C", "java/lang/Character"),
    entry("D", "java/lang/Double"), entry("F", "java/lang/Float"),
    entry("I", "java/lang/Integer"), entry("J", "java/lang/Long"),
    entry("S", "java/lang/Short"), entry("Z", "java/lang/Boolean"),
    entry("V", "java/lang/Void"));

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
    node.typeParams = parseTypeParams();
    node.params = parseParams();
    node.ret = parseReturn();
    node.exceptions = parseExceptions();
    return node;
  }

  private ArrayList<Node> parseExceptions() {
    if (!peek().equals("^")) return null;

    ArrayList<Node> nodes = new ArrayList<>();
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
      return VoidType.v;
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
    if (!peek().equals("<")) return null;
    read(); // consume '<'
    ArrayList<TypeParam> typeParams = new ArrayList<>();
    while (!peek().equals(">")) {
      typeParams.add(parseParam());
    }
    read(); // consume '>'
    return typeParams;
  }

  private TypeParam parseParam() {
    TypeParam node = new TypeParam();
    node.name = parseId();
    node.types = new ArrayList<>();
    while (peek().equals(":")) {
      read();
      node.types.add(parseFieldTypeSignature());
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

  private FieldTypeSignature parseClassTypeSignature() {
    read(); // consume 'L';
    ClassTypeSignature node = new ClassTypeSignature();
    node.fullQualifiedName = parseFullQualifiedName();
    node.typeArgs = parseTypeArgs();
    node.subTypes = parseSubTypes();
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
      sub.typeArgs = parseTypeArgs();
      subs.add(sub);
    }
    return subs;
  }

  private LinkedList<TypeArg> parseTypeArgs() {
    if (!ahead("<"))
      return null;

    read(); // consume '<'
    LinkedList<TypeArg> args = new LinkedList<>();
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

  private ArrayList<String> parseFullQualifiedName() {
    ArrayList<String> name = new ArrayList<>();
    while (true) {
      String n = parseId();
      if (n == null)
        break;
      name.add(n);
      if (peek().equals("/")) {
        read();
      }
    }
    return name;
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