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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.ParseException;

import org.junit.Test;

import com.alu.e3.common.tools.WsseTools.WsseUsernameToken;

public class WsseToolsTest {

	private static final String WSSE_XML_REQUEST = ""
            + "<?xml version='1.0' encoding='UTF-8'?>\n"
            + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">\n"
            + "    <soapenv:Header>\n"
            + "        <wsse:Security soapenv:mustUnderstand=\"true\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">\n"
            + "             <wsse:UsernameToken wsu:Id=\"UsernameToken-1\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\n"
            + "                 <wsse:Username>wally</wsse:Username>\n"
            + "                 <wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">c3dvcmRmaXNo</wsse:Password>\n"
            + "                 <wsse:Nonce EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">0123456789</wsse:Nonce>\n"
            + "                 <wsu:Created>2012-03-27T01:23:45Z</wsu:Created>\n"
            + "             </wsse:UsernameToken>\n"
            + "         </wsse:Security>\n"
            + "    </soapenv:Header>\n"
            + "    <soapenv:Body>\n"
            + "        <loc:NewOperation xmlns:loc=\"http://www.example.org/SimpleService/\">\n"
            + "            <in>My Simple Service Request</in>\n"
            + "        </loc:NewOperation>\n"
            + "    </soapenv:Body>\n"
            + "</soapenv:Envelope>\n"
            + "";
	
	private static final String WSSE_XML_PASSWORDTEXT_REQUEST = ""
            + "<?xml version='1.0' encoding='UTF-8'?>\n"
            + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">\n"
            + "    <soapenv:Header>\n"
            + "        <wsse:Security soapenv:mustUnderstand=\"true\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">\n"
            + "             <wsse:UsernameToken wsu:Id=\"UsernameToken-1\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\n"
            + "                 <wsse:Username>wally</wsse:Username>\n"
            + "                 <wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">c3dvcmRmaXNo</wsse:Password>\n"
            + "                 <wsse:Nonce EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">0123456789</wsse:Nonce>\n"
            + "                 <wsu:Created>2012-03-27T01:23:45Z</wsu:Created>\n"
            + "             </wsse:UsernameToken>\n"
            + "         </wsse:Security>\n"
            + "    </soapenv:Header>\n"
            + "    <soapenv:Body>\n"
            + "        <loc:NewOperation xmlns:loc=\"http://www.example.org/SimpleService/\">\n"
            + "            <in>My Simple Service Request</in>\n"
            + "        </loc:NewOperation>\n"
            + "    </soapenv:Body>\n"
            + "</soapenv:Envelope>\n"
            + "";

	private static final String BAD_WSSE_XML_REQUEST = ""
            + "<?xml version='1.0' encoding='UTF-8'?>\n"
            + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">\n"
            + "    <soapenv:Header>\n"
            + "        <wsse:Security soapenv:mustUnderstand=\"true\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">\n"
            + "             <wsse:UsernameToken wsu:Id=\"UsernameToken-1\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\n"
            + "                 <wsse:Username>wally</wsse:Username>\n"
            + "                 <wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">c3dvcmRmaXNo</wsse:Password>\n"
            + "                 <wsse:Nonce EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">0123456789</wsse:Nonce>\n"
            + "                 <wsu:Created>2012-03-27T01:23:45Z</wsu:Created>\n"
// missing  + "             </wsse:UsernameToken>\n"
            + "         </wsse:Security>\n"
            + "    </soapenv:Header>\n"
            + "    <soapenv:Body>\n"
            + "        <loc:NewOperation xmlns:loc=\"http://www.example.org/SimpleService/\">\n"
            + "            <in>My Simple Service Request</in>\n"
            + "        </loc:NewOperation>\n"
            + "    </soapenv:Body>\n"
            + "</soapenv:Envelope>\n"
            + "";

	private static final String BAD2_WSSE_XML_REQUEST = ""
            + "<?xml version='1.0' encoding='UTF-8'?>\n"
            + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">\n"
            + "    <soapenv:Header>\n"
            + "        <wsse:Security soapenv:mustUnderstand=\"true\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">\n"
            + "             <wsse:UsernameToken wsu:Id=\"UsernameToken-1\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\n"
            + "                 <wsse:Username>wally</wsse:Username>\n"
            + "                 <wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">c3dvcmRmaXNo</wsse:Password>\n"
            + "                 <wsse:Nonce EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">0123456789</wsse:Nonce>\n"
            + "                 <wsu:Created>2012-03-27T01:23:45Z</wsu:Created>\n"
/* extra */ + "                 <wsse:Nonce EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">0123456789</wsse:Nonce>\n"
            + "             </wsse:UsernameToken>\n"
            + "         </wsse:Security>\n"
            + "    </soapenv:Header>\n"
            + "    <soapenv:Body>\n"
            + "        <loc:NewOperation xmlns:loc=\"http://www.example.org/SimpleService/\">\n"
            + "            <in>My Simple Service Request</in>\n"
            + "        </loc:NewOperation>\n"
            + "    </soapenv:Body>\n"
            + "</soapenv:Envelope>\n"
            + "";

	@Test
	public void testXmlInput() throws Exception {
		InputStream in = new ByteArrayInputStream(WSSE_XML_REQUEST.getBytes());
		WsseUsernameToken wsseUsernameToken = WsseTools.parseXml(in);
		assertNotNull(wsseUsernameToken);
		assertEquals("wally", wsseUsernameToken.getUsername());
		assertEquals("c3dvcmRmaXNo", wsseUsernameToken.getPassword());
		assertEquals("0123456789", wsseUsernameToken.getNonce());
		assertEquals("2012-03-27T01:23:45Z", wsseUsernameToken.getCreated());
		assertFalse(wsseUsernameToken.isPasswordText());
	}
	
	@Test
	public void testXmlInputPasswordText() throws Exception {
		InputStream in = new ByteArrayInputStream(WSSE_XML_PASSWORDTEXT_REQUEST.getBytes());
		WsseUsernameToken wsseUsernameToken = WsseTools.parseXml(in);
		assertNotNull(wsseUsernameToken);
		assertEquals("wally", wsseUsernameToken.getUsername());
		assertEquals("c3dvcmRmaXNo", wsseUsernameToken.getPassword());
		assertEquals("0123456789", wsseUsernameToken.getNonce());
		assertEquals("2012-03-27T01:23:45Z", wsseUsernameToken.getCreated());
		assertTrue(wsseUsernameToken.isPasswordText());
	}

	@Test
	public void testBadXmlInput() throws ParseException {
		InputStream in = new ByteArrayInputStream(BAD_WSSE_XML_REQUEST.getBytes());
		try {
			WsseTools.parseXml(in);
			fail("Expected parse exception (bad XML), but it didn't happen.");
		} catch (Exception e) {
		}
	}

	@Test
	public void testRedundantElement() throws ParseException {
		InputStream in = new ByteArrayInputStream(BAD2_WSSE_XML_REQUEST.getBytes());
		try {
			WsseTools.parseXml(in);
			fail("Expected parse exception (redundant element), but it didn't happen.");
		} catch (Exception e) {
		}
	}
}
