package net.srcz.updatediff;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Utils {

	public static File createTempDir(String prefix, String suffix) throws IOException {
		File tempFold = File.createTempFile(prefix, suffix);
		tempFold.delete();
		tempFold.mkdir();
		return tempFold;
	}
	
	static public boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	public static void writeToStream(InputStream is, OutputStream os) throws IOException {
		byte[] buff = new byte[1024];
		while(true) {
			int nb = is.read(buff);
			if(nb < 0)
				break;
			os.write(buff,0,nb);
		}
	}
	
	public static void writeToFile(InputStream is, File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		try {
			writeToStream(is, fos);
		} finally {
			fos.close();
		}
	}
	

	public static boolean areIdenticals(File oldFile, File newFile) {
		if(oldFile.length() != newFile.length())
			return false;
		try {
			FileInputStream fis = new FileInputStream(oldFile);
			FileInputStream fis2 = new FileInputStream(newFile);
			
			byte[] buffer1 = new byte[1024];
			byte[] buffer2 = new byte[1024];
			while(true) {
				int nb = fis.read(buffer1);
				if(nb < 0)
					break;
				fis2.read(buffer2);
				for(int i=0; i<nb; i++) {
					if(buffer1[i] != buffer2[i]);
				}
			}
			return true;
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	
	public static void copyFile(File f1, File f2) throws IOException {
		if (f1.isDirectory()) {
			f2.mkdir();
			File[] files = f1.listFiles();
			for (File f : files) {
				copyFile(f, new File(f2, f.getName()));
			}
			return;
		}

		InputStream in = new FileInputStream(f1);
		try {
			OutputStream out = new FileOutputStream(f2);
			try {
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
			} finally {
				out.close();
			}
		} finally {
			in.close();
		}
	}
}
