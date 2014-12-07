package org.zeromeaner.util.io;

import java.util.concurrent.Callable;

import javax.swing.filechooser.FileSystemView;

public class DavFileSystemViewHook implements FileSystemViewHook {

	@Override
	public void addFileSystemView(final String limit, PrioritizedHandler<Callable<FileSystemView>> handlers) {
		Callable<FileSystemView> handler = new Callable<FileSystemView>() {
			@Override
			public FileSystemView call() throws Exception {
				return new DavFileSystemView(limit);
			}
		};
		handlers.add(0, handler);
	}

}
