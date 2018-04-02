package com.hsiaosiyuan.jexpose;

public enum EnumD {

  D1(1L),
  D2(0L),
  D3(1234567891234L),
  D4(5L),
  D5(100L);


  private long v;

  EnumD(long v) {
    this.v = v;
  }
}
