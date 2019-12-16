
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

import java.util.HashSet;

public class TypeArg extends Node {
  public static final TypeArg wildcard = new TypeArg(true);

  public boolean isWildcard;
  public String prefix;
  public FieldTypeSignature type;

  public TypeArg() {
  }

  public TypeArg(boolean isWildcard) {
    this.isWildcard = isWildcard;
  }

  @Override
  public HashSet<String> collectRefClasses() {
    if (type == null) return new HashSet<>();
    return type.collectRefClasses();
  }

  @Override
  public HashSet<String> getDirectRefClasses() {
    if (type == null) return new HashSet<>();
    return type.getDirectRefClasses();
  }

  @Override
  protected Node clone() throws CloneNotSupportedException {
    TypeArg node = new TypeArg();
    node.isWildcard = isWildcard;
    node.prefix = prefix;
    if (type != null)
      node.type = (FieldTypeSignature) type.clone();
    return node;
  }
}