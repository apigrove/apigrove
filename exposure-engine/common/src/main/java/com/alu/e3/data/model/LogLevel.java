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
package com.alu.e3.data.model;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.log4j.Level;

/**
 *  Data structure for holding java log levels, using log4j conventions
 *  and wrapping the log4j Level class.
 * 
 *  Note:
 *  
 *  Be careful when using the LogLevel(String levelString) constructor, as
 *  it will return the default LogLevel if levelString cannot be parsed
 *  as a valid log4j level value.  Use the static function
 *  isValidLogLevel(String levelString) to check for valid values first
 *  if necessary.
 * 
 *
 */

public class LogLevel implements Serializable {

	private static final long serialVersionUID = -2322396140902593928L;
	public static final Level defaultLevel = Level.INFO;
	
	// Keys for logLevel table in DataManager
	public static final String logLevelKey = "logLevel";
	public static final String smxlogLevelKey = "smxlogLevel";
	public static final String syslogLevelKey = "syslogLevel";
	public static final String logLevelKeyParamSeparator = ":";

	private Level level;
	private SyslogLevel syslogLevel;

	public LogLevel(Level level) 
	{
		if (level == null) {
			level = defaultLevel;
		}
		this.level = level;
		this.syslogLevel = SyslogLevel.valueOf(this.level.getSyslogEquivalent());
	}
		
	/**
	 * LogLevel constructor from a log4j or syslog level string.
	 * 
	 * Note that an invalid level string will not throw an exception,
	 * but will instead return the default level value.
	 * Use the static function isValidLogLevel(String levelString)
	 * or isValidSyslogLevel(String levelString)
	 * to check string values first.
	 */
	public LogLevel(String levelString) 
	{
		// Always check log4j levels first, because the mapping from log4j
		// level to syslog level is not one-to-one
		if (LogLevel.isValidLogLevel(levelString)) {
			this.level = Level.toLevel(levelString, defaultLevel);
			this.syslogLevel = SyslogLevel.valueOf(this.level.getSyslogEquivalent());
		} else if (LogLevel.isValidSyslogLevel(levelString)) {
			this.syslogLevel = Enum.valueOf(SyslogLevel.class, levelString);
			Level equivLevel = this.syslogLevel.getLevelEquivalent();
			this.level = equivLevel != null ? equivLevel : defaultLevel;	// does not try to find nearest match!
		} else {
			this.level = defaultLevel;
			this.syslogLevel = SyslogLevel.valueOf(this.level.getSyslogEquivalent());
		}
		
		this.level = Level.toLevel(levelString, defaultLevel);
	}
	
	public Level getLevel()
	{
		return level;
	}

	public SyslogLevel getSyslogLevel()
	{
		return syslogLevel;
	}

	@Override
	public final String toString() 
	{
		return level.toString();
	}
	
	@Override 
	public boolean equals(Object o) 
	{
		if (!(o instanceof LogLevel)) {
			return false;
		}
		LogLevel that = (LogLevel)o;
		return this.level.equals(that.level);
	}
	
	@Override
	public int hashCode() 
	{
		return level.hashCode();
	}
	
		
	public enum Log4JLevel {
		
		OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE, ALL;
		
		public static final String valuesList = Arrays.asList(LogLevel.Log4JLevel.values()).toString();
		public static final String patternString = "(?i)(" + valuesList.replaceAll("(^\\[|\\]$)", "").replace(", ", "|") + ")(?-i)";
	}
	
	public enum SyslogLevel {
		
		EMERG(0), ALERT(1), CRIT(2), ERR(3), WARNING(4), NOTICE(5), INFO(6), DEBUG(7);
		
		private final int intValue;
		SyslogLevel(int intValue) {this.intValue = intValue; }
		public int intValue() { return this.intValue; }
		public static final String valuesList = Arrays.asList(LogLevel.SyslogLevel.values()).toString();
		public static final String patternString = "(?i)(" + valuesList.replaceAll("(^\\[|\\]$)", "").replace(", ", "|") + ")(?-i)";
	
		// Also define severity level strings that are valid in syslog.conf rules
		public static final String configPatternString = "(?i)(panic|emerg|alert|crit|error|err|warning|warn|notice|info|debug|\\*)(?-i)";
	
		public static SyslogLevel valueOf(int i) 
		{
			for (SyslogLevel level : SyslogLevel.values()) {
				if (level.intValue == i) {
					return level;
				}
			}
			return null;
		}
		
		/**
		 * Returns the equivalent (log4j) Level for a syslog-level object.  Note that the
		 * mapping the Level.getSyslogEquivalent() function is not one-to-one,
		 * so the Level returned may be one of several possible equivalents.
		 * 
		 * @return	The equivalent Level for this SyslogLevel, or <code>null</code> if no equivalent found
		 */
		public Level getLevelEquivalent()
		{
			for (Log4JLevel log4jlevel : Log4JLevel.values()) {
				Level level = Level.toLevel(log4jlevel.name());
				if (level.getSyslogEquivalent() == this.intValue) {
					return level;
				}
			}
			return null;
		}
	}

	
	public static boolean isValidLogLevel(String levelString) 
	{
		return ((levelString != null) && levelString.matches(Log4JLevel.patternString));
	}
	
	public static boolean isValidSyslogLevel(String levelString) 
	{
		return ((levelString != null) && levelString.matches(SyslogLevel.patternString));
	}
		
	// future use
	public static String logLevelKeyForIP(String ipAddress)
	{
		return LogLevel.logLevelKey + ":" + ipAddress;
	}
	
	// future use
	public static String syslogLevelKeyForIP(String ipAddress)
	{
		return LogLevel.syslogLevelKey + logLevelKeyParamSeparator + ipAddress;
	}
	
}
