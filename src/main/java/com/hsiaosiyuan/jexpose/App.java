package com.hsiaosiyuan.jexpose;

import com.alibaba.fastjson.JSON;
import com.hsiaosiyuan.jexpose.signature.Parser;
import com.hsiaosiyuan.jexpose.signature.node.Node;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

public class App {
  public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, ZipException {
    if (args.length != 3)
      throw new IllegalArgumentException("请指定 jar 和 entry interface");

    File jrt = new File(args[0]);
    if (!jrt.exists() || !jrt.isDirectory()) {
      throw new IllegalArgumentException("jrt 路径错误");
    }

    File jar = new File(args[1]);
    if (!jar.exists() || !jar.isFile()) {
      throw new IllegalArgumentException("jar 路径错误");
    }

    String entry = args[2];

    System.out.println("正在解压 Jar 文件");
    long tb = System.currentTimeMillis();

    Path extractPath = extractJar(jar);
    long te = System.currentTimeMillis();
    double elapsed = (te - tb) / 1000.0;
    System.out.println("解压到: " + extractPath + " 耗时: " + elapsed + "s");

    Node node = new ClassResolver(jrt.toPath().toString(), extractPath.toString(), entry).resolve().get();
    System.out.println(node);

    String dist = writeJson2file(jar, JSON.toJSONString(node, true));
    System.out.println(dist);

    String dist2 = writeJson2file(jar, JSON.toJSONString(ClassResolver.getClassPool(), true));
    System.out.println(dist2);

//    new Parser("(Ljava/lang/String;[B)Z").parseMethodTypeSignature();

//    Node node = new Parser("Ljava/util/HashMap<TK;TV;>.HashIterator<TK;>;").parseTypeSignature();
//    Node node = new Parser("<T:Ljava/lang/Object;>(I)Ljava/lang/Class<+TT;>;").parseMethodTypeSignature();
//    System.out.println(node);
  }

  private static Path extractJar(File jar) throws IOException, ZipException {
    String fileName = jar.getName();
    Path dist = Files.createTempDirectory(fileName);
    ZipFile zipFile = new ZipFile(jar);
    zipFile.extractAll(dist.toAbsolutePath().toString());
    return dist;
  }

  private static String writeJson2file(File jar, String json) throws IOException {
    Path tempFile = Files.createTempFile(jar.getName(), ".json");
    BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile.toFile()));
    writer.write(json);
    writer.flush();
    return tempFile.toAbsolutePath().toString();
  }
}
