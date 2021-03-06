/**
 * Copyright (c) 2016-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.zsmartsystems.zigbee.dongle.ember.ezsp.structure;

import com.zsmartsystems.zigbee.IeeeAddress;
import com.zsmartsystems.zigbee.dongle.ember.ezsp.serializer.EzspDeserializer;
import com.zsmartsystems.zigbee.dongle.ember.ezsp.serializer.EzspSerializer;

/**
 * Class to implement the Ember Structure <b>EmberBindingTableEntry</b>.
 * <p>
 * An entry in the binding table.
 * <p>
 * Note that this code is autogenerated. Manual changes may be overwritten.
 *
 * @author Chris Jackson - Initial contribution of Java code generator
 */
public class EmberGpAddress {

	/**
	 * The GPD's EUI64.
	 */
    private IeeeAddress gpdIeeeAddress;
    
    /**
     * The GPD's source ID.
     */
    private int sourceId;
    
    /**
     * The GPD Application ID.
     */
    private int applicationId;
    
    /**
     * The GPD endpoint.
     */
    private int endpoint;

    /**
     * Default Constructor
     */
    public EmberGpAddress() {
    }
    
	public EmberGpAddress(EzspDeserializer deserializer) {
        deserialize(deserializer);
    }

	/**
	 * getter/setter 
	 */

    public IeeeAddress getGpdIeeeAddress() {
		return gpdIeeeAddress;
	}

	public int getSourceId() {
		return sourceId;
	}

	public int getApplicationId() {
		return applicationId;
	}

	public int getEndpoint() {
		return endpoint;
	}

	public void setGpdIeeeAddress(IeeeAddress gpdIeeeAddress) {
		this.gpdIeeeAddress = gpdIeeeAddress;
	}

	public void setSourceId(int sourceId) {
		this.sourceId = sourceId;
	}

	public void setApplicationId(int applicationId) {
		this.applicationId = applicationId;
	}

	public void setEndpoint(int endpoint) {
		this.endpoint = endpoint;
	}


    /**
     * Serialise the contents of the EZSP structure.
     *
     * @param serializer the {@link EzspSerializer} used to serialize
     */
    public int[] serialize(EzspSerializer serializer) {
        // Serialize the fields
    	System.out.println("EmberGpAddress serialize ERROR : NOT YET MANAGED !!!");
    	/*
    	if( 0 == applicationId ) {
            serializer.serializeUInt8(applicationId);
            serializer.serializeUInt8(endpoint);
            serializer.serializeUInt32(sourceId);
            serializer.serializeUInt32(sourceId);
    	}        
    	else {
//            serializer.serializeEmberEui64(gpdIeeeAddress);
        	System.out.println("EmberGpAddress serialize ERROR : Unknown application Id Type !!!");
        }
        */
        return serializer.getPayload();
    }

    /**
     * Deserialise the contents of the EZSP structure.
     *
     * @param deserializer the {@link EzspDeserializer} used to deserialize
     */
    public void deserialize(EzspDeserializer deserializer) {
        // Deserialize the fields
        applicationId = deserializer.deserializeUInt8();
        if( 0 == applicationId ) {
            endpoint = deserializer.deserializeUInt8();
        	sourceId = (deserializer.deserializeUInt8()<<24) + (deserializer.deserializeUInt8()<<16) + 
        			(deserializer.deserializeUInt8()<<8) + (deserializer.deserializeUInt8()<<0);
        	int l_sourceId = (deserializer.deserializeUInt8()<<24) + (deserializer.deserializeUInt8()<<16) + 
        			(deserializer.deserializeUInt8()<<8) + (deserializer.deserializeUInt8()<<0);
        	//System.out.println("EmberGpAddress deserialize source1 : " + String.format("%08X", sourceId) + ", source2 : " + String.format("%08X", l_sourceId));
        	gpdIeeeAddress = new IeeeAddress("000474FF" + String.format("%08X", sourceId) );
        }
        else {
        	System.out.println("EmberGpAddress deserialize ERROR : Unknown application Id Type !!!");
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(175);
        builder.append("EmberGpAddress [gpdIeeeAddress=");
        builder.append(gpdIeeeAddress.toString());
        builder.append(", sourceId=");
        builder.append(sourceId);
        builder.append(", applicationId=");
        builder.append(applicationId);
        builder.append(", endpoint=");
        builder.append(endpoint);
        builder.append(']');
        return builder.toString();
    }
}
