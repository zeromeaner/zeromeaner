package org.zeromeaner.standalone;

import java.io.File;
import java.util.concurrent.Callable;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import org.zeromeaner.util.io.FileSystemViewHook;
import org.zeromeaner.util.io.PrioritizedHandler;

public class StandaloneFileSystemViewHook implements FileSystemViewHook {

	@Override
	public void addFileSystemView(String limit, PrioritizedHandler<Callable<FileSystemView>> handlers) {
		Callable<FileSystemView> handler = new Callable<FileSystemView>() {
			@Override
			public FileSystemView call() throws Exception {
				return FileSystemView.getFileSystemView();
			}
		};
		handlers.add(0, handler);
	}

	@Override
	public void addFileChooser(final String path, PrioritizedHandler<Callable<JFileChooser>> handlers) {
		Callable<JFileChooser> handler = new Callable<JFileChooser>() {
			@Override
			public JFileChooser call() throws Exception {
				return new JFileChooser(new File(System.getProperty("user.dir"), path), FileSystemView.getFileSystemView());
			}
		};
		handlers.add(0, handler);
	}

}
