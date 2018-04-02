package com.hsiaosiyuan.jexpose;

public enum EnumB {
  B1("1"),
  B2("2"),
  B3("3"),
  B4("4");

  private String v;

  String getV() {
    return v;
  }

  EnumB(String v) {
    this.v = v;
  }
}
