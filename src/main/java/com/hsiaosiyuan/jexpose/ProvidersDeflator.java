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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.hsiaosiyuan.jexpose.signature.node.ClassSignature;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.progress.ProgressMonitor;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ProvidersDeflator {
  private String entryName;
  private File entryJar;
  private File libDir;
  private String providerSuffix;
  private Pattern include;
  private Pattern exclude;

  private ArrayList<String> providerNames;
  private HashMap<String, ClassSignature> resolvedProviders;

  private static File distDir;
  private static File extractedDir;
  private static File outputDir;

  public ProvidersDeflator(
    String entry,
    String entryJarPath,
    String libDirPath,
    String providerSuffix,
    Pattern include,
    Pattern exclude
  ) {
    this.providerSuffix = providerSuffix;
    entryName = entry;

    File file = new File(entryJarPath);
    if (!file.isFile()) throw new IllegalArgumentException("malformed path of entry jar");
    entryJar = file;

    file = new File(libDirPath);
    if (!file.isDirectory()) throw new IllegalArgumentException("malformed path of lib directory");
    libDir = file;

    this.include = include;
    this.exclude = exclude;

    providerNames = new ArrayList<>();
    resolvedProviders = new HashMap<>();
  }

  public String process() throws IOException, ZipException, ExecutionException, InterruptedException {
    makeOutputDir();
    extractAndMergeJars();
    scanProviders();
    doResolve();
    saveResult();
    return distDir.getAbsolutePath();
  }

  private void makeOutputDir() throws IOException {
    distDir = new File(Files.createTempDirectory("jexpose").toAbsolutePath().toString());
    extractedDir = new File(Paths.get(distDir.getAbsolutePath(), "extracted").toString());
    outputDir = new File(Paths.get(distDir.getAbsolutePath(), "output").toString());
    if (!extractedDir.mkdir()) throw new IOException("unable to create dir: " + extractedDir.toString());
    if (!outputDir.mkdir()) throw new IOException("unable to create dir: " + outputDir.toString());
  }

  private ArrayList<File> scanJarDir(File jarDir) {
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

  private void extractAndMergeJars() throws ZipException, InterruptedException {
    ArrayList<File> jars = scanJarDir(libDir);
    jars.add(entryJar);
    LinkedList<ZipFile> zipFiles = new LinkedList<>();
    for (File j : jars) {
      ZipFile zipFile = new ZipFile(j.getAbsolutePath());
      zipFiles.add(zipFile);
      zipFile.setRunInThread(true);
      try {
        zipFile.extractAll(extractedDir.getAbsolutePath());
      } catch (ZipException e) {
        System.out.println(Colorize.error("ZipException:" + e.getMessage()));
      }
    }
    while (zipFiles.size() > 0) {
      zipFiles.removeIf(file -> file.getProgressMonitor().getState() != ProgressMonitor.STATE_BUSY);
      Thread.sleep(300);
    }
  }

  private void scanProviders() {
    String entryPath = entryName.replace(".", File.separator);
    File entryDir = new File(Paths.get(extractedDir.getAbsolutePath(), entryPath).toString());
    providerNames.addAll(walkAndScanProviders(entryDir.getAbsolutePath(), entryDir));
  }

  private boolean isBlackFile(File file) {
    return this.exclude != null && this.exclude.matcher(filenameWithoutExt(file)).matches();
  }

  private String filenameWithoutExt(File file) {
    String name = file.getAbsolutePath();
    int dot = name.lastIndexOf(".");
    return name.substring(0, dot);
  }

  private boolean isWhiteFile(File file) {
    String filename = filenameWithoutExt(file);
    if (this.providerSuffix != null) {
      String basename = FilenameUtils.getBaseName(filename);
      return basename.endsWith(this.providerSuffix) && !isBlackFile(file);
    }
    return this.include.matcher(filename).matches() && !isBlackFile(file);
  }

  private ArrayList<String> walkAndScanProviders(String root, File dir) {
    ArrayList<String> ret = new ArrayList<>();
    File[] files = dir.listFiles();
    if (files == null) return ret;
    for (File f : files) {
      if (f.isDirectory()) {
        ret.addAll(walkAndScanProviders(root, f));
      } else if (isWhiteFile(f)) {
        String relativePath = f.getAbsolutePath().replace(root, "");
        String name = FilenameUtils.removeExtension(relativePath).replace(File.separator, ".");
        ret.add(entryName + name);
      }
    }
    return ret;
  }

  private void doResolve() throws IOException, ExecutionException, InterruptedException {
    for (String pn : providerNames) {
      ClassSignature cs = new ClassResolver(extractedDir.getAbsolutePath(), pn).resolve().get();
      resolvedProviders.put(pn, cs);
    }
  }

  private void saveResult() throws IOException {
    ArrayList<String> providers = new ArrayList<>(resolvedProviders.keySet());
    HashMap<String, ClassSignature> classPool = ClassResolver.getClassPoolWithoutBuiltin();

    Result result = new Result();
    result.providers = providers;
    result.classes = (HashMap<String, ClassSignature>) classPool.entrySet().stream()
      .filter(item -> !item.getKey().equals("$VALUES"))
      .collect(Collectors.toMap(p -> p.getKey().replace("/", "."), Map.Entry::getValue));

    int feature = JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.DisableCircularReferenceDetect.getMask();
    feature |= SerializerFeature.PrettyFormat.getMask();
    writeJson2file(JSON.toJSONString(result, feature).replace("\t", "  "));
  }

  private static void writeJson2file(String json) throws IOException {
    File tf = new File(Paths.get(outputDir.getAbsolutePath(), "deflated.json").toAbsolutePath().toString());
    BufferedWriter writer = new BufferedWriter(new FileWriter(tf));
    writer.write(json);
    writer.flush();
  }

  public class Result {
    public ArrayList<String> providers;
    public HashMap<String, ClassSignature> classes;
  }
}
