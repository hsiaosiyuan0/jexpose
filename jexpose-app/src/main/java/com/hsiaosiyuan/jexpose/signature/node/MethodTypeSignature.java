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

import com.alibaba.fastjson.annotation.JSONField;

import java.util.ArrayList;
import java.util.HashSet;

public class MethodTypeSignature extends Node {
  public ArrayList<TypeParam> typeParams;
  public ArrayList<TypeSignature> params;
  public TypeSignature ret;

  public ArrayList<String> formalParams;

  public boolean isOverride;

  @JSONField(serialize = false)
  public ArrayList<Node> exceptions;

  public MethodTypeSignature() {
    typeParams = new ArrayList<>();
    params = new ArrayList<>();
    exceptions = new ArrayList<>();
    formalParams = new ArrayList<>();
  }

  @Override
  public HashSet<String> collectRefClasses() {
    HashSet<String> refs = new HashSet<>();
    for (TypeParam tp : typeParams) {
      refs.addAll(tp.collectRefClasses());
    }
    for (TypeSignature ts : params) {
      refs.addAll(ts.collectRefClasses());
    }
    refs.addAll(ret.collectRefClasses());
    return refs;
  }

  @Override
  public HashSet<String> getDirectRefClasses() {
    HashSet<String> refs = new HashSet<>();
    for (TypeParam tp : typeParams) {
      refs.addAll(tp.getDirectRefClasses());
    }
    for (TypeSignature ts : params) {
      refs.addAll(ts.getDirectRefClasses());
    }
    refs.addAll(ret.getDirectRefClasses());
    return refs;
  }

  @Override
  protected Node clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
}
