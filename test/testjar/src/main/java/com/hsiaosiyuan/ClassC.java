package com.hsiaosiyuan;

import java.util.Date;

public class ClassC<TC1 extends ClassD, TC2 extends Date> extends ClassB<TC1, TC2> {
  TC1 fieldTC1;
  TC2 fieldTC2;
}
