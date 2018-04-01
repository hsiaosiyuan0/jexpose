package com.hsiaosiyuan;

import java.util.Date;

public interface GenericProvider {
  void m1(ClassC a);

  void m2(ClassC<ClassG, Date> a);

  void m3(ClassF a);

  int m4(ClassE a);
}
