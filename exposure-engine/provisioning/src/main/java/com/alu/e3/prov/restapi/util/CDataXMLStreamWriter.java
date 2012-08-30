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
package com.alu.e3.prov.restapi.util;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.cxf.staxutils.DelegatingXMLStreamWriter;

public class CDataXMLStreamWriter extends DelegatingXMLStreamWriter {

	private String currentElementName;

	public CDataXMLStreamWriter(XMLStreamWriter del) {
		super(del);
	}

	@Override
	public void writeCharacters(String text) throws XMLStreamException {
		boolean useCData = checkIfCDATAneededForCurrentElement();
		if (useCData) {
			super.writeCData(text);
		} else {
			super.writeCharacters(text);
		}
	}

	private boolean checkIfCDATAneededForCurrentElement() {
		if ("grammar".equals(currentElementName))
			return true;
		return false;
	}

	public void writeStartElement(String prefix, String local, String uri) throws XMLStreamException {
		currentElementName = local;
		super.writeStartElement(prefix, local, uri);
	}
}