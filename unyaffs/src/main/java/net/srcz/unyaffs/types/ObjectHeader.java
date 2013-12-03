package net.srcz.unyaffs.types;

import net.srcz.unyaffs.Main;

public class ObjectHeader {

    public int type;

    // Apply to everything  
    public int parentObjectId;
    public short sum__NoLongerUsed;     // align to int ?   // checksum of name. No longer used 
    public char[] name = new char[Main.YAFFS_MAX_NAME_LENGTH+1];
    public short sum__NoLongerUsed2;     // align to int ?   // checksum of name. No longer used 
    
	// The following apply to directories, files, symlinks - not hard links 
    public int yst_mode;         // protection 

    public int yst_uid;
    public int yst_gid;
    public int yst_atime;
    public int yst_mtime;
    public int yst_ctime;

    // File size  applies to files only 
    public int fileSize;

    // Equivalent object id applies to hard links only. 
    public int equivalentObjectId;

    // Alias is for symlinks only. 
    public char[] alias = new char[Main.YAFFS_MAX_ALIAS_LENGTH+1];

    public int yst_rdev;     // device stuff for block and char devices (major/min) 

    public int[] win_ctime = new int[2];
    public int[] win_atime = new int[2];
    public int[] win_mtime = new int[2];

    public int inbandShadowsObject;
    public int inbandIsShrink;

    public int[] reservedSpace = new int[2];
    public int shadowsObject;  // This object header shadows the specified object if > 0 

    // isShrink applies to object headers written when we shrink the file (ie resize) 
    public int isShrink;
}
