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
package com.alu.e3.common.logging;

import org.slf4j.LoggerFactory;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.Category;


/**
 * A factory class to create CategoryLogger instances.
 * <br><br>
 * Note that this class follows the org.slf4j.LoggerFactory model of
 * using the factory pattern, even though it returns a new
 * CategoryLogger with each getLogger() call.  This works as expected
 * because a CategoryLogger holds no state of its own, only an
 * slf4j Logger member and a Category enum member.  If, in the future,
 * CategoryLogger is modified to contain unique state, then this
 * factory class may have to account for that state when returning
 * Logger instances.
 */
public class CategoryLoggerFactory {

	@SuppressWarnings("rawtypes")
	public static CategoryLogger getLogger(Class cls) 
	{
		return new CategoryLogger(LoggerFactory.getLogger(cls));
	}

	@SuppressWarnings("rawtypes")
	public static CategoryLogger getLogger(Class cls, Category category) 
	{
		return new CategoryLogger(LoggerFactory.getLogger(cls), category);
	}

	public static CategoryLogger getLogger(String name) 
	{
		return new CategoryLogger(LoggerFactory.getLogger(name));
	}
	
	public static CategoryLogger getLogger(String name, Category category) 
	{
		return new CategoryLogger(LoggerFactory.getLogger(name), category);
	}

}
