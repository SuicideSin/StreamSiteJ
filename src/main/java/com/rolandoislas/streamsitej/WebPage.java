package com.rolandoislas.streamsitej;

import com.rolandoislas.streamsitej.util.Logger;
import io.webfolder.ui4j.api.browser.BrowserEngine;
import io.webfolder.ui4j.api.browser.BrowserFactory;
import io.webfolder.ui4j.api.browser.Page;
import javafx.animation.AnimationTimer;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.web.WebView;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;

public class WebPage {

    private final BrowserEngine browser;
    private Page page;

    public WebPage() {
        browser = BrowserFactory.getWebKit();
    }

    public void writeScreenshot(@Nullable FileOutputStream fifoOutputStream) {
        if (page == null && !connect())
            return;
        if (fifoOutputStream != null) {
            final boolean[] done = {false};
            final AnimationTimer timer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    WebView view = (WebView) page.getView();
                    WritableImage snapshot = view.snapshot(new SnapshotParameters(), null);
                    BufferedImage image = SwingFXUtils.fromFXImage(snapshot, null);
                    try {
                        ImageIO.write(image, "png", fifoOutputStream);
                    }
                    catch (IOException e) {
                        Logger.exception(e);
                    }
                    done[0] = true;
                }
            };
            timer.start();
            while (!done[0])
                continue;
        }
    }

    public boolean connect() {
        page = null;
        try {
            page = browser.navigate(StreamSite.args.url);
            page.show(true);
            return page != null;
        }
        catch (Exception e) {
            return false;
        }
    }
}
