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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.namespace.QName;

import org.apache.commons.codec.binary.Base64;

import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;

public class WsseTools {

	private final static CategoryLogger LOG = CategoryLoggerFactory.getLogger(WsseTools.class, Category.AUTH);

	public static final String WSSE_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
	public static final String WSU_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
	
	public static final String WSSE_LN = "Security";
	public static final String USERNAME_TOKEN_LN = "UsernameToken";
	public static final String USERNAME_LN = "Username";
	public static final String PASSWORD_LN = "Password";
	public static final String NONCE_LN = "Nonce";
	public static final String CREATED_LN = "Created";
	
	public static final String PASSWORD_TYPE_ATTR = "Type";
	public static final String PASSWORD_TYPE_TEXT = "#PasswordText";
	public static final String PASSWORD_TYPE_DIGEST = "#PasswordDigest";
	
	public static final QName WSSE_SECURITY = new QName(WSSE_NS, WSSE_LN);
	public static final QName USERNAME_TOKEN = new QName(WSSE_NS, USERNAME_TOKEN_LN);
	public static final QName USERNAME = new QName(WSSE_NS, USERNAME_LN);
	public static final QName PASSWORD_TYPE = new QName(null, PASSWORD_TYPE_ATTR);
	
	private static final SimpleDateFormat[] matchingFormats = new SimpleDateFormat[] {
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"), // Standard format
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
	};

	static {
		for (SimpleDateFormat format : matchingFormats) {
			format.setTimeZone(TimeZone.getTimeZone("GMT"));
		}
	}

	/**
	 * Validates the WSSE username token.
	 * 
	 * @param passwordDigest the password digest -- ought to be Base64-encoded SHA-1 digest of (nonceBytes + createdBytes + passwordBytes)
	 * @param nonce one-time use bits
	 * @param created when created
	 * @param passwordBytes the server's copy of the shared secret
	 * @return true if the passwordDigest is valid, else false
	 */
	public static boolean isValid(String passwordDigest, String nonce, String created, byte[] passwordBytes) {
		return passwordDigest.equals(new String(getPasswordDigestBytes(nonce, created, passwordBytes)));
	}

	public static byte[] getNonceBytes(String nonce) {
		return nonce.getBytes();
	}
	
	public static String getNonce(byte[] nonceBytes) {
		return new String(Base64.encodeBase64(nonceBytes));
	}
	
	public static Date getCreatedDate(String created) throws ParseException {
		Date dateParsed = null;
		for (DateFormat format : matchingFormats){
			try{
				synchronized (format) {   // some implementations of SimpleDateFormat are not thread-safe
					dateParsed = format.parse(created);
					return dateParsed;
				}
			}catch(Exception e){
				// Do nothing, try next format available
			}
		}
		if(dateParsed == null) throw new ParseException("Unable to parse date: " + created, 0);
		return dateParsed;
	}
	
	public static String getCreated(Date when) {
		String date = null;
		for (DateFormat format : matchingFormats){
			synchronized (format) {   // some implementations of SimpleDateFormat are not thread-safe
				date = format.format(when);
				return date;
			}
		}
		return date;
	}
	
	public static String getPasswordDigest(byte[] passwordDigestBytes) {
		return new String(passwordDigestBytes);
	}

	public static byte[] getPasswordDigestBytes(String nonce, String created, byte[] passwordBytes) {
		byte[] nonceBytes = nonce != null ? Base64.decodeBase64(nonce.getBytes()) : null;
		byte[] createdBytes = created != null ? created.getBytes() : null;
		byte[] bytes = concatenate(nonceBytes, createdBytes, passwordBytes);
		try {
			return Base64.encodeBase64(MessageDigest.getInstance("SHA-1").digest(bytes));
		} catch (NoSuchAlgorithmException e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);   // missing message digest algorithm -- should not happen
		}
    }
	
	static byte[] concatenate(byte[] ... byteArrays) {
		int len = 0;
		for (byte[] bytes : byteArrays) {
			if (bytes != null) {
				len += bytes.length;
			}
		}
		byte[] allBytes = new byte[len];
		int off = 0;
		for (byte[] bytes : byteArrays) {
			if (bytes != null) {
				System.arraycopy(bytes, 0, allBytes, off, bytes.length);
				off += bytes.length;
			}
		}
		return allBytes;
	}


	public static class WsseUsernameToken {
		
		private final String username;
		private final String password;
		private final String nonce;
		private final String created;
		private final boolean isPasswordText;
		
		public WsseUsernameToken(String username, String password, boolean isPasswordText, String nonce, String created) {
			this.username = username;
			this.password = password;
			this.nonce = nonce;
			this.created = created;
			this.isPasswordText = isPasswordText;
		}
		
		public String getUsername() {
			return username;
		}
		
		public String getPassword() {
			return password;
		}
		
		public String getNonce() {
			return nonce;
		}
		
		public String getCreated() {
			return created;
		}
		
		public boolean isPasswordText() {
			return isPasswordText;
		}
	}
}
