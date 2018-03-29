package com.hsiaosiyuan.jexpose;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.hsiaosiyuan.jexpose.signature.node.ClassSignature;
import com.hsiaosiyuan.jexpose.signature.node.TypeSignature;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class ProvidersDeflator {
  private String entryName;
  private File entryJar;
  private File libDir;
  private String jrt;

  private ArrayList<String> providerNames;
  private HashMap<String, ClassSignature> resolvedProviders;

  private static File distDir;
  private static File extractedDir;
  private static File outputDir;

  public ProvidersDeflator(String entry, String entryJarPath, String libDirPath, String jrtPath) {
    entryName = entry;
    jrt = jrtPath;

    File file = new File(entryJarPath);
    if (!file.isFile()) throw new IllegalArgumentException("malformed path of entry jar");
    entryJar = file;

    file = new File(libDirPath);
    if (!file.isDirectory()) throw new IllegalArgumentException("malformed path of lib directory");
    libDir = file;

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

  private void extractAndMergeJars() throws ZipException {
    ArrayList<File> jars = scanJarDir(libDir);
    jars.add(entryJar);
    for (File j : jars) {
      ZipFile zipFile = new ZipFile(j);
      zipFile.extractAll(extractedDir.getAbsolutePath());
    }
  }

  private void scanProviders() {
    String entryPath = entryName.replace(".", File.separator);
    File entryDir = new File(Paths.get(extractedDir.getAbsolutePath(), entryPath).toString());
    providerNames.addAll(walkAndScanProviders(entryDir.getAbsolutePath(), entryDir));
  }

  private ArrayList<String> walkAndScanProviders(String root, File dir) {
    ArrayList<String> ret = new ArrayList<>();
    File[] files = dir.listFiles();
    if (files == null) return ret;
    for (File f : files) {
      if (f.isDirectory()) {
        ret.addAll(walkAndScanProviders(root, f));
      } else if (FilenameUtils.getBaseName(f.getName()).endsWith("Provider")) {
        String relativePath = f.getAbsolutePath().replace(root, "");
        String name = FilenameUtils.removeExtension(relativePath).replace(File.separator, ".");
        ret.add(entryName + name);
      }
    }
    return ret;
  }

  private void doResolve() throws IOException, ExecutionException, InterruptedException {
    for (String pn : providerNames) {
      ClassSignature cs = new ClassResolver(jrt, extractedDir.getAbsolutePath(), pn).resolve().get();
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
