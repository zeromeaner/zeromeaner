package org.zeromeaner.game.evil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.zeromeaner.gui.slick.NullpoMinoSlick;
import org.zeromeaner.gui.swing.NullpoMinoSwing;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class TNMain {
	private static final Properties properties = new Properties();
	public static Properties getProperties() {
		return properties;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			innerMain(args);
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	private static void innerMain(String[] args) throws Exception {
		TNProperties.load();
		TNProperties.setRunExternal(true);
		TNProperties.args(args);
		File nullpoDir = TNProperties.getNullpoDirectory();
		if(nullpoDir == null || !isNullpoDirectory(nullpoDir))
			nullpoDir = selectNullpoDirectory();
		if(nullpoDir == null) {
			return;
		}

		TNProperties.setNullpoDirectory(nullpoDir);
		modifyModeList();
//		modifyRandomizerList();
		modifyRecommendedRules(":EVILINE");
		modifyRecommendedRules(":NET-EVILINE-VS-BATTLE");
		modifyNetlobbyMultimode();
		runNullpoMino();
		TNProperties.store();
	}

	private static final String TNMODE = "org.eviline.nullpo.TNMode";
	private static final String TNVSMODE = "org.eviline.nullpo.TNNetVSBattleMode";
	private static void modifyModeList() throws IOException {
		boolean tnmode = true;
		boolean tnvsmode = true;
		BufferedReader reader = new BufferedReader(new FileReader(TNProperties.getModeList()));
		try {
			for(String line = reader.readLine(); line != null; line = reader.readLine()) {
				if(TNMODE.equals(line))
					tnmode = false;
				if(TNVSMODE.equals(line))
					tnvsmode = false;
			}
		} finally {
			reader.close();
		}
		PrintWriter writer = new PrintWriter(new FileOutputStream(TNProperties.getModeList(), true));
		try {
			if(tnmode)
				writer.println(TNMODE);
			if(tnvsmode)
				writer.println(TNVSMODE);
		} finally {
			writer.close();
		}
	}

	private static final String TNRANDOMIZER = "org.eviline.nullpo.TNRandomizer";
	private static final String TNCRANDOMIZER = "org.eviline.nullpo.TNConcurrentRandomizer";
	private static void modifyRandomizerList() throws IOException {
		boolean tnrand = true;
		boolean tncrand = true;
		BufferedReader reader = new BufferedReader(new FileReader(TNProperties.getRandomizerList()));
		try {
			for(String line = reader.readLine(); line != null; line = reader.readLine()) {
				if(TNRANDOMIZER.equals(line))
					tnrand = false;
				if(TNCRANDOMIZER.equals(line))
					tncrand = false;
			}
		} finally {
			reader.close();
		}
		PrintWriter writer = new PrintWriter(new FileOutputStream(TNProperties.getRandomizerList(), true));
		try {
			if(tnrand)
				writer.println(TNRANDOMIZER);
			if(tncrand)
				writer.println(TNCRANDOMIZER);
		} finally {
			writer.close();
		}
	}

	private static final String TNRULE = "config/rule/TetrevilRandomizer.rul";
	private static final String TNCRULE = "config/rule/TetrevilConcurrentRandomizer.rul";
	private static final String TNSRULE = "config/rule/TetrevilSadisticRandomizer.rul";
	private static final String TNARULE = "config/rule/TetrevilAggressiveRandomizer.rul";
	private static final String TNCARULE = "config/rule/TetrevilConcurrentAggressiveRandomizer.rul";
	private static final String TNANGELRULE = "config/rule/TetrevilAngelRandomizer.rul";
	private static final String TNCANGELRULE = "config/rule/TetrevilConcurrentAngelRandomizer.rul";
	private static final String TNBRULE = "config/rule/TetrevilBipolarRandomizer.rul";
	private static final String TNCBRULE = "config/rule/TetrevilConcurrentBipolarRandomizer.rul";
	
	private static void modifyRecommendedRules(String ruleSection) throws IOException {
		boolean tnr = true;
		boolean tncr = true;
		boolean tnsr = true;
		boolean tnar = true;
		boolean tncar = true;
		boolean tnangelr = true;
		boolean tncangelr = true;
		boolean tnbr = true;
		boolean tncbr = true;
		BufferedReader reader = new BufferedReader(new FileReader(TNProperties.getRecommendedRules()));
		try {
			boolean tnrs = false;
			for(String line = reader.readLine(); line != null; line = reader.readLine()) {
				if(ruleSection.equals(line)) {
					tnrs = true;
				} else if(line.startsWith(":"))
					tnrs = false;
				if(tnrs) {
					if(TNRULE.equals(line))
						tnr = false;
					if(TNCRULE.equals(line))
						tncr = false;
					if(TNSRULE.equals(line))
						tnsr = false;
					if(TNARULE.equals(line))
						tnar = false;
					if(TNCARULE.equals(line))
						tncar = false;
					if(TNANGELRULE.equals(line))
						tnangelr = false;
					if(TNCANGELRULE.equals(line))
						tncangelr = false;
					if(TNBRULE.equals(line))
						tnbr = false;
					if(TNCBRULE.equals(line))
						tncbr = false;
				}
			}
		} finally {
			reader.close();
		}
		if(tnr || tncr || tnsr || tnar || tncar || tnangelr || tncangelr || tnbr || tncbr) {
			PrintWriter writer = new PrintWriter(new FileOutputStream(TNProperties.getRecommendedRules(), true));
			try {
				writer.println(ruleSection);
				if(tnar)
					writer.println(TNARULE);
				if(tnr)
					writer.println(TNRULE);
				if(tncr)
					writer.println(TNCRULE);
				if(tnsr)
					writer.println(TNSRULE);
				if(tncar)
					writer.println(TNCARULE);
				if(tnangelr)
					writer.println(TNANGELRULE);
				if(tncangelr)
					writer.println(TNCANGELRULE);
				if(tnbr)
					writer.println(TNBRULE);
				if(tncbr)
					writer.println(TNCBRULE);
			} finally {
				writer.flush();
				writer.close();
			}
		}

		File f;
		PrintWriter writer = new PrintWriter(f = new File(TNProperties.getNullpoDirectory(), TNRULE));
		try {
			writer.println("0.ruleopt.strRuleName=EVILINE EVIL");
			writer.println("0.ruleopt.strRandomizer=org.eviline.nullpo.TNRandomizer");
		} finally {
			writer.flush();
			writer.close();
		}
//		f.deleteOnExit();

		writer = new PrintWriter(f = new File(TNProperties.getNullpoDirectory(), TNCRULE));
		try {
			writer.println("0.ruleopt.strRuleName=EVILINE FAST EVIL");
			writer.println("0.ruleopt.strRandomizer=org.eviline.nullpo.TNConcurrentRandomizer");
		} finally {
			writer.flush();
			writer.close();
		}
//		f.deleteOnExit();

//		writer = new PrintWriter(f = new File(TNProperties.getNullpoDirectory(), TNSRULE));
//		try {
//			writer.println("0.ruleopt.strRuleName=TETREVIL SADISTIC");
//			writer.println("0.ruleopt.strRandomizer=org.eviline.nullpo.TNSadisticRandomizer");
//		} finally {
//			writer.flush();
//			writer.close();
//		}
//		f.deleteOnExit();

		writer = new PrintWriter(f = new File(TNProperties.getNullpoDirectory(), TNARULE));
		try {
			writer.println("0.ruleopt.strRuleName=EVILINE AGGRESSIVE");
			writer.println("0.ruleopt.strRandomizer=org.eviline.nullpo.TNAggressiveRandomizer");
		} finally {
			writer.flush();
			writer.close();
		}

		writer = new PrintWriter(f = new File(TNProperties.getNullpoDirectory(), TNCARULE));
		try {
			writer.println("0.ruleopt.strRuleName=EVILINE FAST AGGRESSIVE");
			writer.println("0.ruleopt.strRandomizer=org.eviline.nullpo.TNConcurrentAggressiveRandomizer");
		} finally {
			writer.flush();
			writer.close();
		}

		writer = new PrintWriter(f = new File(TNProperties.getNullpoDirectory(), TNANGELRULE));
		try {
			writer.println("0.ruleopt.strRuleName=EVILINE ANGELIC");
			writer.println("0.ruleopt.strRandomizer=org.eviline.nullpo.TNAngelRandomizer");
		} finally {
			writer.flush();
			writer.close();
		}

		writer = new PrintWriter(f = new File(TNProperties.getNullpoDirectory(), TNCANGELRULE));
		try {
			writer.println("0.ruleopt.strRuleName=EVILINE FAST ANGELIC");
			writer.println("0.ruleopt.strRandomizer=org.eviline.nullpo.TNConcurrentAngelRandomizer");
		} finally {
			writer.flush();
			writer.close();
		}

		writer = new PrintWriter(f = new File(TNProperties.getNullpoDirectory(), TNBRULE));
		try {
			writer.println("0.ruleopt.strRuleName=EVILINE BIPOLAR");
			writer.println("0.ruleopt.strRandomizer=org.eviline.nullpo.TNBipolarRandomizer");
		} finally {
			writer.flush();
			writer.close();
		}

		writer = new PrintWriter(f = new File(TNProperties.getNullpoDirectory(), TNCBRULE));
		try {
			writer.println("0.ruleopt.strRuleName=EVILINE FAST BIPOLAR");
			writer.println("0.ruleopt.strRandomizer=org.eviline.nullpo.TNConcurrentBipolarRandomizer");
		} finally {
			writer.flush();
			writer.close();
		}
	}

	private static final String TNVBMULTIMODE = "NET-EVILINE-VS-BATTLE,false";
	private static void modifyNetlobbyMultimode() throws IOException {
		boolean tnvb = true;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(TNProperties.getNetlobbyMultimode()));
			try {
				for(String line = reader.readLine(); line != null; line = reader.readLine()) {
					if(TNVBMULTIMODE.equals(line))
						tnvb = false;
				}
			} finally {
				reader.close();
			}
		} catch(FileNotFoundException e) {
		}
		if(!tnvb)
			return;
		PrintWriter writer = new PrintWriter(new FileOutputStream(TNProperties.getNetlobbyMultimode(), true));
		try {
			writer.println(":TETROMINO");
			writer.println(TNVBMULTIMODE);
		} finally {
			writer.flush();
			writer.close();
		}
	}
	
	private static void runNullpoMino() throws Exception {
		if(!TNProperties.isRunExternal()) {
			runInternalNullpoMino();
			return;
		}
			
		
//		if(TNMain.class.getClassLoader() instanceof URLClassLoader) {
//			try {
//				URLClassLoader cl = (URLClassLoader) TNMain.class.getClassLoader();
//				Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
//				addURL.setAccessible(true);
//				addURL.invoke(cl, new File(TNProperties.getNullpoDirectory(), "NullpoMino.jar").toURI().toURL());
//				addURL.invoke(cl, new File(TNProperties.getNullpoDirectory(), "lib/log4j-1.2.15.jar").toURI().toURL());
//				runInternalNullpoMino();
//				return;
//			} catch(Throwable t) {
//			}
//		}
		
		runExternalNullpoMino();
	}

	private static void runInternalNullpoMino() throws Exception {
//		NullpoMinoSwing.main(new String[0]);
//		NullpoMinoSlick.main(new String[0]);
		// The following bit of reflection is so that maven can compile this without adding
		// slick as a dependency.
		Class<?> slickmainclass = Class.forName("org.zeromeaner.gui.slick.NullpoMinoSlick");
		slickmainclass.getMethod("main", String[].class).invoke(null, new Object[] {new String[0]});
	}

	private static void runExternalNullpoMino() throws IOException, InterruptedException {
		/*
		 * java 
		 * -cp bin;NullpoMino.jar;lib\log4j-1.2.15.jar 
		 * -Dsun.java2d.translaccel=true 
		 * -Dsun.java2d.d3dtexbpp=16 
		 * org.zeromeaner.gui.swing.NullpoMinoSwing
		 */

		List<String> command = new ArrayList<String>();
		command.add("java");


		StringBuilder classpath = new StringBuilder("bin");
		String psep = File.pathSeparator;
		String fsep = File.separator;
		classpath.append(psep + "NullpoMino.jar");
//		classpath.append(psep + "lib" + fsep + "log4j-1.2.15.jar");
		for(File f : new File(TNProperties.getNullpoDirectory(), "lib").listFiles()) {
			if(f.getName().toUpperCase().endsWith(".JAR") && (f.getName().contains("log4j") || !f.getName().contains("-")))
				classpath.append(psep + "lib" + fsep + f.getName());
		}
		classpath.append(psep + getTetrevilClasspath().getAbsolutePath());

		command.add("-cp"); command.add(classpath.toString());
		
		command.add("-Djava.library.path=" + new File(TNProperties.getNullpoDirectory(), "lib").getAbsolutePath());
		
//		command.add("-Dsun.java2d.translaccel=true");
//		command.add("-Dsun.java2d.d3dtexbpp=16");
		
		command.add("org.zeromeaner.gui.slick.NullpoMinoSlick");

		ProcessBuilder pb = new ProcessBuilder(command);
		pb.directory(TNProperties.getNullpoDirectory());

		final Process p = pb.start();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
					for(String line = reader.readLine(); line != null; line = reader.readLine())
						System.out.println(line);
				} catch(IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}).start();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
					for(String line = reader.readLine(); line != null; line = reader.readLine())
						System.err.println(line);
				} catch(IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}).start();
		p.waitFor();
	}

	private static final Pattern JAR_RESOURCE = Pattern.compile("jar:file:(.*)!(.*?)");
	private static final Pattern FILE_RESOURCE = Pattern.compile("file:(.*)/org/eviline/nullpo/TNMain.class");
	private static final Pattern URL_RESOURCE = Pattern.compile("jar:(.*)!(.*?)");
	private static File getTetrevilClasspath() {
		String main = TNMain.class.getResource(TNMain.class.getSimpleName() + ".class").toString();
		Matcher m = JAR_RESOURCE.matcher(main);
		if(m.matches())
			return new File(m.group(1));

		m = FILE_RESOURCE.matcher(main);
		if(m.matches())
			return new File(m.group(1));

		m = URL_RESOURCE.matcher(main);
		if(m.matches()) {
			try {
				URL url = new URL(m.group(1));
				InputStream in = url.openStream();
				File tmp = File.createTempFile("eviline_nullpo", ".jar");
				tmp.deleteOnExit();
				FileOutputStream fout = new FileOutputStream(tmp);
				byte[] buf = new byte[8192];
				for(int r = in.read(buf); r != -1; r = in.read(buf)) {
					fout.write(buf, 0, r);
				}
				in.close();
				fout.close();
				return tmp;
			} catch(Exception ex) {
				return null;
			}
		}

		return null;
	}

	private static boolean isNullpoDirectory(File dir) {
		File mode = new File(dir, "config/list/mode.lst");
		File randomzier = new File(dir, "config/list/randomizer.lst");
		return mode.exists() && randomzier.exists();
	}

	private static File selectNullpoDirectory() {
		JFileChooser chooser = new JFileChooser();
		//		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setDialogTitle("Choose NullpoMino install directory");
		do {
			int c = chooser.showDialog(null, "Choose");
			if(c != JFileChooser.APPROVE_OPTION)
				return null;
			File f = chooser.getSelectedFile();
			if(isNullpoDirectory(f))
				return f;
			if(!f.exists() && (f = new File(f.getParentFile(), f.getName() + ".app")).exists()) {
				f = new File(f, "Contents/Resources/Java");
				if(isNullpoDirectory(f))
					return f;
			}
			JOptionPane.showMessageDialog(null, "Chosen directory " + chooser.getSelectedFile() + " is not a NullpoMino directory", "Not NullpoMino", JOptionPane.ERROR_MESSAGE);
		} while(true);
	}

}
