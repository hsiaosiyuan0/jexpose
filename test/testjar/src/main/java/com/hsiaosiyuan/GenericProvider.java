package com.hsiaosiyuan;

import java.util.Date;

public interface GenericProvider {
  void m1(ClassC a);

  void m2(ClassC<ClassE, Date> a);

  void m3(ClassF a);

  int m1(int a);
}
