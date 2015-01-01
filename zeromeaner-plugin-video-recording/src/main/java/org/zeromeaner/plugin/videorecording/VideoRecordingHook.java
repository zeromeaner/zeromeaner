package org.zeromeaner.plugin.videorecording;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.zeromeaner.gui.reskin.StandaloneGamePanel;
import org.zeromeaner.gui.reskin.StandaloneGamePanel.Hook;
import org.zeromeaner.util.Options;
import org.zeromeaner.util.Session;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IRational;

public class VideoRecordingHook implements Hook {
	private static final Logger log = Logger.getLogger(VideoRecordingHook.class);

	private IMediaWriter videoWriter;
	
	private File videoFile;

	private long videoStart;
	
	private long nextFramePicos;
	
	private long frameStepPicos;
	
	private int videoFPS;

	private int streamIdx;
	
	private ExecutorService encodePool = Executors.newSingleThreadExecutor();
	
	private Map.Entry<Long, BufferedImage> lastFrame;
	private TreeMap<Long, BufferedImage> frames = new TreeMap<>();
	
	private Runnable encodeTask = new Runnable() {
		@Override
		public void run() {
			synchronized(frames) {
				if(frames.size() == 0) {
					frames.notify();
					return;
				}
				Map.Entry<Long, BufferedImage> e = frames.firstEntry();
				if(videoWriter != null) {
					frames.remove(e.getKey());
					if(e.getKey() > (nextFramePicos - frameStepPicos) / 1000) {
						videoWriter.encodeVideo(streamIdx, e.getValue(), nextFramePicos / 1000, TimeUnit.NANOSECONDS);
						nextFramePicos += frameStepPicos;
						lastFrame = e;
					}
				}
				encodePool.execute(this);
			}
		}
	};
	
	@Override
	public void gameStarted(StandaloneGamePanel thiz) {
		synchronized(frames) {
			if(VideoRecordingOptions.get().ENABLED.value()) {
				videoFPS = VideoRecordingOptions.get().FPS.value();
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss");
				try {
					videoFile = new File(
							System.getProperty("user.dir"), 
							"video/" + Session.getUser() + " " + df.format(System.currentTimeMillis()) + ".mpg");
					videoFile.getParentFile().mkdirs();
					videoWriter = ToolFactory.makeWriter(videoFile.getCanonicalPath());
					streamIdx = videoWriter.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG2VIDEO, IRational.make(videoFPS), thiz.gameBuffer.getWidth(), thiz.gameBuffer.getHeight());
					videoStart = System.nanoTime();
					frameStepPicos = 1000000000000L / videoFPS;
					nextFramePicos = 0;
					log.info("Recording video to " + videoFile);
				} catch (IOException e) {
					e.printStackTrace();
					videoWriter = null;
				}
			} else
				videoWriter = null;
		}
	}

	@Override
	public void frameSynced(StandaloneGamePanel thiz) {
		synchronized(frames) {
			if(videoWriter != null) {
				BufferedImage frame = new BufferedImage(thiz.gameBuffer.getWidth(), thiz.gameBuffer.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
				frame.getGraphics().drawImage(thiz.gameBuffer, 0, 0, null);
				frames.put(System.nanoTime() - videoStart, frame);
				encodePool.execute(encodeTask);
			}
		}
	}

	@Override
	public void gameStopped(StandaloneGamePanel thiz) {
		synchronized(frames) {
			if(videoWriter != null) {
				log.info("Waiting for final video encode tasks to complete");
				while(frames.size() > 0) {
					try {
						frames.wait();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
				
				IMediaWriter vw = videoWriter;
				videoWriter = null;
				vw.flush();
				vw.close();
				log.info("Finished recording video to " + videoFile);
			}
		}
	}

}
