<?php
/**
 * Copyright © 2012 Alcatel-Lucent.
 *
 * See the NOTICE file distributed with this work for additional information regarding copyright ownership. Licensed to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the specific language governing permissions and limitations under the License.
 */

/**
 *
 * @author Trevor Pesout <>
 * @version $Id$
 * @copyright 10/12/11 2:48 PM
 */
 
class LoggerInterface
{
    const DEBUG = 1;
    const INFO = 2;
    const NOTICE = 3;
    const WARN = 4;
    const ERROR = 5;
    const CRITICAL = 6;

    protected static $loggers = array();
    protected static $logGroups = array();
    protected static $logCount = 0;

    public static function log($message, $logLevel = LoggerInterface::INFO, $logGroups = null) {
        //initialize with the 'all' loggers
        $loggerIds = (isset(LoggerInterface::$logGroups[null]) ? LoggerInterface::$logGroups[null] : array());

        //add all loggers in 'logGroups'
        if (is_array($logGroups)) {
            foreach ($logGroups as $logGroup) {
                foreach (LoggerInterface::$logGroups[$logGroup] as $logId) {
                    $loggerIds[$logId] = $logId;
                }
            }
        } elseif ($logGroups !== null) {
            foreach (LoggerInterface::$logGroups[$logGroups] as $logId) {
                $loggerIds[$logId] = $logId;
            }
        }

        //log to all (unique'd) loggers
        foreach ($loggerIds as $logId) {
            LoggerInterface::$loggers[$logId]->log($message, $logLevel);
        }
    }

    public static function logException(Exception $e, $logLevel = null, $logGroup = null) {
        if ($e instanceof POException) {
            if ($logLevel == null) $logLevel = $e->getLogLevel();
            if ($logGroup == null) $logGroup = $e->getLogGroups();
        }

        if ($logLevel == null) $logLevel = LoggerInterface::ERROR;

        LoggerInterface::log($e->getMessage() . '\n' . $e->getTraceAsString(), $logLevel, $logGroup);
    }

    public static function logError($errno, $errstr, $errfile = '', $errline = '', $errcontext = null) {
        $poErrorLevel = LoggerInterface::WARN;
        $errnostr = "Unknown";

        switch ($errno) {
            case E_ERROR:
                $errnostr = 'Error';
                $poErrorLevel = LoggerInterface::ERROR;
                break;
            case E_WARNING:
                $errnostr = 'Warning';
                $poErrorLevel = LoggerInterface::WARN;
                break;
            case E_PARSE:
                $errnostr = 'Parsing Error';
                $poErrorLevel = LoggerInterface::ERROR;
                break;
            case E_NOTICE:
                $errnostr = 'Notice';
                $poErrorLevel = LoggerInterface::NOTICE;
                break;
            case E_CORE_ERROR:
                $errnostr = 'Core Error';
                $poErrorLevel = LoggerInterface::CRITICAL;
                break;
            case E_CORE_WARNING:
                $errnostr = 'Core Warning';
                $poErrorLevel = LoggerInterface::ERROR;
                break;
            case E_COMPILE_ERROR:
                $errnostr = 'Compile Error';
                $poErrorLevel = LoggerInterface::CRITICAL;
                break;
            case E_COMPILE_WARNING:
                $errnostr = 'Compile Warning';
                $poErrorLevel = LoggerInterface::ERROR;
                break;
            case E_USER_ERROR:
                $errnostr = 'User Error';
                $poErrorLevel = LoggerInterface::ERROR;
                break;
            case E_USER_WARNING:
                $errnostr = 'User Warning';
                $poErrorLevel = LoggerInterface::WARN;
                break;
            case E_USER_NOTICE:
                $errnostr = 'User Notice';
                $poErrorLevel = LoggerInterface::NOTICE;
                break;
            case E_STRICT:
                $errnostr = 'Runtime Notice';
                $poErrorLevel = LoggerInterface::DEBUG;
                break;
            case E_RECOVERABLE_ERROR:
                $errnostr = 'Catchable Fatal Error';
                $poErrorLevel = LoggerInterface::ERROR;
                break;
        }

        LoggerInterface::log("$errnostr: $errfile($errline): $errstr", $poErrorLevel);
    }

    /*
     * Adding a logger specifying a group means that all logged messages with that groupID
     * will be logged.  Specifying no group means that all messages will be logged
     * regardless of the message's group.
     */
    public static function addLogger($logger, $group = null) {
        LoggerInterface::$logCount += 1;
        LoggerInterface::$loggers[LoggerInterface::$logCount] = $logger;
        if (is_array($group)) {
            foreach ($group as $g) {
                LoggerInterface::$logGroups[$g][LoggerInterface::$logCount] = LoggerInterface::$logCount;
            }
        } else {
            LoggerInterface::$logGroups[$group][LoggerInterface::$logCount] = LoggerInterface::$logCount;
        }
    }

    public static function logLevelString($logLevel) {
        switch ($logLevel) {
            case 0:
                return "NONE";
            case LoggerInterface::INFO:
                return "INFO";
            case LoggerInterface::DEBUG:
                return "DEBUG";
            case LoggerInterface::NOTICE:
                return "NOTICE";
            case LoggerInterface::WARN:
                return "WARN";
            case LoggerInterface::ERROR:
                return "ERROR";
            case LoggerInterface::CRITICAL:
                return "CRITICAL";
        }
        return "";
    }
    public static function logLevelStringToInteger($logLevel) {
        switch ($logLevel) {
            case "NONE":
                return 'NONE';
            case "INFO":
                return LoggerInterface::INFO;
            case "DEBUG":
                return LoggerInterface::DEBUG;
            case "NOTICE":
                return LoggerInterface::NOTICE;
            case "WARN":
                return LoggerInterface::WARN;
            case "ERROR":
                return LoggerInterface::ERROR;
            case "CRITICAL":
                return LoggerInterface::CRITICAL;
        }
        return false;
    }

    public static function getSelectArray($include_none = true) {
        if ($include_none) {
            return array(
                'NONE' => 0,
                'DEBUG' => LoggerInterface::DEBUG,
                'INFO' => LoggerInterface::INFO,
                'NOTICE' => LoggerInterface::NOTICE,
                'WARN' => LoggerInterface::WARN,
                'ERROR' => LoggerInterface::ERROR,
                'CRITICAL' => LoggerInterface::CRITICAL,
            );
        }
        return array(
            'DEBUG' => LoggerInterface::DEBUG,
            'INFO' => LoggerInterface::INFO,
            'NOTICE' => LoggerInterface::NOTICE,
            'WARN' => LoggerInterface::WARN,
            'ERROR' => LoggerInterface::ERROR,
            'CRITICAL' => LoggerInterface::CRITICAL,
        );
    }

}
