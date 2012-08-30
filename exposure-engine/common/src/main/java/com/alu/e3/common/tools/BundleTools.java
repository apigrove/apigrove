/**
 * Copyright © 2012 Alcatel-Lucent.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * Licensed to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.alu.e3.common.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class BundleTools {

	public static byte[] file2ByteArray(InputStream fileInputStream) throws Exception {
		return IOUtils.toByteArray(fileInputStream);
	}
	
	public static byte[] file2ByteArray(File file) throws Exception {
		InputStream fileInputStream = new FileInputStream(file);
		
		return file2ByteArray(fileInputStream);
	}
	
	public static void byteArray2File(byte[] inputByteArray, File outputFile) throws Exception {
		FileUtils.writeByteArrayToFile(outputFile, inputByteArray);
	}
}
