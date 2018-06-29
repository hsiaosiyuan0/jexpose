package com.hsiaosiyuan.jexpose;

import net.lingala.zip4j.exception.ZipException;

import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;

public class App {

  public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, ZipException {
    if (args.length != 4)
      throw new IllegalArgumentException("please specify `entry` `entryJarPath` `libDirPath` `providerSuffix`");

    PrintStream out = new PrintStream(System.out, true, "UTF-8");
    System.setOut(out);
    long tb = System.currentTimeMillis();
    String outDir = new ProvidersDeflator(args[0], args[1], args[2], args[3]).process();
    long te = System.currentTimeMillis();
    double elapsed = (te - tb) / 1000.0;
    System.out.println("Output at: " + outDir);
    System.out.println("elapsed: " + elapsed + "s");
  }
}
