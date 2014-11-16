package org.zeromeaner.plugin.videorecording;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.zeromeaner.gui.reskin.StandaloneGamePanel;
import org.zeromeaner.gui.reskin.StandaloneGamePanel.FramePerSecond;
import org.zeromeaner.gui.reskin.StandaloneGamePanel.Hook;
import org.zeromeaner.util.Options;

import EDU.oswego.cs.dl.util.concurrent.ReentrantLock;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IRational;

public class StandaloneGamePanelHook implements Hook {
	private static final Logger log = Logger.getLogger(StandaloneGamePanelHook.class);

	private DelayQueue<FramePerSecond> videoFrames = new DelayQueue<>();

	protected ReentrantLock lock = new ReentrantLock();
	
	public IMediaWriter videoWriter;
	
	public File videoFile;

	private long videoStart;
	
	private long lastFrame;
	
	public int videoFPS;

	protected BufferedImage videoBuffer;
	
	protected int streamIdx;
	
	@Override
	public void gameStarted(StandaloneGamePanel thiz) {
		videoBuffer = new BufferedImage(thiz.gameBuffer.getWidth(), thiz.gameBuffer.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		videoFPS = Options.standalone().VIDEO_FPS.value();

		if(Options.standalone().RECORD_VIDEO.value()) {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss");
			try {
				videoFile = new File(System.getProperty("user.dir"), "video/" + df.format(System.currentTimeMillis()) + ".mpg");
				videoFile.getParentFile().mkdirs();
				videoWriter = ToolFactory.makeWriter(videoFile.getCanonicalPath());
				streamIdx = videoWriter.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG2VIDEO, IRational.make(videoFPS), videoBuffer.getWidth(), videoBuffer.getHeight());
				videoStart = System.nanoTime();
				lastFrame = Long.MIN_VALUE;
				log.info("Recording video to " + videoFile);
			} catch (IOException e) {
				e.printStackTrace();
				videoWriter = null;
			}
		} else
			videoWriter = null;
	}

	@Override
	public void frameSynced(StandaloneGamePanel thiz) {
		synchronized(videoBuffer) {
			while(videoFrames.poll() != null)
				;
			if(videoWriter != null) {
				if(videoFrames.size() < videoFPS && System.nanoTime() - videoStart > lastFrame + 1000000000L / videoFPS) {
					videoBuffer.getGraphics().drawImage(
							thiz.gameBuffer, 
							0, 0, 
							null);
					videoWriter.encodeVideo(streamIdx, videoBuffer, lastFrame = (System.nanoTime() - videoStart), TimeUnit.NANOSECONDS);
					videoFrames.add(new FramePerSecond());
				}
			}
		}
	}

	@Override
	public void gameStopped(StandaloneGamePanel thiz) {
		synchronized(videoBuffer) {
			if(videoWriter != null) {
				videoWriter.close();
				log.info("Finished recording video to " + videoFile);
				videoWriter = null;
			}
		}
	}

}
