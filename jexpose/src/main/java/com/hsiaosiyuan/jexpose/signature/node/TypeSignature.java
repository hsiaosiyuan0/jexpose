package com.hsiaosiyuan.jexpose.signature.node;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.HashMap;

public abstract class TypeSignature extends Node {
  @JSONField(serialize = false)
  public boolean isStatic;

  abstract void applyTypeArgs(HashMap<String, TypeArg> args);
}
