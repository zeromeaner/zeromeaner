package org.zeromeaner.plugin.videorecording;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.zeromeaner.gui.reskin.StandaloneGamePanel;
import org.zeromeaner.gui.reskin.StandaloneGamePanel.FramePerSecond;
import org.zeromeaner.gui.reskin.StandaloneGamePanel.Hook;
import org.zeromeaner.util.Options;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IRational;

public class StandaloneGamePanelHook implements Hook {
	private static final Logger log = Logger.getLogger(StandaloneGamePanelHook.class);

	private DelayQueue<FramePerSecond> videoFrames = new DelayQueue<>();

	public IMediaWriter videoWriter;
	
	public File videoFile;

	private long videoStart;
	
	public int videoFPS;

	protected BufferedImage videoBuffer;

	@Override
	public void gameStarted(StandaloneGamePanel thiz) {
		videoBuffer = new BufferedImage(640, 480, BufferedImage.TYPE_3BYTE_BGR);
		videoFPS = Options.standalone().VIDEO_FPS.value();

		if(Options.standalone().RECORD_VIDEO.value()) {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss");
			try {
				videoFile = new File(System.getProperty("user.dir"), "video/" + df.format(System.currentTimeMillis()) + ".mpg");
				videoFile.getParentFile().mkdirs();
				videoWriter = ToolFactory.makeWriter(videoFile.getCanonicalPath());
				videoWriter.addVideoStream(0, 0, ICodec.findEncodingCodec(ICodec.ID.CODEC_ID_MPEG1VIDEO), IRational.make((double) videoFPS), thiz.gameBuffer.getWidth(), thiz.gameBuffer.getHeight());
				videoStart = System.currentTimeMillis();
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
			if(videoFrames.size() < videoFPS) {
				if(videoWriter != null) {
					videoBuffer.getGraphics().drawImage(thiz.gameBuffer, 0, 0, null);
					videoWriter.encodeVideo(0, videoBuffer, System.currentTimeMillis() - videoStart, TimeUnit.MILLISECONDS);
				}
				videoFrames.add(new FramePerSecond());
			}
		}
	}

	@Override
	public void gameStopped(StandaloneGamePanel thiz) {
		if(videoWriter != null) {
			synchronized(videoBuffer) {
				videoWriter.close();
				log.info("Finished recording video to " + videoFile);
				videoWriter = null;
			}
		}
	}

}
