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
package com.alu.e3.prov;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alu.e3.common.camel.ExchangeConstantKeys;
import com.alu.e3.prov.restapi.model.Api;
import com.alu.e3.prov.restapi.model.ApiContext;
import com.alu.e3.prov.restapi.model.ApiType;
import com.alu.e3.prov.restapi.model.AuthType;
import com.alu.e3.prov.restapi.model.Authkey;
import com.alu.e3.prov.restapi.model.DynamicTdr;
import com.alu.e3.prov.restapi.model.HTTPSType;
import com.alu.e3.prov.restapi.model.ProvisionAuthentication;
import com.alu.e3.prov.restapi.model.StaticTdr;
import com.alu.e3.prov.restapi.model.TLSMode;
import com.alu.e3.prov.restapi.model.TargetHost;
import com.alu.e3.prov.restapi.model.TdrData;
import com.alu.e3.prov.restapi.model.TdrEnabled;
import com.alu.e3.prov.restapi.model.TdrType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { 
		"classpath:/spring/provisioning.auth-template-test.xml"
		 })
public class HttpsTemplateTest {

	@EndpointInject(uri = "mock:setup")
    protected MockEndpoint setup;
	
	@EndpointInject(uri = "mock:testMessage")
    protected MockEndpoint testMessage;
	
	@Produce(uri = "direct:test")
    protected ProducerTemplate producerTemplate;
	
	@Test
	@DirtiesContext
	public void testHttpsSuccess() throws Exception {
		setup.whenAnyExchangeReceived(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				setupExchange(exchange, true);
			}
		}); 

		testMessage.setExpectedMessageCount(1);
		testMessage.allMessages().body().isNotNull();
		testMessage.allMessages().body().regex(
				// Turns on single-line mode
				"(?s)\\A.*"+
				"<from uri=\"ssljetty:https://.*/>"+
				".*\\z");

		producerTemplate.requestBody(String.class);

		testMessage.assertIsSatisfied();
	}

	@Test
	@DirtiesContext
	public void testHttpSuccess() throws Exception {
		setup.whenAnyExchangeReceived(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				setupExchange(exchange, false);
			}
		});

		testMessage.setExpectedMessageCount(1);
		testMessage.allMessages().body().isNotNull();
		testMessage.allMessages().body().regex(
				// Turns on single-line mode
				"(?s)\\A.*"+
				"<from uri=\"jetty:http://.*/>"+
				".*\\z");

		producerTemplate.requestBody(String.class);

		testMessage.assertIsSatisfied();
	}

	private void setupExchange(Exchange exchange, boolean https){
		Api api = new Api();

		api.setType(ApiType.PASS_THROUGH);

		ApiContext env = new ApiContext();
		env.setId("test");

		api.setContexts(Arrays.asList(env));

		List<TargetHost> targetList = new ArrayList<TargetHost>();

		api.setEndpoint("www.yahoo.fr");

		TargetHost to1 = new TargetHost();
		to1.setUrl("http://www.google.com");
		targetList.add(to1);

		TargetHost to2 = new TargetHost();
		to2.setUrl("http://www.google.com?toto=tutu");
		targetList.add(to2);

		TargetHost to3 = new TargetHost();
		to3.setUrl("http://www.google.com?toto=tutu&tata=tete&titi=toto");
		targetList.add(to3);

		env.setTargetHosts(targetList);

		TdrEnabled tdr = new TdrEnabled();
		tdr.setEnabled("true");
		api.setTdrEnabled(tdr);


		exchange.setProperty(ExchangeConstantKeys.E3_REQUEST_PAYLOAD.toString(), api);
		exchange.setProperty(ExchangeConstantKeys.E3_API_ID.toString(), "MyApiID");
		exchange.setProperty(ExchangeConstantKeys.E3_API_ID_ENCODED.toString(), "MonApiIDEncoded");
		exchange.setProperty(ExchangeConstantKeys.E3_PROVISION_ID.toString(), "MyProvId");

		ProvisionAuthentication auth = new ProvisionAuthentication();
		Authkey authKey = new Authkey();
		authKey.setKeyName("MyAuthkeyValue");
		auth.getAuths().add(AuthType.AUTHKEY);
		auth.setAuthKey(authKey);

		HTTPSType httpsType = new HTTPSType();
		httpsType.setEnabled(https);
		httpsType.setTlsMode(TLSMode.ONE_WAY);
		api.setHttps(httpsType);

		TdrData tdrData = new TdrData();

		TdrType tdrType = new TdrType();
		tdrType.getType().add("apiRateLimit");

		DynamicTdr dt = new DynamicTdr();
		dt.setHttpHeaderName("HTTP_HEADER");
		dt.setTdrPropName("propname");
		dt.setTypes(tdrType);

		tdrData.getDynamic().add(dt);

		StaticTdr st = new StaticTdr();
		st.setValue("staticValue");
		st.setTdrPropName("staticName");

		st.setTypes(tdrType);

		tdrData.getStatic().add(st);
		api.setTdr(tdrData);

		api.setAuthentication(auth);

	}

}
