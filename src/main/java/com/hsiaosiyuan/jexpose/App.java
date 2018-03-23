package com.hsiaosiyuan.jexpose;

import com.alibaba.fastjson.JSON;
import com.hsiaosiyuan.jexpose.signature.node.Node;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class App {
  private static File distDir;
  private static File extractedDir;
  private static File outputDir;

  public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, ZipException {
    if (args.length != 3)
      throw new IllegalArgumentException("please specific jar-dir and entry-interface");

    File jrt = new File(args[0]);
    if (!jrt.exists() || !jrt.isDirectory()) {
      throw new IllegalArgumentException("deformed jrt-path");
    }

    File jarDir = new File(args[1]);
    if (!jarDir.exists() || !jarDir.isDirectory()) {
      throw new IllegalArgumentException("deformed jar-dir");
    }

    String entry = args[2];

    resolveOutputDir();

    System.out.println("extracting and merging jars...");
    long tb = System.currentTimeMillis();

    extractAndMergeJars(jarDir);
    long te = System.currentTimeMillis();
    double elapsed = (te - tb) / 1000.0;
    System.out.println("elapsed: " + elapsed + "s");

    System.out.println("\nexposing...");
    tb = System.currentTimeMillis();
    Node node = new ClassResolver(jrt.toPath().toString(), extractedDir.getAbsolutePath(), entry).resolve().get();
    te = System.currentTimeMillis();
    elapsed = (te - tb) / 1000.0;
    System.out.println("elapsed: " + elapsed + "s");

    writeJson2file("jexpose-entry", JSON.toJSONString(node, true));
    writeJson2file("jexpose-class-pool", JSON.toJSONString(ClassResolver.getClassPool(), true));

    System.out.println("Output at: " + distDir.getAbsolutePath());
  }

  private static void resolveOutputDir() throws IOException {
    distDir = new File(Files.createTempDirectory("jexpose").toAbsolutePath().toString());
    extractedDir = new File(Paths.get(distDir.getAbsolutePath(), "extracted").toString());
    outputDir = new File(Paths.get(distDir.getAbsolutePath(), "output").toString());
    if (!extractedDir.mkdir()) throw new IOException("unable to create dir: " + extractedDir.toString());
    if (!outputDir.mkdir()) throw new IOException("unable to create dir: " + outputDir.toString());
  }

  private static ArrayList<File> scanJarDir(File jarDir) {
    ArrayList<File> jars = new ArrayList<>();
    File[] files = jarDir.listFiles();
    if (files == null) return jars;
    for (File f : files) {
      if (FilenameUtils.isExtension(f.getName(), "jar")) {
        jars.add(f);
      }
    }
    return jars;
  }

  private static void extractAndMergeJars(File jarDir) throws IOException, ZipException {
    ArrayList<File> jars = scanJarDir(jarDir);
    for (File j : jars) {
      ZipFile zipFile = new ZipFile(j);
      zipFile.extractAll(extractedDir.getAbsolutePath());
    }
  }

  private static void writeJson2file(String name, String json) throws IOException {
    File tf = new File(Paths.get(outputDir.getAbsolutePath(), name + ".json").toAbsolutePath().toString());
    BufferedWriter writer = new BufferedWriter(new FileWriter(tf));
    writer.write(json);
    writer.flush();
  }
}
