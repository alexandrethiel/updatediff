package net.srcz.updatediff.splitters;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import net.srcz.updatediff.Splitter;
import net.srcz.updatediff.Utils;

public class GZipSplitter implements Splitter {

	@Override
	public boolean canUnpack(File file) {
		try {
			if(!file.getName().endsWith(".gz"))
				return false;
			DataInputStream dis = new DataInputStream(new FileInputStream(file));
			boolean ok = dis.read() == 0x1F;
			ok &= dis.read() == 0x8B;
			dis.close();
			return ok;
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	@Override
	public String getId() {
		return "gzip";
	}

	@Override
	public void repack(String id, File folder, File outputFile) {
		try {
			GZIPOutputStream gzos = new GZIPOutputStream(new FileOutputStream(outputFile));
			File f = folder.listFiles()[0];
			FileInputStream fis = new FileInputStream(f);
			Utils.writeToStream(fis, gzos);
			fis.close();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
	}

	@Override
	public void unpackToFolder(File file, File folder) {
		folder.mkdir();
		try {
			String fileName = file.getName().substring(0, file.getName().length() -3);
			GZIPInputStream gzis2 = new GZIPInputStream(new FileInputStream(file));
			Utils.writeToFile(gzis2, new File(folder,fileName));
			gzis2.close();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

}
