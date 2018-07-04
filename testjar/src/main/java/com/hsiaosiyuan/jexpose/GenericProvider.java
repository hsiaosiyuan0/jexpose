package com.hsiaosiyuan.jexpose;

import java.util.Date;

public interface GenericProvider {
  void m1(ClassC a);

  void m2(ClassC<ClassG, Date> a);

  void m3(ClassF a);

  int m4(ClassE a);

  void m5(ClassH a);

  void m6(EnumA a);

  void m7(EnumB a);

  void m8(EnumC a);

  void m9(EnumD a);

  void m10(EnumE a);
}
