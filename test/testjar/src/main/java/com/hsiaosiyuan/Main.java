package com.hsiaosiyuan;

import java.lang.reflect.Field;
import java.util.Date;

public class Main {

  public static void main(String[] args) throws NoSuchFieldException {
    User<String, Date> u = (User<String, Date>) (new User());
    Class<?> c = u.getClass();
    Field f = c.getField("personField");
    System.out.format("Type: %s%n", f.getType());
    f = c.getField("id");
    System.out.format("Type: %s%n", f.getType());
  }
}
