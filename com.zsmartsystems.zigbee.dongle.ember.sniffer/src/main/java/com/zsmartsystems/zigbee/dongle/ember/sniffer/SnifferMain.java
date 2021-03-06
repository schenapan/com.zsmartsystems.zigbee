/**
 * Copyright (c) 2016-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.zsmartsystems.zigbee.dongle.ember.sniffer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.LoggerFactory;

import com.zsmartsystems.zigbee.dongle.ember.EzspFrameHandler;
import com.zsmartsystems.zigbee.dongle.ember.ZigBeeDongleEzsp;
import com.zsmartsystems.zigbee.dongle.ember.ezsp.EzspFrame;
import com.zsmartsystems.zigbee.dongle.ember.ezsp.command.EzspMfgLibRxHandler;
import com.zsmartsystems.zigbee.dongle.ember.ezsp.command.EzspMfgLibSetChannelRequest;
import com.zsmartsystems.zigbee.dongle.ember.ezsp.command.EzspMfgLibSetChannelResponse;
import com.zsmartsystems.zigbee.dongle.ember.ezsp.command.EzspMfgLibStartRequest;
import com.zsmartsystems.zigbee.dongle.ember.ezsp.command.EzspMfgLibStartResponse;
import com.zsmartsystems.zigbee.dongle.ember.ezsp.structure.EmberStatus;
import com.zsmartsystems.zigbee.serial.ZigBeeSerialPort;
import com.zsmartsystems.zigbee.transport.ZigBeePort;
import com.zsmartsystems.zigbee.transport.ZigBeePort.FlowControl;

public class SnifferMain {
    /**
     * The {@link org.slf4j.Logger}.
     */
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(SnifferMain.class);
    
    private static boolean captureEnable = false;
    private static File file;
    private static FileOutputStream fos = null;

    
    private static EzspFrameHandler ezspListener = new EzspFrameHandler() {

		@Override
		public void handlePacket(EzspFrame response) {
			if( captureEnable ) {
				if(response instanceof EzspMfgLibRxHandler) {
					int[] msg = ((EzspMfgLibRxHandler)response).getMessageContents();
					
					byte[] out = new byte[msg.length];
					
					for(int loop=0; loop<msg.length; loop++) {
						out[loop] = (byte) msg[loop];
					}
										
					try {
						// timestamp
						long stamp = System.currentTimeMillis();
						fos.write((byte)(stamp&0xff));
						fos.write((byte)(stamp>>8)&0xff);
						fos.write((byte)(stamp>>16)&0xff);
						fos.write((byte)(stamp>>24)&0xff);
						fos.write((byte)(stamp&0xff));
						fos.write((byte)(stamp>>8)&0xff);
						fos.write((byte)(stamp>>16)&0xff);
						fos.write((byte)(stamp>>24)&0xff);
						
						// packet length
						fos.write((byte)msg.length);
						fos.write(0);
						fos.write(0);
						fos.write(0);
						fos.write((byte)msg.length);
						fos.write(0);
						fos.write(0);
						fos.write(0);
						
						// packet
						fos.write(out);
						fos.flush();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}

		@Override
		public void handleLinkStateChange(boolean state) {
			// TODO Auto-generated method stub
			
		}
    	
    };
    		

    /**
     * The main method.
     *
     * @param args the command arguments
     */
    public static void main(final String[] args) {
        
        final String serialPortName = "/dev/ttyUSB0";
        final int serialBaud = 57600;
        final FlowControl flowControl = FlowControl.FLOWCONTROL_OUT_XONOFF;

        logger.info("Create Serial Port");
        final ZigBeePort serialPort = new ZigBeeSerialPort(serialPortName, serialBaud, flowControl);

        logger.info("Create ezsp dongle");
        final ZigBeeDongleEzsp dongle = new ZigBeeDongleEzsp(serialPort);

        logger.info("ASH Init");
        if( dongle.initializeEzspProtocol() ) {
        	
        	Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
    			
    			@Override
    			public void run() {
    				captureEnable = false;
        	        try {
        				Thread.sleep(1000);
        			} catch (InterruptedException e) {
        				// TODO Auto-generated catch block
        				e.printStackTrace();
        			}
    			}
    		}));
        	
        	/** @todo for testing initialization of zigbee pro stack
        	logger.info("Configure stack");
            dongle.setStackConfigurationValue( EzspConfigId.EZSP_CONFIG_ADDRESS_TABLE_SIZE, 100 );
            dongle.applyStackConfiguration();
            
            logger.info("Configure policy");
            dongle.setStackPolicyValue( EzspPolicyId.EZSP_POLL_HANDLER_POLICY, EzspDecisionId.EZSP_POLL_HANDLER_IGNORE );
            dongle.applyStackPolicy();
            
            logger.info("Add endpoint");
            dongle.addEndpoint(1, 0x0007, ZigBeeProfileType.HOME_AUTOMATION.getId(), new int[] { 0 }, new int[] { 0 });
            
            logger.info("Start network");
            dongle.initializeZigbeeNetwork();
            */
        	
        	/** @todo for testing initialization of mfgLib */
        	// add listener for receive alla ezsp response and handler
        	dongle.addListener( ezspListener );
        	
        	// start MfgLib
        	EzspMfgLibStartRequest mfgStartRqst = new EzspMfgLibStartRequest();
        	mfgStartRqst.setRxCallback(true);
        	EzspMfgLibStartResponse mfgStartRsp = (EzspMfgLibStartResponse) dongle.singleCall(mfgStartRqst, EzspMfgLibStartResponse.class);
        	if(EmberStatus.EMBER_SUCCESS == mfgStartRsp.getStatus()) {
            	// set channel
        		EzspMfgLibSetChannelRequest mfgSetChannelRqst = new EzspMfgLibSetChannelRequest();
        		mfgSetChannelRqst.setChannel(11);
        		EzspMfgLibSetChannelResponse mfgSetChannelRsp = (EzspMfgLibSetChannelResponse) dongle.singleCall(mfgSetChannelRqst, EzspMfgLibSetChannelResponse.class);
        		
        		if(EmberStatus.EMBER_SUCCESS == mfgSetChannelRsp.getStatus() ) {
        			// create capture file
        			String fileName = "zigbee_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date()) + ".cap";
        			file = new File(fileName);
        			boolean fvar = false;
        			
        			try {
						fvar = file.createNewFile();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
        			
        			if( fvar ) {
            			// write header file
        				try {
							fos = new FileOutputStream(file);
						} catch (FileNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
        				
        				byte[] header = {(byte)0xd4, (byte)0xc3, (byte)0xb2, (byte)0xa1, 0x02, 0x00, 0x04, 0x00,
			                                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			                                (byte)0xff, (byte)0xff, 0x00, 0x00, (byte)0xc3, 0x00, 0x00, 0x00};
        				
        				try {
							fos.write(header);
							fos.flush();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
        				
            			
            			// enable capture
            			captureEnable = true;
            			
                    	// infinite loop ?
                        logger.info("While loop");
                    	while(captureEnable) {
                	        try {
                				Thread.sleep(500);
                			} catch (InterruptedException e) {
                				// TODO Auto-generated catch block
                				e.printStackTrace();
                			}
                    	}
                    	
                    	try {
							fos.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                    }
        		}
        	}
        }

        dongle.shutdown();
        logger.info("Sniffer closed.");
    }

}
