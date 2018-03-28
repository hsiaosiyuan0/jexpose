package com.hsiaosiyuan;

import java.util.Date;

public class ClassB<TB extends ClassD, TB1 extends Date> extends ClassA<TB1> {
  TB fieldB;
}
