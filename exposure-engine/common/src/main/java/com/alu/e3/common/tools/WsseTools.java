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

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Stack;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.codec.binary.Base64;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;

public class WsseTools {

	private final static CategoryLogger LOG = CategoryLoggerFactory.getLogger(WsseTools.class, Category.AUTH);
	
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
	
	public static WsseUsernameToken parseXml(InputStream in) throws ParseException, IOException {
		return new WsseUsernameTokenParser().getWsseUsernameToken(in);
	}


	/**
	 * There's no obvious way to notify a SAX parser that we've got what we need
	 * and it can stop parsing. So when that condition arises we throw this.
	 * 
	 */
	private static class AttentionSpanExhaustedException extends SAXException {
		private static final long serialVersionUID = 7514707683730084613L;
	}

	/**
	 * SAX parser. Can be used to extract a WSSE UsernameToken element without
	 * the overhead of constructing a document for the entire XML input.
	 * 
	 */
	private static class WsseUsernameTokenParser extends org.xml.sax.helpers.DefaultHandler {
		
		// fields to contain the values being extracted
		private String username;
		private String passwordDigest;
		private boolean isPasswordText;
		private String nonce;
		private String created;

		// parse state variables
		private boolean closed;   // the values have been extracted; ignore subsequent elements
		private boolean inUsernameToken;   // within UsernameToken element
		private StringBuilder buf;   // inter-element text accumulator
		private Stack<String> stack;   // element parse stack (only used within UsernameToken)

		public WsseUsernameToken getWsseUsernameToken(InputStream in) throws ParseException, IOException {
			// initialize parse state
			username = null;
			passwordDigest = null;
			nonce = null;
			created = null;
			closed = false;
			inUsernameToken = false;
			isPasswordText = true; // default value according to specs
			buf = new StringBuilder();
			stack = new Stack<String>();
			
			// get a new XML parser
	        javax.xml.parsers.SAXParserFactory spf = SAXParserFactory.newInstance();
	        spf.setValidating(false);  // No validation required
	        spf.setNamespaceAware(true);
	        javax.xml.parsers.SAXParser sp;
			try {
				sp = spf.newSAXParser();
			} catch (ParserConfigurationException e) {
				LOG.error(e.getMessage());
				throw new RuntimeException(e);   // should not happen
			} catch (SAXException e) {
				LOG.error(e.getMessage());
				throw new RuntimeException(e);   // should not happen
			}
			
			// parse the input stream
			IOException streamResetException = null;
	        try {
				sp.parse(new InputSource(in), this);
				
	        } catch (AttentionSpanExhaustedException e) {
	        	// got everything we need; bailing midstream
			} catch (SAXException e) {
				LOG.debug(e.getMessage());
				throw new ParseException(e.getMessage(), 0);
			} catch (IOException e) {
				LOG.debug(e.getMessage());
				throw new ParseException(e.getMessage(), 0);
			} finally {
				// Resetting inputstream to prevent loosing request's body
		        try {
					in.reset();	
				} catch (IOException e) {
					streamResetException = e;
				}
		        
		        // all done -- release resources
		        buf = null;
		        stack = null;
			}
	        
	        if(streamResetException != null) {
	        	throw streamResetException;
	        }
	        
	        // construct and return an immutable token containing the extracted values
			return new WsseUsernameToken(username, passwordDigest, isPasswordText, nonce, created);
		}
		
		/**
		 * Sets parse state to minimize subsequent work. Releases resources.
		 * May be invoked more than once.
		 */
		private void close() {
			inUsernameToken = false;
			closed = true;
			buf = null;
			stack = null;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (closed) {
				return;   // disregard if we've already finished extracting the UsernameToken
			}
			LOG.debug("startElement(\"" + uri + "\", \"" + localName + "\", \"" + qName + "\", " + attributes + ")");
			buf.setLength(0);   // reset text accumulator
			if ("wsse:UsernameToken".equals(qName)) {
				if (inUsernameToken) {
					throw new SAXException("startElement: unexpected UsernameToken in UsernameToken");
				}
				inUsernameToken = true;   // entering UsernameToken
			}
			if (inUsernameToken) {
				stack.push(qName);   // keep track of where we are in the UsernameToken tree
				
				if("wsse:Password".equals(qName)) {
					String passwordType = attributes.getValue("Type");
					if(passwordType != null) {
						isPasswordText = !passwordType.contains("#PasswordDigest");
					}
				}
			}
		}

		@Override
		public void characters(char[] chars, int off, int len) throws SAXException {
			if (!inUsernameToken) {
				return;   // disregard if we're before or after the UsernameToken
			}
			buf.append(chars, off, len);   // accumulate inter-element text
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (!inUsernameToken) {
				return;   // disregard if we're before or after the UsernameToken
			}
			LOG.debug("endElement(\"" + uri + "\", \"" + localName + "\", \"" + qName + "\")");

			// make sure we're where we expect to be in the tree
			String popped = stack.pop();
			if (!qName.equals(popped)) {
				close();
				throw new SAXException("endElement: expected \"" + popped + "\", got \"" + qName + "\"");
			}

			// see if we're at the end of the UsernameToken
			if ("wsse:UsernameToken".equals(qName)) {
				close();   // we've got what we came for
				throw new AttentionSpanExhaustedException();   // bail out of the parser
			}

			// see if we're at the end of an element we're interested in
			if ("wsse:Username".equals(qName)) {
				ensureNotSpecified(username, "Username");
				username = buf.toString();
			} else if ("wsse:Password".equals(qName)) {
				ensureNotSpecified(passwordDigest, "Password");
				passwordDigest = buf.toString();
			} else if ("wsse:Nonce".equals(qName)) {
				ensureNotSpecified(nonce, "Nonce");
				nonce = buf.toString();
			} else if ("wsu:Created".equals(qName)) {
				ensureNotSpecified(created, "Created");
				created = buf.toString();
			}
		}

		/**
		 * Ensures that the specified value is null.
		 * 
		 * @param value the value
		 * @param name the name of the value
		 * @throws SAXException if the value is not null
		 */
		private void ensureNotSpecified(String value, String name) throws SAXException {
			if (value != null) {
				close();
				throw new SAXException("unexpected " + name + ": previously specified");
			}
		}
	}
}
