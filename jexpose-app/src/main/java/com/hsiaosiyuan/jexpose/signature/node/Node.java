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

import java.util.HashSet;

public abstract class Node {

  @JSONField(serialize = false)
  public boolean isPrimitive() {
    return this instanceof Primitive;
  }

  @JSONField(serialize = false)
  public boolean isArray() {
    return this instanceof ArrayTypeSignature;
  }

  @JSONField(serialize = false)
  public boolean isClassType() {
    return this instanceof ClassTypeSignature;
  }

  @JSONField(serialize = false)
  public boolean isClass() {
    return this instanceof ClassSignature;
  }

  @JSONField(serialize = false)
  public boolean isTypeVar() {
    return this instanceof TypeVar;
  }

  @JSONField(serialize = false)
  public Primitive asPrimitive() {
    return (Primitive) this;
  }

  @JSONField(serialize = false)
  public ArrayTypeSignature asArray() {
    return (ArrayTypeSignature) this;
  }

  @JSONField(serialize = false)
  public ClassTypeSignature asClassType() {
    return (ClassTypeSignature) this;
  }

  @JSONField(serialize = false)
  public ClassSignature asClass() {
    return (ClassSignature) this;
  }

  @JSONField(serialize = false)
  public TypeVar asTypeVar() {
    return (TypeVar) this;
  }

  @JSONField(serialize = false)
  public abstract HashSet<String> collectRefClasses();

  @JSONField(serialize = false)
  public abstract HashSet<String> getDirectRefClasses();

  protected abstract Node clone() throws CloneNotSupportedException;
}