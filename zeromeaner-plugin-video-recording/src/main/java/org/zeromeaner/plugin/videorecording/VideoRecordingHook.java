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

	private ReentrantLock lock = new ReentrantLock();
	
	private IMediaWriter videoWriter;
	
	private File videoFile;

	private long videoStart;
	
	private long nextFramePicos;
	
	private long frameStepPicos;
	
	private int videoFPS;

	private BufferedImage videoBuffer;
	
	private int streamIdx;
	
	private ExecutorService encodePool = Executors.newSingleThreadExecutor();
	
	private Runnable encodeTask = new Runnable() {
		@Override
		public void run() {
			lock.lock();
			try {
				if(videoWriter == null)
					return;
				if(System.nanoTime() - videoStart > nextFramePicos / 1000) {
					videoWriter.encodeVideo(streamIdx, videoBuffer, nextFramePicos / 1000, TimeUnit.NANOSECONDS);
					nextFramePicos += frameStepPicos;
				}
				if(System.nanoTime() - videoStart > nextFramePicos / 1000)
					encodePool.execute(this);
			} finally {
				lock.unlock();
			}
		}
	};
	
	@Override
	public void gameStarted(StandaloneGamePanel thiz) {
		lock.lock();
		try {
			if(VideoRecordingOptions.get().ENABLED.value()) {
				videoBuffer = new BufferedImage(thiz.gameBuffer.getWidth(), thiz.gameBuffer.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
				videoFPS = VideoRecordingOptions.get().FPS.value();
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss");
				try {
					videoFile = new File(
							System.getProperty("user.dir"), 
							"video/" + Session.getUser() + " " + df.format(System.currentTimeMillis()) + ".mpg");
					videoFile.getParentFile().mkdirs();
					videoWriter = ToolFactory.makeWriter(videoFile.getCanonicalPath());
					streamIdx = videoWriter.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG2VIDEO, IRational.make(videoFPS), videoBuffer.getWidth(), videoBuffer.getHeight());
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
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void frameSynced(StandaloneGamePanel thiz) {
		lock.lock();
		try {
			if(videoWriter != null)
				videoBuffer.getGraphics().drawImage(thiz.gameBuffer, 0, 0, null);
		} finally {
			lock.unlock();
		}
		encodePool.execute(encodeTask);
	}

	@Override
	public void gameStopped(StandaloneGamePanel thiz) {
		lock.lock();
		try {
			if(videoWriter != null) {
				IMediaWriter vw = videoWriter;
				videoWriter = null;
				vw.flush();
				vw.close();
				log.info("Finished recording video to " + videoFile);
			}
		} finally {
			lock.unlock();
		}
	}

}
