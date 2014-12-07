package org.zeromeaner.standalone;

import java.util.concurrent.Callable;

import javax.swing.filechooser.FileSystemView;

import org.zeromeaner.util.io.FileSystemViewHook;
import org.zeromeaner.util.io.PrioritizedHandler;

public class StandaloneFileSystemViewHook implements FileSystemViewHook {

	@Override
	public void addFileSystemView(PrioritizedHandler<Callable<FileSystemView>> handlers) {
		Callable<FileSystemView> handler = new Callable<FileSystemView>() {
			@Override
			public FileSystemView call() throws Exception {
				return FileSystemView.getFileSystemView();
			}
		};
		handlers.add(-1, handler);
	}

}
