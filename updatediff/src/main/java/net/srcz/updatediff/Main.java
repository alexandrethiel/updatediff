package net.srcz.updatediff;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.srcz.updatediff.diff.JBDiff;
import net.srcz.updatediff.splitters.AndroidBootImgSplitter;
import net.srcz.updatediff.splitters.GZipSplitter;
import net.srcz.updatediff.splitters.ZipSplitter;

public class Main {

	Splitter[] splitters = {new ZipSplitter(), new AndroidBootImgSplitter(), new GZipSplitter(), 
			//new KernelImageSplitter()
	};
	
// http://fr.wikipedia.org/wiki/LZMA
	public Main() {
		try {
			// FileOutputStream("update-cm-4.1.99-4.2.2-diff.gdiff"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	public void performDiff(File oldFolder, File newFolder, File diffFolder,boolean display)
			throws Exception {
		File[] oldChilds = oldFolder.listFiles();
		if (oldChilds == null)
			oldChilds = new File[0];
		for (File f : oldChilds) {
			File targetFile = new File(newFolder, f.getName());
			if (f.isDirectory()) {
				if (targetFile.exists() && targetFile.isDirectory()) {
					// recurse into directories
					// perform diff on childs
					File targetDiffFolder = new File(diffFolder, f.getName());
					performDiff(f, targetFile, targetDiffFolder,true);
				} else {
					// folder removed
					//removedFiles.add(f);
				}
			} else {
				if (targetFile.exists() && targetFile.isFile()) {
					System.out.println(f.getName());
					// perform diff between files
					//diffFolder.mkdirs();
					File targetDiffFile = new File(diffFolder, f.getName()
							+ ".diff");
					smartDiff(f, targetFile, targetDiffFile);
					//System.out.println(targetDiffFile.length());
					if (targetDiffFile.length() == 0)
						targetDiffFile.delete();
				} else {
					// file has been removed
					diffFolder.mkdirs();
					File targetDiffFile = new File(diffFolder, f.getName()
							+ ".removed");
					targetDiffFile.createNewFile();
				}
			}
		}
		File[] newChilds = newFolder.listFiles();
		if (newChilds == null)
			newChilds = new File[0];

		for (File f : newChilds) {
			File targetFile = new File(oldFolder, f.getName());
			if (targetFile.exists()
					&& targetFile.isDirectory() == f.isDirectory())
				continue;
			// new file or folder : direct copy
			diffFolder.mkdir();
			File targetDiffFile = new File(diffFolder, f.getName());
			Utils.copyFile(f, targetDiffFile);
		}
	}

	public void performSplitDiff(Splitter splitter, File oldFile, File newFile, File outputFile) {
		try {
			outputFile.mkdirs();
			File tempFoldOld = Utils.createTempDir("zzz"+oldFile.getName(), "");
			try {
				File tempFoldNew = Utils.createTempDir("zzz"+newFile.getName(), "");
				try {
					splitter.unpackToFolder(oldFile, tempFoldOld);
					splitter.unpackToFolder(newFile, tempFoldNew);
					performDiff(tempFoldOld, tempFoldNew, outputFile,false);
				} finally {
					Utils.deleteDirectory(tempFoldNew);
				}
			} finally {
				Utils.deleteDirectory(tempFoldOld);
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public void smartDiff(File oldFile, File newFile, File outputFile)
			throws IOException {
		// new Delta().compute(f,targetFile,gw);
		if(Utils.areIdenticals(oldFile, newFile))
			return;
		boolean splitted = false;
		for(Splitter splitter : splitters) {
			boolean ok = splitter.canUnpack(oldFile) && splitter.canUnpack(newFile);
			if(!ok)
				continue;
			System.out.println("splitting "+oldFile.getName()+" using "+splitter.getId());
			performSplitDiff(splitter,oldFile,newFile,outputFile);
			splitted = true;
			break;
		}
		// binary diff by default
		if (!splitted) {
			outputFile.delete();
			outputFile.getParentFile().mkdirs();
			JBDiff.bsdiff(oldFile, newFile, outputFile);
			return;
		}


	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length < 3) {
			System.out.println("Parameters : inputOldFile inputNewFile diffOutputFile");
			return;
		}
		File oldFile = new File(args[0]);
		File newFile = new File(args[1]);
		File diffFile = new File(args[2]);
		try {
			// new ZipFile(diffFile);
			Main m = new Main();
			Utils.deleteDirectory(diffFile);
			m.performDiff(oldFile, newFile, diffFile,true);
			// m.gw.close();
			/*
			 * new JarDelta().computeDelta(new ZipFile(oldFile), new
			 * ZipFile(newFile), new ZipOutputStream(new
			 * FileOutputStream(diffFile)));
			 */
			System.out.println("done");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
