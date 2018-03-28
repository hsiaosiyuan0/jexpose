package com.hsiaosiyuan;

import org.apache.http.message.BasicHeader;

import java.util.*;

public interface ServiceProvider1 {

  ArrayList<User> users = new ArrayList<User>();

  boolean login(String username, byte[] password);

  User<String, Date> getUser(String username);

  User getUser(int id);

  ArrayList<User> getUserFriends(User user);

  // (Ljava/util/ArrayList<Lorg/apache/http/message/BasicHeader;>;[[I)Z
  boolean doSomething1(ArrayList<BasicHeader> headers, int[][] a);

  ArrayList<User> doSomething2(HashMap<String, ArrayList<BasicHeader>> headersMap, int[][] a, ArrayList<Integer>[] b);

  Map doSomething3(TreeMap<String, User> map, int[][] a);

  int doSomething4(char p1, double p2, long p3, int p4, User[] p5);
}
