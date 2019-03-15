package com.hsiaosiyuan.jexpose;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import net.lingala.zip4j.exception.ZipException;

public class App {

	public static void main(String[] args1) throws IOException, ExecutionException, InterruptedException, ZipException {
		for (int i = 0; i < args1.length; i++) {
			if (args1[i].indexOf("-") != 0) {
				if (i == 0) {
					args1[i] = ("-entry" + args1[i]);
				}
				if (i == 1) {
					args1[i] = ("-entry-jar" + args1[i]);
				}
				if (i == 2) {
					args1[i] = ("-lib" + args1[i]);
				}
				if (i == 3) {
					args1[i] = ("-provider-suffix" + args1[i]);
				}
				if (i == 4) {
					args1[i] = ("-output" + args1[i]);
				}
			}
			System.out.println(args1[i] + "   " + i);
		}

		Options options = new Options();
		String[] args = args1;

		options.addOption("t", "entry", true, "the entry point to start our exposing");
		options.addOption("j", "entry-jar", true, "path of target jar");
		options.addOption("l", "lib", true, "dir contains dependencies of target jar");
		options.addOption("s", "provider-suffix", true, "suffix of provider name");
		options.addOption("i", "include", true, "regexp for including files");
		options.addOption("e", "exclude", true, "regexp for excluding files");
		options.addOption("o", "output", true, "output dir");

		CommandLineParser parser = new DefaultParser();
		CommandLine line = null;
		String providerSuffix = null;
		Pattern include = null;
		Pattern exclude = null;
		try {
			line = parser.parse(options, args);
			if (!line.hasOption("entry")) {
				throw new Exception("missing entry");
			}
			if (!line.hasOption("entry-jar")) {
				throw new Exception("missing entry-jar");
			}
			if (!line.hasOption("lib")) {
				throw new Exception("missing lib");
			}
			providerSuffix = line.getOptionValue("provider-suffix");

			String includeStr = line.getOptionValue("include");
			if (providerSuffix == null && includeStr == null) {
				throw new Exception("missing either provider-suffix or filter");
			}
			if (includeStr != null) {
				include = Pattern.compile(includeStr);
			}

			String excludeStr = line.getOptionValue("exclude");
			if (excludeStr != null) {
				exclude = Pattern.compile(excludeStr);
			}
			try {
				String outputdir = line.getOptionValue("output");
				deleteFile(new File(outputdir));
			} catch (Exception e) {
				System.out.println("删除outputdir错误");
				System.out.println();
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

		String outDir = new ProvidersDeflator(line.getOptionValue("entry"), line.getOptionValue("entry-jar"),
				line.getOptionValue("lib"), providerSuffix, include, exclude).process();

		long te = System.currentTimeMillis();
		double elapsed = (te - tb) / 1000.0;
		System.out.println("Output at: " + outDir);
//		System.out.println("elapsed: " + elapsed + "s");
	}

	public static void deleteFile(File file) {
		file.setWritable(true);
		// 判断传递进来的是文件还是文件夹,如果是文件,直接删除,如果是文件夹,则判断文件夹里面有没有东西
		if (file.isDirectory()) {
			// 如果是目录,就删除目录下所有的文件和文件夹
			File[] files = file.listFiles();
			// 遍历目录下的文件和文件夹
			for (File f : files) {
				f.setWritable(true);
				// 如果是文件,就删除
				if (f.isFile()) {
					System.out.println("已经被删除的文件:" + f);
					// 删除文件
					f.delete();
				} else if (file.isDirectory()) {
					// 如果是文件夹,就递归调用文件夹的方法
					deleteFile(f);
				}
			}
			// 删除文件夹自己,如果它低下是空的,就会被删除
			System.out.println("已经被删除的文件夹:" + file);
			file.delete();
		}
		// 如果是文件,就直接删除自己
		System.out.println("已经被删除的文件:" + file);
		file.delete();
	}

}
