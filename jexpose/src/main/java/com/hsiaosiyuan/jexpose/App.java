package com.hsiaosiyuan.jexpose;

import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

public class App {

	public static void main(String[] args1) throws IOException, ExecutionException, InterruptedException {
		// Compatible historical version
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
			} catch (Exception e) {
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

		String outDir = new ProvidersDeflator(line.getOptionValue("entry"), line.getOptionValue("entry-jar"),
				line.getOptionValue("lib"), providerSuffix, include, exclude).process();

		System.out.println("Output at: " + outDir);
		// This line needs to be removed to prevent a directory error from js fetching
		// newlines
//		System.out.println("elapsed: " + elapsed + "s");
	}

}
