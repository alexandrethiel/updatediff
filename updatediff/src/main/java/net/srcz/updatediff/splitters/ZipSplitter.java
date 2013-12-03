package net.srcz.updatediff.splitters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import net.srcz.updatediff.Splitter;
import net.srcz.updatediff.Utils;


public class ZipSplitter implements Splitter {

	@Override
	public boolean canUnpack(File file) {
		if(file.getName().endsWith(".apk"))
			return true;
		if(file.getName().endsWith(".zip"))
			return true;
		if(file.getName().endsWith(".jar"))
			return true;
		return false;
	}

	@Override
	public String getId() {
		return "zip";
	}

	private void compressFiles(File rootFolder, File[] files, ZipOutputStream zos) throws IOException {
		for(File f : files) {
			if(f.isDirectory()) {
				File[] files2 = f.listFiles();
				compressFiles(rootFolder,files2,zos);
				continue;
			}
			String rootFolderPath = rootFolder.getCanonicalPath();
			String filePath = f.getCanonicalPath();
			filePath = filePath.substring(rootFolderPath.length(), filePath.length());
			ZipEntry z = new ZipEntry(filePath);
			z.setTime(f.lastModified());
			zos.putNextEntry(z);
			FileInputStream fis = new FileInputStream(f);
			try {
				Utils.writeToStream(fis, zos);
			} finally {
				fis.close();
			}
		}
	}
	
	@Override
	public void repack(String id, File folder, File outputFile) {
		try {
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputFile));

			File[] files = folder.listFiles();
			compressFiles(folder,files,zos);
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void unpackToFolder(File file, File folder) {
		try {
			ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
			try {
				ZipEntry ze = zis.getNextEntry();
				while (ze != null) {
					// for each entry to be extracted
					String entryName = ze.getName();
					//System.out.println("entryname " + entryName);
	
					File newFile = new File(folder, entryName);
					if (ze.isDirectory())
						newFile.mkdir();
					else {
						newFile.getParentFile().mkdirs();
						Utils.writeToFile(zis, newFile);
					}
					zis.closeEntry();
					ze = zis.getNextEntry();
				}
			} finally {
				zis.close();
			}
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}
