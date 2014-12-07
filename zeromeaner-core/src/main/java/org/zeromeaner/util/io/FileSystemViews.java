package org.zeromeaner.util.io;

import java.util.concurrent.Callable;

import javax.swing.filechooser.FileSystemView;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;
import org.zeromeaner.util.ServiceHookDispatcher;

public class FileSystemViews {
	private static final Logger log = Logger.getLogger(FileSystemViews.class);
	
	private static final FileSystemViews instance = new FileSystemViews();
	public static FileSystemViews get() {
		return instance;
	}
	
	private ServiceHookDispatcher<FileSystemViewHook> hook = new ServiceHookDispatcher<>(FileSystemViewHook.class);
	
	private FileSystemViews() {}
	
	public FileSystemView fileSystemView() {
		PrioritizedHandler<Callable<FileSystemView>> handlers = new PrioritizedHandler<>();
		hook.dispatcher().addFileSystemView(handlers);
		for(Callable<FileSystemView> handler : handlers.get()) {
			try {
				return handler.call();
			} catch(Exception e) {
				log.warn(e);
			}
		}
		return FileSystemView.getFileSystemView();
	}
}
