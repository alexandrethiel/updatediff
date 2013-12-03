package net.srcz.updatediff.splitters;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.srcz.updatediff.Splitter;
import net.srcz.updatediff.Utils;

public class AndroidBootImgSplitter implements Splitter {

	public static final byte[] MAGIC = "ANDROID!".getBytes();
	public static final int BOOT_NAME_SIZE = 16;
	public static final int BOOT_ARGS_SIZE = 512;

	@Override
	public boolean canUnpack(File file) {
		if (!file.getName().endsWith(".img"))
			return false;
		try {
			FileInputStream fis = new FileInputStream(file);
			try {
				byte[] buff = new byte[MAGIC.length];
				fis.read(buff);
				for (int i = 0; i < buff.length; i++) {
					if (buff[i] != MAGIC[i])
						return false;
				}
			} finally {
				fis.close();
			}
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	@Override
	public String getId() {
		return "android_boot_img";
	}

	@Override
	public void repack(String id, File folder, File outputFile) {
		String[] filesNames = { "header", "vmlinux", "ramdisk.cpio.gz",
				"second" };
		try {
			FileOutputStream fos = new FileOutputStream(outputFile);
			for (String fileName : filesNames) {
				FileInputStream fis = new FileInputStream(new File(folder,
						fileName));
				Utils.writeToStream(fis, fos);
				fis.close();
			}
			fos.close();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private void readSegment(DataInputStream is, File outputFile, int size,
			int pageSize) throws IOException {
		byte[] page = new byte[pageSize];
		FileOutputStream fos = new FileOutputStream(outputFile);
		int nPage = (size + pageSize - 1) / pageSize;
		// System.out.println("npage "+nPage);
		int byteToRead = size;
		for (int i = 0; i < nPage; i++) {
			is.readFully(page);
			// if(page[1] == 0x1F && page[0] == 0x8B)
			// System.out.println("gzip page found ");
			// revertBytes(page);
			fos.write(page, 0, Math.min(pageSize, byteToRead));
			byteToRead -= pageSize;
		}
		fos.close();
	}

	public static byte[] revertBytes(byte[] bytes) {
		byte[] bytes2 = new byte[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			bytes2[i] = bytes[bytes.length - i - 1];
		}
		return bytes2;
	}

	public static final long readUnsignedInt(InputStream is) throws IOException {
		byte[] bytes = new byte[4];
		is.read(bytes);
		long l = 0;
		l |= bytes[3] & 0xFF;
		l <<= 8;
		l |= bytes[2] & 0xFF;
		l <<= 8;
		l |= bytes[1] & 0xFF;
		l <<= 8;
		l |= bytes[0] & 0xFF;
		return l & 0x00000000ffffffff;
	}

	@Override
	public void unpackToFolder(File file, File folder) {
		try {
			DataInputStream dis = new DataInputStream(new FileInputStream(file));
			dis.skip(MAGIC.length);
			long kernelSize = readUnsignedInt(dis);
			// System.out.println("k "+kernelSize);
			dis.skip(4);
			long ramdiskSize = readUnsignedInt(dis);
			// System.out.println("R "+ramdiskSize);
			dis.skip(4);
			long secondSize = readUnsignedInt(dis);
			// System.out.println("second "+secondSize);
			dis.skip(4);
			dis.skip(4);
			int pageSize = (int) readUnsignedInt(dis);
			// System.out.println("page "+pageSize);
			/*
			 * dis.skip(2); byte[] nameBytes = new byte[BOOT_NAME_SIZE];
			 * dis.readFully(nameBytes); nameBytes = revertBytes(nameBytes);
			 * //System.out.println("name " + new String(nameBytes)); byte[]
			 * argsBytes = new byte[BOOT_ARGS_SIZE]; dis.readFully(argsBytes);
			 * //argsBytes = revertBytes(argsBytes);
			 * //System.out.println("args "+new String(argsBytes));
			 */
			dis.close();
			dis = new DataInputStream(new FileInputStream(file));

			File headerFile = new File(folder, "header");
			readSegment(dis, headerFile, pageSize, pageSize);

			// File gzKernelFile = File.createTempFile("kernel", "");

			// http://docs.blackfin.uclinux.org/doku.php?id=bootloaders:u-boot:uimage
			File gzKernelFile = new File(folder, "vmlinux");
			readSegment(dis, gzKernelFile, (int) kernelSize, pageSize);
			// File gzRamDiskFile = File.createTempFile("ramDisk", "");
			// File gzRamDiskFile = new File(folder,"ramDisk");
			File ramDiskFile = new File(folder, "ramdisk.cpio.gz");
			readSegment(dis, ramDiskFile, (int) ramdiskSize, pageSize);
			File secondFile = new File(folder, "second");
			readSegment(dis, secondFile, (int) secondSize, pageSize);
			// System.out.println("test " + dis.read());
			/*
			 * File kernelFile = new File(folder,"kernel"); GZIPInputStream gzis
			 * = new GZIPInputStream(new FileInputStream(gzKernelFile));
			 * Utils.writeToFile(gzis, kernelFile); gzis.close();
			 */
			// GZIPInputStream gzis2 = new GZIPInputStream(new
			// FileInputStream(gzRamDiskFile));
			// Utils.writeToFile(gzis2, ramDiskFile);
			// gzis.close();
			dis.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		File out = new File("boot.img.out");
		Utils.deleteDirectory(out);
		out.mkdir();
		new AndroidBootImgSplitter().unpackToFolder(new File(
				"update-cm-4.2.1-signed/boot.img"), out);
	}

}
