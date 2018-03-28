package com.hsiaosiyuan;

import java.util.ArrayList;
import java.util.Date;

enum Gender {
  Female, Male,
}

class Person<T, E extends Date> extends Primitive {
  public String name;
  public Integer age;
  public Gender gender;
  public int height;
  public int weight;
  public ArrayList<User>[] arr;
  public E personField;
}
