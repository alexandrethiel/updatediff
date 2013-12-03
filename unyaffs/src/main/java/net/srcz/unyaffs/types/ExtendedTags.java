package net.srcz.unyaffs.types;

public class ExtendedTags {

    int validMarker0;
    int chunkUsed; //  Status of the chunk: used or unused 
    int objectId;  // If 0 then this is not part of an object (unused) 
    int chunkId;   // If 0 then this is a header, else a data chunk 
    int byteCount; // Only valid for data chunks 

    // The following stuff only has meaning when we read 
    int eccResult;
    int blockBad;

    // YAFFS 1 stuff 
    int chunkDeleted;  // The chunk is marked deleted 
    int serialNumber;  // Yaffs1 2-bit serial number 

    // YAFFS2 stuff 
    int sequenceNumber;    // The sequence number of this block 

    // Extra info if this is an object header (YAFFS2 only) 

    int extraHeaderInfoAvailable;  // There is extra info available if this is not zero 
    int extraParentObjectId;   // The parent object 
    int extraIsShrinkHeader;   // Is it a shrink header? 
    int extraShadows;      // Does this shadow another object? 

    int extraObjectType;   // What object type? 

    int extraFileLength;       // Length if it is a file 
    int extraEquivalentObjectId;   // Equivalent object Id if it is a hard link 

    int validMarker1;
}
