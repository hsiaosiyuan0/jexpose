package com.hsiaosiyuan;

import java.util.ArrayList;
import java.util.Date;

class User<B, E extends Date> extends Person<E, E> {
  public int id;
  public String username;
  public byte[] password;
  public ArrayList<User> friends;
  public B userField;
}
