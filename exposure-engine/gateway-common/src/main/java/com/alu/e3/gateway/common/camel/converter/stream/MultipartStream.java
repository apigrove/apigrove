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
package com.alu.e3.gateway.common.camel.converter.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;

import org.apache.axiom.attachments.Attachments;
import org.apache.camel.StreamCache;
import org.apache.camel.util.IOHelper;

public class MultipartStream extends InputStream implements StreamCache {

	private InputStream mainPart;
	private InputStream attachments;
	private InputStream stream;
	private ContentType contentType;
	
	public MultipartStream(InputStream in, String contentTypeString) {		
		Attachments at = new Attachments(in, contentTypeString);		
		mainPart = at.getSOAPPartInputStream();
		attachments = at.getIncomingAttachmentsAsSingleStream();
		try {
			contentType = new ContentType(contentTypeString);
		} catch (ParseException e) {
			// Do nothing. If there is something wrong with Content-Type header,
			// Attachments would have raise an exception.
		}
		stream = new SequenceInputStream(toInputStream(getPreamble(at)),
					new SequenceInputStream(new SequenceInputStream(mainPart,
						toInputStream(getBoundaryDelimiter(at))), attachments));
	}
	
	private static final String DELIMITER = "\r\n";
	
	private String getPreamble(Attachments at) {
		String soapPartContentType = at.getSOAPPartContentType();
		// Attachments.getSOAPPartContentID() is tricky. Get Content-ID from request's Content-Type header
		String soapPartContentId = (contentType.getParameter("start") != null ? 
				contentType.getParameter("start") : at.getSOAPPartContentID());

		StringBuilder preamble = new StringBuilder();
		preamble.append(getBoundaryDelimiter(at))
			.append("Content-Type: ").append(soapPartContentType.trim()).append(DELIMITER)
			.append("Content-Transfer-Encoding: 8bit").append(DELIMITER)	//hardcoded
			.append("Content-ID: ").append(soapPartContentId).append(DELIMITER)
			.append(DELIMITER);
        
		return preamble.toString();
	}
	
	private String getBoundaryDelimiter(Attachments at) {
		String boundary = "--" + contentType.getParameter("boundary");
        
		StringBuilder result = new StringBuilder();
		result.append(DELIMITER).append(boundary).append(DELIMITER);
        
		return result.toString();
	}
	
	private InputStream toInputStream(String in) {
		return org.apache.camel.converter.IOConverter.toInputStream(in.getBytes());
	}
	
	@Override
	public int read() throws IOException {
		return stream.read();
	}

	@Override
	public int available() throws IOException {
		return stream.available();
	}

	@Override
	public void close() throws IOException {
		stream.close();
	}

	@Override
	public synchronized void reset() {
		try {
			mainPart.reset();
		} catch (IOException e) {
			// TODO: do something 
		}
	}

	@Override
	public boolean markSupported() {
		return false;
	}
	
	public InputStream getMainPart() {
		return mainPart;
	}

	public InputStream getAtatchments() {
		return attachments;
	}
	
	@Override
	public void writeTo(OutputStream os) throws IOException {
		IOHelper.copy(stream, os);	
	}
}
