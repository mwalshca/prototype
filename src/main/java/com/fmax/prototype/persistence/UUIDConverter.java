package com.fmax.prototype.persistence;

import java.nio.ByteBuffer;
import java.util.UUID;

import javax.persistence.AttributeConverter;

public class UUIDConverter implements AttributeConverter<UUID,byte[]> {

	//TODO make unit test
	public static void main(String[] args) {
		UUIDConverter converter = new UUIDConverter();
		
		UUID original = UUID.randomUUID();
		byte[] column = converter.convertToDatabaseColumn(original);
		UUID test = converter.convertToEntityAttribute(column);
		System.out.println( original.equals(test));
	}
	
	@Override
	public byte[] convertToDatabaseColumn(UUID attribute) {
		 ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * 2);
		 buffer.putLong( attribute.getMostSignificantBits() );
		 buffer.putLong( attribute.getLeastSignificantBits() );
		  
		 return buffer.array();
	}

	@Override
	public UUID convertToEntityAttribute(byte[] dbData) {
		 ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * 2);
		 buffer.put(dbData);
		 buffer.flip();
		 long msb = buffer.getLong();
		 long lsb = buffer.getLong();
		 return new UUID( msb, lsb );
	}
	
	public byte[] longToBytes(long x) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.putLong(x);
	    return buffer.array();
	}

	public long bytesToLong(byte[] bytes) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.put(bytes);
	    buffer.flip();//need flip 
	    return buffer.getLong();
	}

}
