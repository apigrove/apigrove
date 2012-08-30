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

import org.slf4j.Logger;

import com.alu.e3.common.camel.ExchangeConstantKeys;
import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;
import com.alu.e3.prov.restapi.ExchangeData;

/**
 * <p>
 * A utility class for formatting and standardizing the log messages for provisioning events.</p>
 * <p>
 * The original version of this class provided variants on a <code>log</code> function with
 * an <code>org.slf4j.Logger</code> as the first parameter.  This <code>logger</code> parameter
 * was ignored, however, and instead this class's static <code>Logger</code> instance (named
 * &ldquo;e3provLogger&rdquo;) was used to output all messages.</p>
 * <p>
 * To make this class more compatible with the idea of logging categories introduced
 * by <code>com.alu.e3.common.logging.CategoryLogger</code>, the internal static 
 * <code>Logger</code> has been changed to a <code>CategoryLogger</code> (with
 * category <code>PROV</code>), and the <code>logger</code> parameter is no longer ignored.  
 * If the <code>logger</code> is not <code>null</code>, then that <code>logger</code> will 
 * be used to output (and categorize) the formatted message.  If <code>logger</code> is null, 
 * this class's <code>logger</code> will be used instead, which is the equivalent of the 
 * original operation.</p>
 */
public class LogUtil {
	public static final String LOG_PATTERN_MODEL = "[ExchangeID: {}];[Action: {}];[API_ID: {}];[Mode: {}];[Encoded: {}];[ProvID: {}];[Phase: {}];[State: {}]";
	public static final String LOG_PATTERN = "{};{};{};{};{};{};{};{}";
	
	// Keep original (non-class) name for logger?
	private static final CategoryLogger LOG = CategoryLoggerFactory.getLogger("e3provLogger", Category.PROV);
	
	public static final void log(Logger logger, ExchangeData exchange, Phase phase, State status) 
	{
		log(logger, exchange, phase.getLabel(), status.getLabel());
	}

	public static final void log(Logger logger, ExchangeData exchange, Phase phase, String status) 
	{
		log(logger, exchange, phase.getLabel(), status);
	}

	public static final void log(Logger logger, ExchangeData exchange, String phase, String status) 
	{
		String action = (String)exchange.getProperty(ExchangeConstantKeys.E3_OPERATION_NAME.toString());
		String apiID = (String)exchange.getProperty(ExchangeConstantKeys.E3_API_ID.toString());
		String creationMode = (String)exchange.getProperty(ExchangeConstantKeys.E3_API_ID_CREATION_MODE.toString());
		String encodedApiID = (String)exchange.getProperty(ExchangeConstantKeys.E3_API_ID_ENCODED.toString());
		String provID = (String)exchange.getProperty(ExchangeConstantKeys.E3_PROVISION_ID.toString());

		if (phase == null)
			phase = "";
		if (status == null)
			status = "";

		if (logger != null) {
			logger.debug(LOG_PATTERN, new Object[] { provID, action, apiID, creationMode, encodedApiID, provID, phase, status });
		} else {
			LOG.debug(LOG_PATTERN, new Object[] { provID, action, apiID, creationMode, encodedApiID, provID, phase, status });
		}
	}

	public enum Phase {

		PREPROCESS("PREPROCESS"),

		POSTPROCESS("POSTPROCESS"),

		PROCESS("PROCESS"),

		DEPLOY("DEPLOY"),
		
		UNDEPLOY("UNDEPLOY"),
		
		GET("GET"),
		
		GETALL("GETALL"),
		
		UNKNOWN("-");

		private String label;

		Phase(String label) {

			this.label = label;
		}

		public String getLabel() {
			return label;
		}

	}

	public enum State {

		RECEIVED("RECEIVED"),

		START("START"),

		INPROGRESS("INPROGRESS"),

		DONE("DONE"),

		FAILED("FAILED"),

		UNKNOWN("-");

		private String label;

		State(String label) {

			this.label = label;
		}

		public String getLabel() {
			return label;
		}

	}
}
