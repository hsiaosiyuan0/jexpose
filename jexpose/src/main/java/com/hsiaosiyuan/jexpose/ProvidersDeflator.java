package com.hsiaosiyuan.jexpose;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.hsiaosiyuan.jexpose.signature.node.ClassSignature;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;

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

	public ProvidersDeflator(String entry, String entryJarPath, String libDirPath, String providerSuffix,
			Pattern include, Pattern exclude) {
		this.providerSuffix = providerSuffix;
		entryName = entry;

		File file = new File(entryJarPath);
		if (!file.isFile())
			throw new IllegalArgumentException("malformed path of entry jar");
		entryJar = file;

		file = new File(libDirPath);
		if (!file.isDirectory())
			throw new IllegalArgumentException("malformed path of lib directory");
		libDir = file;

		this.include = include;
		this.exclude = exclude;

		providerNames = new ArrayList<>();
		resolvedProviders = new HashMap<>();
	}

	public String process() throws IOException, ExecutionException, InterruptedException {
		makeOutputDir();
		extractAndMergeJars();
		scanProviders();
		doResolve();
		saveResult();
		return distDir.getAbsolutePath();
	}

	private void makeOutputDir() throws IOException {
		String userhome = System.getProperty("user.dir").replaceAll("/", File.separator);
		System.out.println("Get user directory:" + userhome);
		String masterdir = userhome + File.separator + "jexpose" + File.separator;
		System.out.println("Clear working directory:" + masterdir);

		String te = (userhome + File.separator + "jexpose" + File.separator + UUID.randomUUID()).replaceAll("/",
				File.separator);
		FileUtil.mkdir(te);
		System.out.println("Generate temporary directory:" + te);
		distDir = new File(te);
		extractedDir = new File(Paths.get(distDir.getAbsolutePath(), "extracted").toString());
		outputDir = new File(Paths.get(distDir.getAbsolutePath(), "output").toString());
		if (!extractedDir.mkdir())
			throw new IOException("unable to create dir: " + extractedDir.toString());
		if (!outputDir.mkdir())
			throw new IOException("unable to create dir: " + outputDir.toString());
	}

	private ArrayList<File> scanJarDir(File jarDir) {
		ArrayList<File> jars = new ArrayList<>();
		File[] files = jarDir.listFiles();
		if (files == null)
			return jars;
		for (File f : files) {
			if (FilenameUtils.isExtension(f.getName(), "jar")) {
				jars.add(f);
			}
		}
		return jars;
	}

	private void extractAndMergeJars() throws InterruptedException {
		ArrayList<File> jars = scanJarDir(libDir);
		jars.add(entryJar);
		for (File j : jars) {
			try {
				ZipUtil.unzip(j, new File(extractedDir.getAbsolutePath()));
			} catch (Exception e) {
				System.out.println(Colorize.error("ZipException:" + e.getMessage()));
			}
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
		if (files == null)
			return ret;
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
		String path = (outputDir.getAbsolutePath() + File.separator + "deflated.json").replaceAll("/", File.separator);
		cn.hutool.core.io.file.FileWriter fileWriter = new cn.hutool.core.io.file.FileWriter(path);
		fileWriter.write(json);

	}

	public class Result {
		public ArrayList<String> providers;
		public HashMap<String, ClassSignature> classes;
	}

}
