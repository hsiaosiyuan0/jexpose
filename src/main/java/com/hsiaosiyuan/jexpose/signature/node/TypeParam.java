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
package com.hsiaosiyuan.jexpose.signature.node;

import java.util.ArrayList;
import java.util.HashSet;

public class TypeParam extends Node {
  public String name;
  public ArrayList<FieldTypeSignature> types;

  public TypeParam() {
    types = new ArrayList<>();
  }

  @Override
  public HashSet<String> collectRefClasses() {
    HashSet<String> refs = new HashSet<>();
    for (FieldTypeSignature ts : types) {
      refs.addAll(ts.collectRefClasses());
    }
    return refs;
  }

  @Override
  public HashSet<String> getDirectRefClasses() {
    HashSet<String> refs = new HashSet<>();
    for (FieldTypeSignature ts : types) {
      refs.addAll(ts.getDirectRefClasses());
    }
    return refs;
  }

  @Override
  protected Node clone() throws CloneNotSupportedException {
    TypeParam node = new TypeParam();
    node.name = name;
    for (TypeSignature t : types) {
      node.types.add((FieldTypeSignature) t.clone());
    }
    return node;
  }

  public TypeArg toTypeArg() {
    TypeArg ta = new TypeArg();
    ta.type = new TypeVar(name);
    return ta;
  }
}
