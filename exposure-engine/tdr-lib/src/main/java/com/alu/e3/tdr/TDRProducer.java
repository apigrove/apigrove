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
package com.alu.e3.tdr;

import java.io.StringWriter;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TDRProducer extends DefaultProducer {
	
	private static Logger logger = LoggerFactory.getLogger(TDRProducer.class);
	private TDREndpoint tdrEndpoint;
	
	DocumentBuilder docBuilder = null;
    
	public TDRProducer(Endpoint endpoint) {
		super(endpoint);
		
		if(!(endpoint instanceof TDREndpoint)){
			throw new RuntimeException("Unsupported endpoint type:"+ endpoint.getClass().getName());
		}
		
		try {
			docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("DOM Builder error :", e);
		}
		
		// Keep another reference so I don't have to cast everywhere to get the properties out
		tdrEndpoint = (TDREndpoint) endpoint;
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		logger.debug("TDR Processor Hit");
		Map<String, Object> tdrProps = TDRDataService.getTxTDRProperties(exchange);
				
        Document doc = docBuilder.newDocument();
        
        //create the root element and add it to the document
        Element root = doc.createElement("tdr");
        root.setAttribute("id", exchange.getExchangeId());
        doc.appendChild(root);
        
        // Add the headers
        for(String head : this.tdrEndpoint.getHeaders()){
        	Object value = exchange.getIn().getHeader(head);
        	if(value == null){
        		value = "null";
        	}
        	
        	//create child element, add an attribute, and add to root
        	Element child = doc.createElement("nvp");
        	child.setAttribute("name", head);
        	child.setAttribute("value", value.toString());
        	root.appendChild(child);
        }
        
        // Add the TDR properties that have been added along the way
        for(Entry<String, Object> tdrProp : tdrProps.entrySet()){
        	//create child element, add an attribute, and add to root
        	Element child = doc.createElement("nvp");
        	child.setAttribute("name", tdrProp.getKey());
        	child.setAttribute("value", tdrProp.getValue() == null ? "null" : tdrProp.getValue().toString());
        	root.appendChild(child);
        }
        
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        Result result = new StreamResult(writer);
        Source source = new DOMSource(root);
        transformer.transform(source, result);
        writer.close();

        String xml = writer.toString();
        //exchange.getIn().setBody(xml);
        
        exchange.getOut().setBody(xml);
		
		/* **************************************
		 * This version is like a properties file
		StringBuilder sb = new StringBuilder();
		
		// Add the id
		sb.append("txid: "+exchange.getExchangeId()+"\n");
		
		// Add the headers
		for(String head : this.headersToInclude){
			Object value = exchange.getIn().getHeader(head);
			if(value == null){
				value = "null";
			}
			sb.append(head+": "+value+"\n");
		}
		
		// Add the TDR properties that have been added along the way
		for(String key : tdrProps.keySet()){
			Object value = tdrProps.get(key);
			if(value == null){
				value = "null";
			}
			sb.append(key+": "+(value.toString())+"\n");
		}
		
		exchange.getIn().setBody(sb.toString());
		*/
        if(logger.isDebugEnabled()){
        	logger.debug("TDR: \n"+xml);
        	//logger.debug(root.toString());
        }
	}

}
