package com.rolandoislas.streamsitej.data;

import com.rolandoislas.streamsitej.util.StringUtil;

import java.util.Arrays;
import java.util.List;

public class ArgumentParser {
	private final List<String> argsList;
	public final int widthWebsite;
	public final int heightWebsite;
	public final int fpsIn;
	public final boolean reload;
	public final String backgroundColor;
	public final int fpsOut;
	public final int keyInt;
	public final int audioBitrate;
	public final int videoBitrate;
	public final int widthVideo;
	public final int heightVideo;
	public final String url;
	public final String rtmp;
	public String logLevel;
	public String preset;

	public ArgumentParser(String[] args) {
		argsList = Arrays.asList(args);
		if (hasOption("-h", "--help") || args.length < 2)
			showHelp();
		url = args[0];
		rtmp = args[1];
		logLevel = getArgAfter("--log", "INFO");
		widthWebsite = StringUtil.parseInt(getArgAfter("--width-website", "1920"));
		heightWebsite = StringUtil.parseInt(getArgAfter("--height-website", "1080"));
		fpsIn = StringUtil.parseInt(getArgAfter("--fps-in", "1"));
		reload = hasOption("--reload");
		backgroundColor = getArgAfter("--background", "#ffffff");
		fpsOut = StringUtil.parseInt(getArgAfter("--fps-out", "30"));
		keyInt = StringUtil.parseInt(getArgAfter("--keyint", "2"));
		audioBitrate = StringUtil.parseInt(getArgAfter("--audio-bitrate", "92000"));
		videoBitrate = StringUtil.parseInt(getArgAfter("--video-bitrate", "3500000"));
		widthVideo = StringUtil.parseInt(getArgAfter("--width-video", "1920"));
		heightVideo = StringUtil.parseInt(getArgAfter("--height-video", "1080"));
		preset = getArgAfter("--video-preset", "ultrafast");
	}

	private void showHelp() {
		System.out.print("StreamSite\n");
		System.out.print("Usage: streamsite <url> <rtmp/file> [options]\n\n");
		System.out.print("\t<url>\tURL of webpage to load\n");
		System.out.print("\t<rtmp>\tRTMP ingest server or path to output file\n\n");
		System.out.print("Options:\n");
		System.out.print("\t-h, --help\tShow help text\n");
		System.out.print("\t--width-website <pixels>\tSet the width of website viewport\n");
		System.out.print("\t--height-website <pixels>\tSet the height of website viewport\n");
		System.out.print("\t--log\tLog level\tValues: SEVERE WARNING INFO CONFIG FINE FINER FINEST\n");
		System.out.print("\t--fps-in <fps>\tScreenshots per second\n");
		System.out.print("\t--dir <path>\tWorking directory\n");
		System.out.print("\t--reload\tReload page after every render\n");
		System.out.print("\t--background <color>\tSet HTML body background color\n");
		System.out.print("\t--fps-out <fps>\tSet fps of stream\n");
		System.out.print("\t--keyint <keyint>\tSet keyint\n");
		System.out.print("\t--audio-bitrate <bitrate>\tSet audio bitrate\n");
		System.out.print("\t--video-bitrate <bitrate>\tSet video bitrate\n");
		System.out.print("\t--width-video <pixels>\tSet width of output video\n");
		System.out.print("\t--height-video <pixels>\tSet height of output video\n");
		System.out.print("\t--video-preset <preset>\tVideo preset\n");
		System.exit(1);
	}

	private boolean hasOption(String... options) {
		for (String opt : options)
			if (argsList.contains(opt))
				return true;
		return false;
	}

	private String getArgAfter(String arg, String defaultValue) {
		if (!argsList.contains(arg)
				|| argsList.indexOf(arg) + 1 >= argsList.size()
				|| argsList.get(argsList.indexOf(arg) + 1).startsWith("-"))
			return defaultValue;
		return argsList.get(argsList.indexOf(arg) + 1);
	}
}
