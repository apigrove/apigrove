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
package com.alu.e3.tdr.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.alu.e3.common.performance.PerfWatch;
import com.alu.e3.tdr.rotator.RotatableFileWriterProvider;
import com.alu.e3.tdr.rotator.RotatableWriterProvider;
import com.alu.e3.tdr.rotator.WriterProvider;
import com.alu.e3.tdr.rotator.XmlStreamWriter;

/**
 * Writes out TDRs in XML format.
 * <p>
 * This class encapsulates TDR specific functionality. The XmlStreamWriter superclass
 * (TDR independent) takes care of writing out the XML.
 * 
 * @see {@link XmlStreamWriter} and {@link RotatableWriterProvider}
 */
public class TdrStreamWriter extends XmlStreamWriter {

	private static final int TDR_FILE_SIZE = 10 * 1024 * 1024;   // TDR file size rotation trigger
	private static final int TDR_FILE_AGE = 30 * 1000;   // TDR file age rotation trigger

	private static PerfWatch perfWatch;
	public PerfWatch getPerfWatch() {
		if (perfWatch == null )
			perfWatch = new PerfWatch();
		
		return perfWatch;
	}
	
	public TdrStreamWriter(File dir) throws TransformerConfigurationException {
		this(new RotatableFileWriterProvider(dir, TDR_FILE_SIZE, TDR_FILE_AGE) {
			@Override
			protected String getFileName() {
				return "tdrs." + System.currentTimeMillis() + ".xml";   // TDR file name
			} 

			@Override
			protected void openWriter(Writer writer) throws IOException {
				writer.append("<TDRS>\n");
			}

			@Override
			protected void closeWriter(Writer writer) throws IOException {
				writer.append("</TDRS>\n");
			}
		});
	}

	public TdrStreamWriter(WriterProvider outputWriterProvider) throws TransformerConfigurationException {
		super(outputWriterProvider);
	}

	public void writeTdrs(List<Map<String, Object>> tdrDatas) throws TransformerException, IOException {
		Document doc = getDocBuilder().newDocument();
		for (Map<String, Object> tdrProps : tdrDatas) {
			writeXml(writerBulkTdrXml(doc, tdrProps));
		}
	}

	private Element writerBulkTdrXml(Document doc, Map<String, Object> tdrProps) throws TransformerException, IOException {
	
	Long startTime = System.nanoTime();
			
		// create a root element and fill it up
		Element tdrElem = doc.createElement("TDR");

		if (tdrProps != null) {
			// Add the TDR properties that have been added along the way
			
			for (Entry<String, Object> entry : tdrProps.entrySet()) {
				// create child element, add an attribute, and add to root
				Element child = doc.createElement(entry.getKey());
				child.setTextContent(entry.getValue() == null ? "null" : entry.getValue().toString());
				tdrElem.appendChild(child);
			}
		}

		getPerfWatch().getElapsedTime().addAndGet(System.nanoTime()-startTime);
		getPerfWatch().getIterationCount().getAndIncrement();
		getPerfWatch().log("TdrStreamWriter.writerBulkTdrXml()");
		
		return tdrElem;
	}
}
