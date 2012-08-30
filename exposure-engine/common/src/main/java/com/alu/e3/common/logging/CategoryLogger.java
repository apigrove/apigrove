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

import org.slf4j.Logger;
import org.slf4j.Marker;

import com.alu.e3.common.logging.Category;

/**
 * An org.slf4j.Logger implementation (and wrapper) that provides
 * all the normal slf4j logging methods along with variants
 * that take a Category parameter to classify log messages.
 */
public class CategoryLogger implements org.slf4j.Logger {
	
	private final Logger logger;
	private final Category category;
		

	CategoryLogger(Logger logger) 
	{
		this.logger = logger;
		this.category = null;
	}
	
	CategoryLogger(Logger logger, Category category) 
	{
		this.logger = logger;
		this.category = category;
	}
	
	/**
	 * The method for &ldquo;tagging&rdquo; logging messages for possible filtering.
	 * Currently, if a message is categorized, then the message text is prepended
	 * with the Category name enclosed in brackets ([]).  
	 *  
	 * @param category	The Category to apply
	 * @param msg	The original log message
	 * @return	The tagged log message
	 */
	static private String categorizedMessage(Category category, String msg)
	{
		return category != null ? "[" + category.name() + "] " + msg : msg;
	}
	

	@Override
	public void debug(String msg) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.debug(categorizedMessage(this.category, msg));
		}
	}
	
	@Override
	public void debug(String format, Object arg) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.debug(categorizedMessage(this.category, format), arg);
		}
	}

	@Override
	public void debug(String format, Object[] argArray) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.debug(categorizedMessage(this.category, format), argArray);
		}
	}

	@Override
	public void debug(String msg, Throwable t) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.debug(categorizedMessage(this.category, msg), t);
		}
	}

	@Override
	public void debug(Marker marker, String msg) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.debug(marker, categorizedMessage(this.category, msg));	
		}
	}
	
	@Override
	public void debug(String format, Object arg1, Object arg2) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.debug(categorizedMessage(this.category, format), arg1, arg2);
		}
	}

	@Override
	public void debug(Marker marker, String format, Object arg) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.debug(marker, categorizedMessage(this.category, format), arg);	
		}
	}

	@Override
	public void debug(Marker marker, String format, Object[] argArray) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.debug(marker, categorizedMessage(this.category, format), argArray);
		}
	}

	@Override
	public void debug(Marker marker, String msg, Throwable t) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.debug(marker, categorizedMessage(this.category, msg), t);
		}
	}

	@Override
	public void debug(Marker marker, String format, Object arg1, Object arg2) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.debug(marker, categorizedMessage(this.category, format), arg1, arg2);
		}
	}

	public void debug(Category category, String msg) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.debug(categorizedMessage(category, msg));
		}
	}
	
	public void debug(Category category, String format, Object arg) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.debug(categorizedMessage(category, format), arg);
		}
	}

	public void debug(Category category, String format, Object[] argArray) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.debug(categorizedMessage(category, format), argArray);
		}
	}

	public void debug(Category category, String msg, Throwable t) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.debug(categorizedMessage(category, msg), t);
		}
	}

	public void debug(Category category, Marker marker, String msg) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.debug(marker, categorizedMessage(category, msg));	
		}
	}
	
	public void debug(Category category, String format, Object arg1, Object arg2) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.debug(categorizedMessage(category, format), arg1, arg2);
		}
	}

	public void debug(Category category, Marker marker, String format, Object arg) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.debug(marker, categorizedMessage(category, format), arg);	
		}
	}

	public void debug(Category category, Marker marker, String format, Object[] argArray) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.debug(marker, categorizedMessage(category, format), argArray);
		}
	}

	public void debug(Category category, Marker marker, String msg, Throwable t) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.debug(marker, categorizedMessage(category, msg), t);
		}
	}

	public void debug(Category category, Marker marker, String format, Object arg1, Object arg2) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.debug(marker, categorizedMessage(category, format), arg1, arg2);
		}
	}

	@Override
	public void error(String msg) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.error(categorizedMessage(this.category, msg));
		}
	}

	@Override
	public void error(String format, Object arg) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.error(categorizedMessage(this.category, format), arg);
		}
	}

	@Override
	public void error(String format, Object[] argArray) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.error(categorizedMessage(this.category, format), argArray);
		}
	}

	@Override
	public void error(String msg, Throwable t) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.error(categorizedMessage(this.category, msg), t);
		}		
	}

	@Override
	public void error(Marker marker, String msg) 
	{		
		if (Category.isCategoryEnabled(this.category)) {
			logger.error(marker, categorizedMessage(this.category, msg));
		}
	}

	@Override
	public void error(String format, Object arg1, Object arg2) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.error(categorizedMessage(this.category, format), arg1, arg2);	
		}
	}

	@Override
	public void error(Marker marker, String format, Object arg) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.error(marker, categorizedMessage(this.category, format), arg);
		}
	}

	@Override
	public void error(Marker marker, String format, Object[] argArray) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.error(marker, categorizedMessage(this.category, format), argArray);	
		}
	}

	@Override
	public void error(Marker marker, String msg, Throwable t) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.error(marker, categorizedMessage(this.category, msg), t);
		}
	}

	@Override
	public void error(Marker marker, String format, Object arg1, Object arg2) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.error(marker, categorizedMessage(this.category, format), arg1, arg2);
		}
	}

	public void error(Category category, String msg) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.error(categorizedMessage(category, msg));
		}
	}

	public void error(Category category, String format, Object arg) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.error(categorizedMessage(category, format), arg);
		}
	}

	public void error(Category category, String format, Object[] argArray) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.error(categorizedMessage(category, format), argArray);
		}
	}

	public void error(Category category, String msg, Throwable t) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.error(categorizedMessage(category, msg), t);
		}		
	}

	public void error(Category category, Marker marker, String msg) 
	{		
		if (Category.isCategoryEnabled(category)) {
			logger.error(marker, categorizedMessage(category, msg));
		}
	}

	public void error(Category category, String format, Object arg1, Object arg2) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.error(categorizedMessage(category, format), arg1, arg2);	
		}
	}

	public void error(Category category, Marker marker, String format, Object arg) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.error(marker, categorizedMessage(category, format), arg);
		}
	}

	public void error(Category category, Marker marker, String format, Object[] argArray) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.error(marker, categorizedMessage(category, format), argArray);	
		}
	}

	public void error(Category category, Marker marker, String msg, Throwable t) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.error(marker, categorizedMessage(category, msg), t);
		}
	}

	public void error(Category category, Marker marker, String format, Object arg1, Object arg2) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.error(marker, categorizedMessage(category, format), arg1, arg2);
		}
	}

	@Override
	public String getName() {
		return logger.getName();
	}

	@Override
	public void info(String msg) {
		if (Category.isCategoryEnabled(this.category)) {
			logger.info(categorizedMessage(this.category, msg));
		}
	}

	@Override
	public void info(String format, Object arg) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.info(categorizedMessage(this.category, format), arg);
		}
	}

	@Override
	public void info(String format, Object[] argArray) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.info(categorizedMessage(this.category, format), argArray);
		}
	}

	@Override
	public void info(String msg, Throwable t) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.info(categorizedMessage(this.category, msg), t);
		}
	}

	@Override
	public void info(Marker marker, String msg) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.info(marker, categorizedMessage(this.category, msg));
		}
	}

	@Override
	public void info(String format, Object arg1, Object arg2)
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.info(categorizedMessage(this.category, format), arg1, arg2);	
		}
	}

	@Override
	public void info(Marker marker, String format, Object arg) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.info(marker, categorizedMessage(this.category, format), arg);
		}
	}

	@Override
	public void info(Marker marker, String format, Object[] argArray) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.info(marker, categorizedMessage(this.category, format), argArray);
		}
	}

	@Override
	public void info(Marker marker, String msg, Throwable t) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.info(marker, categorizedMessage(this.category, msg), t);	
		}
	}

	@Override
	public void info(Marker marker, String format, Object arg1, Object arg2) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.info(marker, categorizedMessage(this.category, format), arg1, arg2);	
		}
	}

	public void info(Category category, String msg) {
		if (Category.isCategoryEnabled(category)) {
			logger.info(categorizedMessage(category, msg));
		}
	}

	public void info(Category category, String format, Object arg) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.info(categorizedMessage(category, format), arg);
		}
	}

	public void info(Category category, String format, Object[] argArray) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.info(categorizedMessage(category, format), argArray);
		}
	}

	public void info(Category category, String msg, Throwable t) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.info(categorizedMessage(category, msg), t);
		}
	}

	public void info(Category category, Marker marker, String msg) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.info(marker, categorizedMessage(category, msg));
		}
	}

	public void info(Category category, String format, Object arg1, Object arg2)
	{
		if (Category.isCategoryEnabled(category)) {
			logger.info(categorizedMessage(category, format), arg1, arg2);	
		}
	}

	public void info(Category category, Marker marker, String format, Object arg) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.info(marker, categorizedMessage(category, format), arg);
		}
	}

	public void info(Category category, Marker marker, String format, Object[] argArray) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.info(marker, categorizedMessage(category, format), argArray);
		}
	}

	public void info(Category category, Marker marker, String msg, Throwable t) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.info(marker, categorizedMessage(category, msg), t);	
		}
	}

	public void info(Category category, Marker marker, String format, Object arg1, Object arg2) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.info(marker, categorizedMessage(category, format), arg1, arg2);	
		}
	}
	
	@Override
	public boolean isDebugEnabled() 
	{
		return (Category.isCategoryEnabled(this.category) && logger.isDebugEnabled());
	}

	@Override
	public boolean isDebugEnabled(Marker marker) 
	{
		return (Category.isCategoryEnabled(this.category) && logger.isDebugEnabled(marker));
	}

	@Override
	public boolean isErrorEnabled() 
	{
		return (Category.isCategoryEnabled(this.category) && logger.isErrorEnabled());
	}

	@Override
	public boolean isErrorEnabled(Marker marker) 
	{
		return (Category.isCategoryEnabled(this.category) && logger.isErrorEnabled(marker));
	}

	@Override
	public boolean isInfoEnabled() 
	{
		return (Category.isCategoryEnabled(this.category) && logger.isInfoEnabled());
	}

	@Override
	public boolean isInfoEnabled(Marker marker) 
	{
		return (Category.isCategoryEnabled(this.category) && logger.isInfoEnabled(marker));
	}

	@Override
	public boolean isTraceEnabled()  
	{
		return (Category.isCategoryEnabled(this.category) && logger.isTraceEnabled());
	}

	@Override
	public boolean isTraceEnabled(Marker marker) 
	{
		return (Category.isCategoryEnabled(this.category) && logger.isTraceEnabled(marker));
	}

	@Override
	public boolean isWarnEnabled() 
	{
		return (Category.isCategoryEnabled(this.category) && logger.isWarnEnabled());
	}

	@Override
	public boolean isWarnEnabled(Marker marker) 
	{
		return (Category.isCategoryEnabled(this.category) && logger.isWarnEnabled(marker));
	}

	@Override
	public void trace(String msg) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.trace(categorizedMessage(this.category, msg));
		}
	}

	@Override
	public void trace(String format, Object arg) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.trace(categorizedMessage(this.category, format), arg);
		}
	}

	@Override
	public void trace(String format, Object[] argArray) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.trace(categorizedMessage(this.category, format), argArray);	
		}
	}

	@Override
	public void trace(String msg, Throwable t) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.trace(categorizedMessage(this.category, msg), t);
		}
	}

	@Override
	public void trace(Marker marker, String msg) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.trace(marker, categorizedMessage(this.category, msg));
		}
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.trace(categorizedMessage(this.category, format), arg1, arg2);
		}
	}

	@Override
	public void trace(Marker marker, String format, Object arg) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.trace(marker, categorizedMessage(this.category, format), arg);
		}
	}

	@Override
	public void trace(Marker marker, String format, Object[] argArray) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.trace(marker, categorizedMessage(this.category, format), argArray);
		}
	}

	@Override
	public void trace(Marker marker, String msg, Throwable t) {
		if (Category.isCategoryEnabled(this.category)) {
			logger.trace(marker, categorizedMessage(this.category, msg), t);
		}
	}

	@Override
	public void trace(Marker marker, String format, Object arg1, Object arg2) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.trace(marker, categorizedMessage(this.category, format), arg1, arg2);
		}
	}

	public void trace(Category category, String msg) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.trace(categorizedMessage(category, msg));
		}
	}

	public void trace(Category category, String format, Object arg) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.trace(categorizedMessage(category, format), arg);
		}
	}

	public void trace(Category category, String format, Object[] argArray) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.trace(categorizedMessage(category, format), argArray);	
		}
	}

	public void trace(Category category, String msg, Throwable t) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.trace(categorizedMessage(category, msg), t);
		}
	}

	public void trace(Category category, Marker marker, String msg) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.trace(marker, categorizedMessage(category, msg));
		}
	}

	public void trace(Category category, String format, Object arg1, Object arg2) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.trace(categorizedMessage(category, format), arg1, arg2);
		}
	}

	public void trace(Category category, Marker marker, String format, Object arg) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.trace(marker, categorizedMessage(category, format), arg);
		}
	}

	public void trace(Category category, Marker marker, String format, Object[] argArray) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.trace(marker, categorizedMessage(category, format), argArray);
		}
	}

	public void trace(Category category, Marker marker, String msg, Throwable t) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.trace(marker, categorizedMessage(category, msg), t);
		}
	}

	public void trace(Category category, Marker marker, String format, Object arg1, Object arg2) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.trace(marker, categorizedMessage(category, format), arg1, arg2);
		}
	}

	@Override
	public void warn(String msg) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.warn(categorizedMessage(this.category, msg));
		}
	}

	@Override
	public void warn(String format, Object arg) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.warn(categorizedMessage(this.category, format), arg);
		}
	}

	@Override
	public void warn(String format, Object[] argArray) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.warn(categorizedMessage(this.category, format), argArray);
		}
	}

	@Override
	public void warn(String msg, Throwable t) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.warn(categorizedMessage(this.category, msg), t);
		}
	}

	@Override
	public void warn(Marker marker, String msg) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.warn(marker, categorizedMessage(this.category, msg));
		}
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.warn(categorizedMessage(this.category, format), arg1, arg2);
		}
	}

	@Override
	public void warn(Marker marker, String format, Object arg) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.warn(marker, categorizedMessage(this.category, format), arg);
		}
	}

	@Override
	public void warn(Marker marker, String format, Object[] argArray) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.warn(marker, categorizedMessage(this.category, format), argArray);
		}
	}

	@Override
	public void warn(Marker marker, String msg, Throwable t) 
	{
		if (Category.isCategoryEnabled(this.category)) {
			logger.warn(marker, categorizedMessage(this.category, msg), t);
		}
	}

	@Override
	public void warn(Marker marker, String format, Object arg1, Object arg2) {
		if (Category.isCategoryEnabled(this.category)) {
			logger.warn(marker, categorizedMessage(this.category, format), arg1, arg2);
		}
	}
	
	public void warn(Category category, String msg) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.warn(categorizedMessage(category, msg));
		}
	}

	public void warn(Category category, String format, Object arg) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.warn(categorizedMessage(category, format), arg);
		}
	}

	public void warn(Category category, String format, Object[] argArray) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.warn(categorizedMessage(category, format), argArray);
		}
	}

	public void warn(Category category, String msg, Throwable t) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.warn(categorizedMessage(category, msg), t);
		}
	}

	public void warn(Category category, Marker marker, String msg) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.warn(marker, categorizedMessage(category, msg));
		}
	}

	public void warn(Category category, String format, Object arg1, Object arg2) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.warn(categorizedMessage(category, format), arg1, arg2);
		}
	}

	public void warn(Category category, Marker marker, String format, Object arg) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.warn(marker, categorizedMessage(category, format), arg);
		}
	}

	public void warn(Category category, Marker marker, String format, Object[] argArray) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.warn(marker, categorizedMessage(category, format), argArray);
		}
	}

	public void warn(Category category, Marker marker, String msg, Throwable t) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.warn(marker, categorizedMessage(category, msg), t);
		}
	}

	public void warn(Category category, Marker marker, String format, Object arg1, Object arg2) 
	{
		if (Category.isCategoryEnabled(category)) {
			logger.warn(marker, categorizedMessage(category, format), arg1, arg2);
		}
	}
}
