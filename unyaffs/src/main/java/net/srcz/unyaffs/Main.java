package net.srcz.unyaffs;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import net.srcz.unyaffs.types.ObjectHeader;
import net.srcz.unyaffs.types.PackedTags2;


/**
 * http://unyaffs.googlecode.com/files/unyaffs.c
 */
public class Main {

	public static final int CHUNK_SIZE = 2048;
	public static final int SPARE_SIZE = 64;
	public static final int MAX_OBJECTS = 10000;
	public static final int YAFFS_OBJECTID_ROOT = 1;
	public static final int YAFFS_MAX_NAME_LENGTH  = 255;
	public static final int YAFFS_MAX_ALIAS_LENGTH = 159;
	
	public static final int YAFFS_ECC_RESULT_UNKNOWN = 0;
	public static final int YAFFS_ECC_RESULT_NO_ERROR = 1;
	public static final int YAFFS_ECC_RESULT_FIXED = 2;
	public static final int YAFFS_ECC_RESULT_UNFIXED = 3;
	
	public static final int YAFFS_OBJECT_TYPE_UNKNOWN = 0;
	public static final int YAFFS_OBJECT_TYPE_FILE = 1;
	public static final int YAFFS_OBJECT_TYPE_SYMLINK = 2;
	public static final int YAFFS_OBJECT_TYPE_DIRECTORY = 3;
	public static final int YAFFS_OBJECT_TYPE_HARDLINK = 4;
	public static final int YAFFS_OBJECT_TYPE_SPECIAL = 5;
	
	static byte[] data = new byte[CHUNK_SIZE + SPARE_SIZE];
	static byte[] chunk_data = data;
	static byte[] spare_data = null; // data + CHUNK_SIZE 
	static FileInputStream img_file;
	static String[] obj_list = new String[MAX_OBJECTS];
	
	static void process_chunk(byte[] data) throws Exception
	{
		PackedTags2 pt = StructHandler.read(PackedTags2.class, ByteBuffer.wrap(data, CHUNK_SIZE, data.length-CHUNK_SIZE).order(ByteOrder.LITTLE_ENDIAN));
		if (pt.t.byteCount != 0xffff)
			return;
		ObjectHeader oh = StructHandler.read(ObjectHeader.class,  ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN));
		String full_path_name = obj_list[oh.parentObjectId]+ "/" + StructHandler.getString(oh.name);
		obj_list[pt.t.objectId] = full_path_name;

		switch(oh.type) {
			case YAFFS_OBJECT_TYPE_FILE:
				System.out.println("Extracting file "+full_path_name+" (size "+oh.fileSize+")");
				// open with oh.yst_mode ?
				File outFile = new File(full_path_name);
				FileOutputStream out_file = new FileOutputStream(outFile);
				long remain = ((long)oh.fileSize);
				//System.out.println("remain "+remain);
				while(remain > 0) {
					byte[] chunk_data = new byte[CHUNK_SIZE + SPARE_SIZE];
					int nb_read = img_file.read(chunk_data);
					if(nb_read <= -1)
						break;
					pt = StructHandler.read(PackedTags2.class, ByteBuffer.wrap(chunk_data, CHUNK_SIZE, chunk_data.length-CHUNK_SIZE).order(ByteOrder.LITTLE_ENDIAN));
					//System.out.println("nb_read "+nb_read);
					//System.out.println("chuck bytecount "+pt.t.byteCount);
					if(pt.t.byteCount > CHUNK_SIZE)
						pt.t.byteCount = CHUNK_SIZE;
					int s = (remain < pt.t.byteCount) ? (int)remain : pt.t.byteCount;	
					//System.out.println("s "+s);
					out_file.write(chunk_data, 0, s);
					remain -= s;
				}
				//close(out_file);
				break;
			case YAFFS_OBJECT_TYPE_SYMLINK:
				//symlink(oh->alias, full_path_name);
				break;
			case YAFFS_OBJECT_TYPE_DIRECTORY:
				//mkdir(full_path_name, 0777);
				new File(full_path_name).mkdir();
				break;
			case YAFFS_OBJECT_TYPE_HARDLINK:
				//link(obj_list[oh->equivalentObjectId], full_path_name);
				break;
		}
	}


	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: unyaffs image_file_name");
			System.exit(1);
		}
		try {
			// system.img
			// recovery-RAv1.2.0G.img
			img_file =  new FileInputStream(args[0]);

			obj_list[YAFFS_OBJECTID_ROOT] = "extract";
			while(true) {
				byte[] chunk_data = new byte[CHUNK_SIZE + SPARE_SIZE];
				int nb_read = img_file.read(chunk_data);
				//System.out.println("nb read = "+nb_read);
				if(nb_read != chunk_data.length)
					break;
				process_chunk(chunk_data);
			}
			img_file.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	
	
}
