package net.srcz.updatediff.splitters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import net.srcz.updatediff.Splitter;
import net.srcz.updatediff.Utils;

//http://android-dls.com/wiki/index.php?title=HOWTO:_Unpack%2C_Edit%2C_and_Re-Pack_Boot_Images
//http://docs.blackfin.uclinux.org/doku.php?id=bootloaders:u-boot:uimage
public class KernelImageSplitter implements Splitter {
	public static final int[] MAGIC = { 0x00, 0x00, 0xA0, 0xE1 };
	public static final int MAGIC_COUNT = 8;
	
	@Override
	public boolean canUnpack(File file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			for(int i=0; i<MAGIC_COUNT; i++) {
				for(int j=0; j<MAGIC.length; j++) {
					int val = fis.read();
					if(val != MAGIC[j]) {
						fis.close();
						return false;
					}
						
				}
			}
			//System.out.println("found kernel image");
			fis.close();
			return true;
		} catch(Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return "linux_kernel_img";
	}

	@Override
	public void repack(String id, File folder, File outputFile) {
		try {
			FileOutputStream fos = new FileOutputStream(outputFile);
			FileInputStream fisHeader = new FileInputStream(new File(folder,"bootloader"));
			FileInputStream fisGzip = new FileInputStream(new File(folder,"linuxkernel.gz"));
			Utils.writeToStream(fisHeader, fos);
			Utils.writeToStream(fisGzip, fos);
			fos.close();
			fisGzip.close();
			fisHeader.close();
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void unpackToFolder(File file, File folder) {
		try {
			folder.mkdir();
			int[] gzipHeader = { 0x00, 0x00, 0x00, 0x00, 0x1F, 0x8B };
			FileInputStream fis = new FileInputStream(file);
			FileOutputStream fosHeader = new FileOutputStream(new File(folder,"bootloader"));
			FileOutputStream fosGZip = new FileOutputStream(new File(folder,"linuxkernel.gz"));
			int nbFound = 0;
			while(true) {
				int val = fis.read();
				if(val == -1)
					break;
				if(val == gzipHeader[nbFound]) {
					nbFound++;
					if(nbFound == 6)
						break;
					continue;
				} else {
					if(nbFound != 0) {
						for(int i=0; i<nbFound; i++)
							fosHeader.write(gzipHeader[i]);
						nbFound = 0;
					}
					fosHeader.write(val);
				}
			}
			fosGZip.write(0x1F);
			fosGZip.write(0x8B);
			byte[] buffer = new byte[1024];
			while(true) {
				int nb = fis.read(buffer);
				if(nb == -1)
					break;
				fosGZip.write(buffer,0,nb);
			}
			fosGZip.close();
			fosHeader.close();
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public static void main(String[] args) {
		try {
			File f = new File("vmlinux_extract");
			f.mkdir();
			new KernelImageSplitter().unpackToFolder(new File("vmlinux"), f);
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
