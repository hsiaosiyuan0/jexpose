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

import java.util.HashMap;
import java.util.HashSet;

public class Primitive extends TypeSignature {
  @JSONField(serialize = false)
  public String binaryName;

  public Primitive(String binaryName) {
    this.binaryName = binaryName;
  }

  @Override
  public HashSet<String> collectRefClasses() {
    return new HashSet<String>() {{
      add(binaryName);
    }};
  }

  @Override
  public HashSet<String> getDirectRefClasses() {
    return new HashSet<>();
  }

  @Override
  protected Node clone() throws CloneNotSupportedException {
    return new Primitive(binaryName);
  }

  @JSONField(name = "name")
  public String getName() {
    return binaryName.replace("/", ".");
  }

  @Override
  void applyTypeArgs(HashMap<String, TypeArg> args) {
  }
}