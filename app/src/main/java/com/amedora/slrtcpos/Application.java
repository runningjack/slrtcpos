/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.amedora.slrtcpos;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

//import android.content.SharedPreferences;

public class Application extends  android.app.Application {

	private SerialPort mSerialPort = null;

	public SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
		if (mSerialPort == null) {
			/* Read serial port parameters */
			//SharedPreferences sp = getSharedPreferences("android_serialport_api.sample_preferences", MODE_PRIVATE);
			String path = "/dev/ttyS1"; // HdxUtil.GetPrinterPort();;"/dev/ttyS2"

			int baudrate = 115200;//Integer.decode(sp.getString("BAUDRATE", "-1"));

			/* Open the serial port */
			mSerialPort = new SerialPort(new File(path), baudrate, 0);
		}
		return mSerialPort;
	}

	public void closeSerialPort() {

		if (mSerialPort != null) {
			mSerialPort.close();
			mSerialPort = null;
		}
	}
}
