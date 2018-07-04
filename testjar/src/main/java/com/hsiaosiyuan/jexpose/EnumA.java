package com.hsiaosiyuan.jexpose;

public enum EnumA {

  A1(1),
  A2(2),
  A3(3),
  A4(100);

  private int v;

  int getV() {
    return v;
  }

  EnumA(int v) {
    this.v = v;
  }
}
