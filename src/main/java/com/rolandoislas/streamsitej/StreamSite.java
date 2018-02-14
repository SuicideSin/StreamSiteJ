package com.rolandoislas.streamsitej;

import com.rolandoislas.streamsitej.data.ArgumentParser;
import com.rolandoislas.streamsitej.util.Logger;

import java.util.logging.Level;

public class StreamSite {
    public static ArgumentParser args;

    public static void main(String[] args) {
        StreamSite.args = new ArgumentParser(args);
        // Log level
        Logger.setLevel(Level.parse(StreamSite.args.logLevel));
        //System.setProperty("ui4j.headless", "true");
        // Start ffmpeg and screenshot
        WebPage webPage = new WebPage();
        webPage.connect();
        Ffmpeg ffmpeg = new Ffmpeg();
        ffmpeg.start();
        // Screenshot and push to fmpeg
        long lastScreenshot = 0;
        while (ffmpeg.isAlive()) {
            if (System.currentTimeMillis() - lastScreenshot >= 1000 / StreamSite.args.fpsIn) {
                webPage.writeScreenshot(ffmpeg.getFifoOutputStream());
                lastScreenshot = System.currentTimeMillis();
            }
            else {
                try {
                    Thread.sleep(1000 / StreamSite.args.fpsIn);
                } catch (InterruptedException ignore) {}
            }
        }
    }
}
