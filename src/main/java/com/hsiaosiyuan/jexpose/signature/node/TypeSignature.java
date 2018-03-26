package com.hsiaosiyuan.jexpose.signature.node;

import com.alibaba.fastjson.annotation.JSONField;

public abstract class TypeSignature extends Node {
  public boolean isArray = false;

  @JSONField(serialize = false)
  public boolean isStatic;
}
