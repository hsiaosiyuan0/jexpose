/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hsiaosiyuan.jexpose;

import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

/**
 * @author hsiaosiyuan
 */
public class App {

  public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, ZipException {
    Options options = new Options();
    options.addOption("t", "entry", true, "the entry point to start our exposing");
    options.addOption("j", "entry-jar", true, "path of target jar");
    options.addOption("l", "lib", true, "dir contains dependencies of target jar");
    options.addOption("s", "provider-suffix", true, "suffix of provider name");
    options.addOption("i", "include", true, "regexp for including files");
    options.addOption("e", "exclude", true, "regexp for excluding files");

    CommandLineParser parser = new DefaultParser();
    CommandLine line = null;
    String providerSuffix = null;
    Pattern include = null;
    Pattern exclude = null;
    try {
      line = parser.parse(options, args);
      if (!line.hasOption("entry")) {
        throw new IllegalArgumentException("missing entry");
      }
      if (!line.hasOption("entry-jar")) {
        throw new IllegalArgumentException("missing entry-jar");
      }
      if (!line.hasOption("lib")) {
        throw new IllegalArgumentException("missing lib");
      }
      providerSuffix = line.getOptionValue("provider-suffix");

      String includeStr = line.getOptionValue("include");
      if (providerSuffix == null && includeStr == null) {
        throw new IllegalArgumentException("missing either provider-suffix or filter");
      }
      if (includeStr != null) {
        include = Pattern.compile(includeStr);
      }

      String excludeStr = line.getOptionValue("exclude");
      if (excludeStr != null) {
        exclude = Pattern.compile(excludeStr);
      }
    } catch (Exception e) {
      System.out.println(e.getMessage() + "\n");
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("jexpose", options, true);
      System.exit(1);
    }

    PrintStream out = new PrintStream(System.out, true, "UTF-8");
    System.setOut(out);
    long tb = System.currentTimeMillis();

    String outDir = new ProvidersDeflator(
      line.getOptionValue("entry"),
      line.getOptionValue("entry-jar"),
      line.getOptionValue("lib"),
      providerSuffix,
      include,
      exclude).process();

    long te = System.currentTimeMillis();
    double elapsed = (te - tb) / 1000.0;
    System.out.println("Output at: " + outDir);
    System.out.println("elapsed: " + elapsed + "s");
  }
}
