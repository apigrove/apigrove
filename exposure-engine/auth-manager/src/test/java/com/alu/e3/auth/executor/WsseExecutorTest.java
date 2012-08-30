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
package com.alu.e3.auth.executor;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import com.alu.e3.auth.MockAuthDataAccess;
import com.alu.e3.common.camel.AuthReport;
import com.alu.e3.common.tools.WsseTools;

public class WsseExecutorTest {

	private static SecureRandom random;

	static {
		try {
			random = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException();   // missing algorithm -- should not happen
		}
	}

	final CamelContext context = new DefaultCamelContext();
	
	private static final String XML = ""
	        + "<wsse:Security xmlns:wsse='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd'" +
	        "                 xmlns:wsu='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd'>\n"
            + "    <wsse:UsernameToken>\n"
            + "        <wsse:Username>wally</wsse:Username>\n"
            + "        <wsse:Password Type=\"...#PasswordDigest\">weYI3nXd8LjMNVksCKFV8t3rgHh3Rw==</wsse:Password>\n"
            + "        <wsse:Nonce>WScqanjCEAC4mQoBE07sAQ==</wsse:Nonce>\n"
            + "        <wsu:Created>2012-07-19T01:24:32Z</wsu:Created>\n"
            + "    </wsse:UsernameToken>\n"
            + "</wsse:Security>\n"
            ;

	@Test
	public void testWin() {
		Exchange exchange = new DefaultExchange(context);
		
		// Setting the username = "win" should succeed
		exchange.getIn().setBody(XML);
		WsseExecutor executor = new WsseExecutor("123", new MockAuthDataAccess(null, "wally:", null));
		
		AuthReport authReport = executor.checkAllowed(exchange);		

		assertNotNull("This authentication should have succeeded", authReport.getAuthIdentity());
	}

	public static String asXml(String username, byte[] passwordBytes) {
		return asXml(username, passwordBytes, null, null);
	}
		
	public static String asXml(String username, byte[] passwordBytes, byte[] nonceBytes, Date when) {
		if (nonceBytes == null) {
			nonceBytes = new byte[10];
			random.nextBytes(nonceBytes);
		}
		if (when == null) {
			when = new Date();
		}
		String nonce = WsseTools.getNonce(nonceBytes);
		String created = WsseTools.getCreated(when);
		String passwordDigest = WsseTools.getPasswordDigest(WsseTools.getPasswordDigestBytes(nonce, created, passwordBytes));
		return asXml(username, passwordDigest, nonce, created);
	}
	
	public static String asXml(String username, String passwordDigest, String nonce, String created) {
		return new StringBuilder()
		        .append("<Security>\n")
		        .append("    <Username>").append(username).append("</Username>\n")
		        .append("    <Password Type=\"...#PasswordDigest\">").append(passwordDigest).append("</Password>\n")
		        .append("    <Nonce>").append(nonce).append("</Nonce>\n")
		        .append("    <Created>").append(created).append("</Created>\n")
		        .append("</Security>\n")
		        .toString();
	}
}
