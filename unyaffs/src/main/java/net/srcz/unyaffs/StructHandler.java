package net.srcz.unyaffs;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

public class StructHandler {

	public static String getString(char[] data) {
		int i=0; 
		while(i <data.length) {
			if(data[i] == '\0')
				return new String(data,0,i);
			i++;
		}
		return new String(data);
	}

	public static long uint(int n) {
		long n2 = n;
		n2 &= 0xffffffff;
		return n2;
	}
	

	public static byte intToPseudoUnsignedByte(int n) {
		if (n < 128) return (byte)n;
		return (byte)(n - 256);
	}
	
	public static int unsignedInt(int char1, int char2) {
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.put(new byte[] {0, 0, intToPseudoUnsignedByte(char1), intToPseudoUnsignedByte(char2)});
		bb.rewind();
		return bb.getInt();
	}
	
	public static int getSize(Class<?> c) {
		
		if(c.equals(Integer.TYPE))
			return 4;

		if(c.equals(Character.TYPE))
			return 1;

		if(c.equals(Byte.TYPE))
			return 1;

		if(c.equals(Short.TYPE))
			return 2;

		if(c.equals(Long.TYPE))
			return 8;

		int nb = 0;
		try {
			for(Field f : c.getFields()) {
				//System.out.println(f.getName()+" "+f.getType());
				Object o = null;
				if(f.getType().isArray()) {
					if(o == null)
						o = c.newInstance();
					Object fval = f.get(o);
					nb += Array.getLength(fval)*getSize(f.getType().getComponentType());
				} else {
					nb += getSize(f.getType());
				}
			}
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
		return nb;	
	}
	
	public static <T> T read(Class<T> c, ByteBuffer is) {
		try {
			T obj = c.newInstance();
			for(Field f : c.getFields()) {
				if(f.getType().isArray()) {
					//System.out.println("array "  + f.getName()+" "+f.getType());
					int nb = Array.getLength(f.get(obj));
					Class<?> cc = f.getType().getComponentType();
					for(int i=0; i<nb; i++) {
						if(cc.equals(Integer.TYPE))
							Array.set(f.get(obj),i, is.getInt());
						else
							if(cc.equals(Short.TYPE))
								Array.set(f.get(obj),i, is.getShort());
							else
								if(cc.equals(Character.TYPE))
									Array.set(f.get(obj),i, (char)is.get());
								else
									if(cc.equals(Byte.TYPE))
										Array.set(f.get(obj),i, is.get());
								else
									Array.set(f.get(obj),i, read(cc,is));
						//System.out.println("obj "+i+" = "  + f.getName()+" "+Array.get(f.get(obj),i));
					}
				} else {
					if(f.getType().equals(Integer.TYPE))
						f.set(obj, is.getInt());
					else
						if(f.getType().equals(Short.TYPE))
							f.set(obj, is.getShort());
						else
							if(f.getType().equals(Character.TYPE))
								f.set(obj, (char)is.get());
							else
								if(f.getType().equals(Byte.TYPE))
									f.set(obj, is.get());
							else
								f.set(obj, read(f.getType(),is));
					/*if(c.equals(ObjectHeader.class))
						System.out.println("obj "  + f.getName()+" "+f.get(obj));
						*/
				}
					
			}
			return obj;
		} catch (Exception e) {
			throw new RuntimeException("Erreur reading"+c,e);
		}
	}
	
}
