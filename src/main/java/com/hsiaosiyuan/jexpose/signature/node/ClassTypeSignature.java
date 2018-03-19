package com.hsiaosiyuan.jexpose.signature.node;

import java.util.ArrayList;
import java.util.LinkedList;

public class ClassTypeSignature extends FieldTypeSignature {
  public ArrayList<String> fullQualifiedName;
  public LinkedList<TypeArg> typeArgs;
  public LinkedList<Sub> subTypes;

  public static class Sub {
    public String name;
    public LinkedList<TypeArg> typeArgs;
  }

  public String getFullQualifiedName() {
    if (fullQualifiedName.size() == 0) return "";
    if (fullQualifiedName.size() == 1) return fullQualifiedName.get(0);

    StringBuilder s = new StringBuilder();
    s.append(fullQualifiedName.get(0));
    for (int i = 1; i < fullQualifiedName.size(); i++) {
      s.append("/").append(fullQualifiedName.get(i));
    }
    return s.toString();
  }

  public boolean hasTypeArgs(){
    return typeArgs != null && typeArgs.size() > 0;
  }
}