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
package com.alu.e3.prov.lifecycle;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IDHelper {

	private static final Logger logger = LoggerFactory.getLogger(IDHelper.class);
	
	/**
	 * Extract the apiID from the given filename.
	 * @param fileName
	 * @return the API ID
	 */
	public static String extractApiIdFromFileName(String fileName) {
		String [] datas = extractAllFromFileName(fileName);
		return datas == null ? null : datas[1];
	}
	
	/**
	 * Extract datas from the given filename.
	 * @param fileName
	 * @return a String[] containing respectively: { apiIDEncoded, apiID, provID }
	 */
	public static String[] extractAllFromFileName(String fileName) {
		Pattern p = Pattern.compile("([a-f0-9]+)-([a-f0-9]+)\\.[a-z]{3}");
		Matcher m = p.matcher(fileName);
		if (!m.matches()) return null;
		
		String apiIDEncoded = m.group(1);
		String apiID = null;
		try {
			apiID = IDHelper.decode(apiIDEncoded);
		} catch (RuntimeException e) {
			e.printStackTrace();
			logger.error("Undecodable apiId:{}", apiIDEncoded);
		}
		String provID = m.group(2);
		
		String[] datas = new String[] {
				apiIDEncoded,
				apiID,
				provID
		};
		
		return datas;
	}
	
	// Generate a unique ID to be used as apiID or provID
	public static String generateUID() {
		return UUID.randomUUID().toString().replace("-", "");
	}
	
	public static String encode(String original) {
		return Hex.encodeHexString(original.getBytes());
	}
	
	public static String decode(String encoded) {
		try {
			return new String(Hex.decodeHex(encoded.toCharArray()));
		} catch (DecoderException e) {
			throw new RuntimeException(e);
		}
	}
	
}
