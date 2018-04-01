package com.hsiaosiyuan;

import java.util.*;

public class ClassB<TB extends ClassD, TB1 extends Date> extends ClassA<TB1> {
  TB fieldB1;
  List<TB> fieldB2;
  HashMap<String, ArrayList<TB>> fieldB3;
}
