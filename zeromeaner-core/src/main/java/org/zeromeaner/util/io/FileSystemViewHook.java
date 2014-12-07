package org.zeromeaner.util.io;

import java.util.concurrent.Callable;

import javax.swing.filechooser.FileSystemView;

public interface FileSystemViewHook {
	public void addFileSystemView(String limit, PrioritizedHandler<Callable<FileSystemView>> handlers);
}
