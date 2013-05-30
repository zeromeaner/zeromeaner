package org.zeromeaner.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.filechooser.FileSystemView;

import org.zeromeaner.gui.applet.AppletMain;

import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.Factory;
import com.googlecode.sardine.Sardine;

public class ResourceFileSystemView extends FileSystemView {
	private static Sardine s;
	static {
		try {
			s = new Factory().begin("zero", "zero");
		} catch(IOException ioe) {
		}
	}
	
	protected String url() {
		return "http://" + AppletMain.url.getHost() + "/webdav/" + AppletMain.userId + "/";
	}
	
	protected String toSardine(File file) {
		return url() + file.getPath().replace(File.separator, "/").replaceAll("/+", "/").replaceAll("/+$", "");
	}
	
	protected File fromSardine(String url) {
		return new File(url.replace(url(), ""));
	}
	
	@Override
	public File createNewFolder(File containingDir) throws IOException {
		String containingUrl = toSardine(containingDir);
		s.createDirectory(containingUrl + "/NewFolder");
		return new File(containingDir, "NewFolder");
	}



	@Override
	public Boolean isTraversable(File f) {
		String u = toSardine(f);
		try {
			s.getResources(toSardine(f) + "/");
			return true;
		} catch(Exception se) {
			return false;
		}
	}



	@Override
	public boolean isParent(File folder, File file) {
		return toSardine(file).startsWith(toSardine(folder));
	}



	@Override
	public File getChild(File parent, String fileName) {
		return new File(parent, fileName);
	}



	@Override
	public boolean isFileSystem(File f) {
		return false;
	}



	@Override
	public boolean isHiddenFile(File f) {
		return false;
	}



	@Override
	public boolean isFileSystemRoot(File dir) {
		if(dir.getPath().isEmpty())
			return true;
		return false;
	}



	@Override
	public boolean isDrive(File dir) {
		return false;
	}



	@Override
	public boolean isFloppyDrive(File dir) {
		return false;
	}



	@Override
	public boolean isComputerNode(File dir) {
		return false;
	}



	@Override
	public File[] getRoots() {
		return new File[] {new File("")};
	}



	@Override
	public File getHomeDirectory() {
		return new File("");
	}



	@Override
	public File getDefaultDirectory() {
		return new File("");
	}



	@Override
	public File createFileObject(File dir, String filename) {
		return new File(dir, filename);
	}



	@Override
	public File createFileObject(String path) {
		return new File(path);
	}



	@Override
	public File[] getFiles(File dir, boolean useFileHiding) {
		List<File> ret = new ArrayList<File>();
		try {
			for(DavResource d : s.getResources(toSardine(dir))) {
				if(d.getNameDecoded().isEmpty())
					continue;
				ret.add(fromSardine(d.getBaseUrl() + d.getNameDecoded()));
			}
		} catch(Exception ex) {
		}
		return ret.toArray(new File[0]);
	}



	@Override
	public File getParentDirectory(File dir) {
		if(dir == null)
			return null;
		return dir.getParentFile();
	}


}
