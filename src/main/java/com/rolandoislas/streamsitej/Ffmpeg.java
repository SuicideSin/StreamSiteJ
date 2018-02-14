package com.rolandoislas.streamsitej;

import com.rolandoislas.streamsitej.util.Logger;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Ffmpeg {

    private File fifo = null;
    private FileOutputStream fifoOutputStream;
    private Thread ffmpegThread;

    public Ffmpeg() {
        try {
            fifo = File.createTempFile("streamsite", "");
        } catch (IOException e) {
            error("Failed to create temp file", e);
        }
        if (!fifo.delete())
            error("Failed to create temp file", null);
        try {
            Process mkfifo = Runtime.getRuntime().exec(new String[]{"mkfifo", fifo.getAbsolutePath()});
            if (mkfifo.waitFor() != 0)
                error("Failed to create temp file", new Exception(IOUtils.toString(mkfifo.getErrorStream(),
                        StandardCharsets.UTF_8)));
        }
        catch (IOException | InterruptedException e) {
            error("Failed to create temp file", e);
        }
    }

    /**
     * Error if there is no ffmpeg found
     */
    private void error(String message, @Nullable Exception e) {
        Logger.warn(message);
        if (e != null)
            Logger.exception(e);
        System.exit(1);
    }

    public void start() {
        // Find FFmpeg
        String ffmpegPath = null;
        try {
            Process ffmpegWhich = Runtime.getRuntime().exec("which ffmpeg");
            ffmpegWhich.waitFor();
            ffmpegPath = IOUtils.toString(ffmpegWhich.getInputStream(), StandardCharsets.UTF_8).trim()
                    .replace("\n", "").replace("\r", "");
        }
        catch (IOException | InterruptedException e) {
            error("Failed to find ffmpeg.", e);
        }
        if (ffmpegPath == null || ffmpegPath.isEmpty())
            error("Failed to find ffmpeg.", null);
        assert ffmpegPath != null;
        FFmpeg ffmpeg = null;
        try {
            ffmpeg = new FFmpeg(ffmpegPath);
        }
        catch (IOException e) {
            error("Failed to find ffmpeg.", e);
        }
        assert ffmpeg != null;
        // Find FFprobe
        String ffprobePath = null;
        try {
            Process ffprobeWhich = Runtime.getRuntime().exec("which ffprobe");
            ffprobeWhich.waitFor();
            ffprobePath = IOUtils.toString(ffprobeWhich.getInputStream(), StandardCharsets.UTF_8).trim()
                    .replace("\n", "").replace("\r", "");
        }
        catch (IOException | InterruptedException e) {
            error("Failed to find ffprobe", e);
        }
        if (ffprobePath == null || ffprobePath.isEmpty())
            error("Failed to find ffprobe", null);
        assert ffprobePath != null;
        FFprobe ffprobe = null;
        try {
            ffprobe = new FFprobe(ffprobePath);
        }
        catch (IOException e) {
            error("Failed to find ffproble", e);
        }
        assert ffprobe != null;
        // Probe FIFO
        /*FFmpegProbeResult probeResult = null;
        try {
            probeResult = ffprobe.probe(fifo.getAbsolutePath());
        }
        catch (IOException e) {
            error("Failed to probe", e);
        }
        assert probeResult != null;*/
        // Populate build
        FFmpegBuilder builder = new FFmpegBuilder()
                .addExtraArgs(
                        "-re",
                        "-loop", "1",
                        "-f", "image2pipe",
                        "-framerate", String.valueOf(StreamSite.args.fpsIn)
                )
                .setInput(fifo.getAbsolutePath())
                .overrideOutputFiles(true)
                .addOutput(StreamSite.args.rtmp)
                .setFormat("flv")
                .disableSubtitle()
                .disableAudio()
                .setVideoCodec("libx264")
                .setVideoFrameRate(StreamSite.args.fpsOut, 1)
                .setVideoResolution(StreamSite.args.widthVideo, StreamSite.args.heightVideo)
                .setVideoPixelFormat("yuv420p")
                .addExtraArgs(
                        "-movflags", "+faststart",
                        "-x264-params", String.format("keyint=%d:no-scenecut=1", StreamSite.args.keyInt),
                        "-preset", StreamSite.args.preset
                )
                //.setDuration(48, TimeUnit.HOURS)
                .done();
        // Create job
        FFmpegExecutor executor = null;
        try {
            executor = new FFmpegExecutor(ffmpeg);
        } catch (IOException e) {
            error("Failed to launch ffmpeg.", e);
        }
        assert executor != null;
        FFmpegJob ffmpegJob = executor.createJob(builder, new ProgressListener() {
            @Override
            public void progress(Progress progress) {
                Logger.info(progress.toString());
            }
        });
        ffmpegThread = new Thread(() -> {
            Thread ffmpegJobThread = new Thread(ffmpegJob);
            ffmpegJobThread.setDaemon(true);
            ffmpegJobThread.setName("Ffmpeg job");
            ffmpegJobThread.start();
            while (true) {
                if (!ffmpegJobThread.isAlive()) {
                    Logger.warn("Ffmpeg died. Exiting");
                    System.exit(1);
                }
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                    Logger.exception(e);
                }
            }
        });
        ffmpegThread.setDaemon(true);
        ffmpegThread.setName("Ffmpeg");
        ffmpegThread.start();
    }

    public boolean isAlive() {
        return ffmpegThread != null && ffmpegThread.isAlive();
    }

    @Nullable
    public FileOutputStream getFifoOutputStream() {
        try {
            if (fifoOutputStream == null)
                fifoOutputStream = new FileOutputStream(fifo);
        }
        catch (IOException e) {
            Logger.exception(e);
        }
        return fifoOutputStream;
    }
}
