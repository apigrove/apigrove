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
package com.alu.e3.tdr.rotator;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;

import com.alu.e3.common.performance.PerfWatch;

/**
 * Writes arbitrary XML to a {@link Writer} (a character stream), which is obtained
 * from a {@link WriterProvider}.
 * 
 */
public class XmlStreamWriter {

	private final DocumentBuilder docBuilder;
	
	private Transformer transformer = null;

	// source of the current Writer
	private final WriterProvider outputWriterProvider;
	
	private static PerfWatch perfWatch;
	public PerfWatch getPerfWatch() {
		if (perfWatch == null )
			perfWatch = new PerfWatch();
		
		return perfWatch;
	}
	
	public XmlStreamWriter(WriterProvider outputWriterProvider)throws TransformerConfigurationException {
		this.outputWriterProvider = outputWriterProvider;
		
		TransformerFactory factory = TransformerFactory.newInstance();
		this.transformer = factory.newTransformer();
		this.transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		this.transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		
		try {
			docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("DOM Builder error: ", e);
		}
	}
	
	public DocumentBuilder getDocBuilder() {
		return docBuilder;
	}

	/**
	 * Writes the specified DOM tree in XML format to the current character output stream.
	 * 
	 * @param root top of the DOM tree to be written
	 * @throws TransformerException
	 * @throws IOException
	 */
	
	
	public void writeXml(Element root) throws TransformerException, IOException {
	
		Long startTime = System.nanoTime();

		Source source = new DOMSource(root.cloneNode(true));
		synchronized (outputWriterProvider.getSynchroLock()) {   // ensure atomicity of the entire DOM write
			Writer outputWriter = outputWriterProvider.getWriter();   // get current output stream
			Result result = new StreamResult(outputWriter);
			transformer.transform(source, result);   // write out the XML
			
		}
		
		getPerfWatch().getElapsedTime().addAndGet(System.nanoTime()-startTime);
		getPerfWatch().getIterationCount().getAndIncrement();
		getPerfWatch().log("XmlStreamWriter.writeXml()");
	}
	
	public void stop() {
		synchronized (outputWriterProvider.getSynchroLock()) {
			outputWriterProvider.stop();
		}
	}

	
}
