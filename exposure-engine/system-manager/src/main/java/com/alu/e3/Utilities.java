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
package com.alu.e3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.List;

import com.alu.e3.common.E3Constant;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.tools.CommonTools;
import com.alu.e3.data.model.Instance;
import com.alu.e3.installer.NonExistingManagerException;

public class Utilities {

	public static String getStackTrace(Throwable throwable) {
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		throwable.printStackTrace(printWriter);
		return writer.toString();
	}
	
	public static void copyFile(File srcFile, File dstFile, boolean overwrite) throws IOException {

		// preconditions check
		if(dstFile.isDirectory())
			throw new IOException("copyFile: destination is a directory: " + dstFile.getAbsolutePath());
		
		if(dstFile.exists() && !overwrite)
			throw new IOException("copyFile: destination file already exists: " + dstFile.getAbsolutePath());

		FileChannel srcChannel = null;
		FileChannel dstChannel = null;
		
		try {
			srcChannel = new FileInputStream(srcFile).getChannel();
			dstChannel = new FileOutputStream(dstFile).getChannel();
		
			long transfered = 0;
			long size = srcChannel.size();
			
			while ((transfered += srcChannel.transferTo(0, size-transfered, dstChannel)) < size);
			
		} finally {
			if(srcChannel != null)
				srcChannel.close();
			
			if(dstChannel != null)
				dstChannel.close();
		}
	}

	
	/**
	 * Method getManagerByIP
	 * @param managerHostName the manager's host name
	 * @param managerIP the manager's ip adress
	 * @param topology 
	 * @param logger 
	 * @return Instance the manager of that ip if existing
	 * @throws NonExistingManagerException thrown if parameter is no valid ip
	 */
	public static Instance getManagerByIP(String managerHostName, String managerIP, List<Instance> instances, final CategoryLogger logger) throws NonExistingManagerException {
		
		boolean found = false;
		Iterator<Instance> iter = instances.iterator();
		
		Instance manager = null;
		
		while (!found && iter.hasNext()) {
			Instance currentInstance = iter.next();
			String currentIP = currentInstance.getInternalIP();
			if (currentIP == null) {
				logger.warn("Encountered instance in topology with null internal IP: {}", currentInstance);
				continue;
			}
			if(logger.isDebugEnabled()) {
				logger.debug("Comparing manager ip '" + managerIP + "' with current instance internal ip '" + currentIP + "'");
			}
			//instance internal ip has to match manager ip or manager hostname (allows to work with both) 
			//for AIB install, the internalIP for the manager is "localhost", so for AIB allow match on "localhost"
			if (currentIP.equals(managerIP) || currentIP.equals(managerHostName) || //currentIP.equals(E3Constant.localhost)) {
					(CommonTools.isLocal(managerIP) && CommonTools.isLocal(currentIP))) {
				found = true;
				if(logger.isDebugEnabled()) {
					logger.debug("Found manager with ip '" + managerIP + "' found");
				}
				manager = currentInstance;
			}
		}
		
		if (!found) { //this manager ip does not exist
			if(logger.isErrorEnabled()) {
				logger.error("No manager with ip '" + managerIP + "' found");
			}
			throw new NonExistingManagerException("No manager with ip '" + managerIP + "' found");
		}
		
		return manager;
	}
	

}
