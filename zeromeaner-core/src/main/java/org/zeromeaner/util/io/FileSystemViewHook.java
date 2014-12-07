package org.zeromeaner.util.io;

import java.util.concurrent.Callable;

import javax.swing.filechooser.FileSystemView;

public interface FileSystemViewHook {
	public void addFileSystemView(PrioritizedHandler<Callable<FileSystemView>> handlers);
}
