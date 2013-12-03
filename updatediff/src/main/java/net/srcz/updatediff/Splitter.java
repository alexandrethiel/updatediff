package net.srcz.updatediff;

import java.io.File;

public interface Splitter {

	public boolean canUnpack(File file);
	
	public void unpackToFolder(File file, File folder);
	
	public String getId();
	
	public void repack(String id, File folder, File outputFile);
}
