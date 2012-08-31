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
 * Created by JetBrains PhpStorm.
 *
 * @author Trevor Pesout <>
 * @version $Id$
 * @copyright 10/12/11 2:48 PM
 */
 
class POLI
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

    public static function setAsHandler() {
        set_exception_handler(array('POLI', 'logException'));
        set_error_handler(array('POLI', 'logError'));
    }

    public static function log($message, $logLevel = POLI::INFO, $logGroups = null) {
        //initialize with the 'all' loggers
        $loggerIds = (isset(POLI::$logGroups[null]) ? POLI::$logGroups[null] : array());

        //add all loggers in 'logGroups'
        if (is_array($logGroups)) {
            foreach ($logGroups as $logGroup) {
                foreach (POLI::$logGroups[$logGroup] as $logId) {
                    $loggerIds[$logId] = $logId;
                }
            }
        } elseif ($logGroups !== null) {
            foreach (POLI::$logGroups[$logGroups] as $logId) {
                $loggerIds[$logId] = $logId;
            }
        }

        //log to all (unique'd) loggers
        foreach ($loggerIds as $logId) {
            POLI::$loggers[$logId]->log($message, $logLevel);
        }
    }

    public static function logException(Exception $e, $logLevel = null, $logGroup = null) {
        if ($e instanceof POException) {
            if ($logLevel == null) $logLevel = $e->getLogLevel();
            if ($logGroup == null) $logGroup = $e->getLogGroups();
        }

        if ($logLevel == null) $logLevel = POLI::ERROR;

        POLI::log($e->getMessage() . '\n' . $e->getTraceAsString(), $logLevel, $logGroup);
    }

    public static function logError($errno, $errstr, $errfile = '', $errline = '', $errcontext = null) {
        $poErrorLevel = POLI::WARN;
        $errnostr = "Unknown";

        switch ($errno) {
            case E_ERROR:
                $errnostr = 'Error';
                $poErrorLevel = POLI::ERROR;
                break;
            case E_WARNING:
                $errnostr = 'Warning';
                $poErrorLevel = POLI::WARN;
                break;
            case E_PARSE:
                $errnostr = 'Parsing Error';
                $poErrorLevel = POLI::ERROR;
                break;
            case E_NOTICE:
                $errnostr = 'Notice';
                $poErrorLevel = POLI::NOTICE;
                break;
            case E_CORE_ERROR:
                $errnostr = 'Core Error';
                $poErrorLevel = POLI::CRITICAL;
                break;
            case E_CORE_WARNING:
                $errnostr = 'Core Warning';
                $poErrorLevel = POLI::ERROR;
                break;
            case E_COMPILE_ERROR:
                $errnostr = 'Compile Error';
                $poErrorLevel = POLI::CRITICAL;
                break;
            case E_COMPILE_WARNING:
                $errnostr = 'Compile Warning';
                $poErrorLevel = POLI::ERROR;
                break;
            case E_USER_ERROR:
                $errnostr = 'User Error';
                $poErrorLevel = POLI::ERROR;
                break;
            case E_USER_WARNING:
                $errnostr = 'User Warning';
                $poErrorLevel = POLI::WARN;
                break;
            case E_USER_NOTICE:
                $errnostr = 'User Notice';
                $poErrorLevel = POLI::NOTICE;
                break;
            case E_STRICT:
                $errnostr = 'Runtime Notice';
                $poErrorLevel = POLI::DEBUG;
                break;
            case E_RECOVERABLE_ERROR:
                $errnostr = 'Catchable Fatal Error';
                $poErrorLevel = POLI::ERROR;
                break;
        }

        POLI::log("$errnostr: $errfile($errline): $errstr", $poErrorLevel);
    }

    /*
     * Adding a logger specifying a group means that all logged messages with that groupID
     * will be logged.  Specifying no group means that all messages will be logged
     * regardless of the message's group.
     */
    public static function addLogger($logger, $group = null) {
        POLI::$logCount += 1;
        POLI::$loggers[POLI::$logCount] = $logger;
        if (is_array($group)) {
            foreach ($group as $g) {
                POLI::$logGroups[$g][POLI::$logCount] = POLI::$logCount;
            }
        } else {
            POLI::$logGroups[$group][POLI::$logCount] = POLI::$logCount;
        }
    }

    public static function logLevelString($logLevel) {
        switch ($logLevel) {
            case 0:
                return "NONE";
            case POLI::INFO:
                return "INFO";
            case POLI::DEBUG:
                return "DEBUG";
            case POLI::NOTICE:
                return "NOTICE";
            case POLI::WARN:
                return "WARN";
            case POLI::ERROR:
                return "ERROR";
            case POLI::CRITICAL:
                return "CRITICAL";
        }
        return "";
    }
    public static function logLevelStringToInteger($logLevel) {
        switch ($logLevel) {
            case "NONE":
                return 'NONE';
            case "INFO":
                return POLI::INFO;
            case "DEBUG":
                return POLI::DEBUG;
            case "NOTICE":
                return POLI::NOTICE;
            case "WARN":
                return POLI::WARN;
            case "ERROR":
                return POLI::ERROR;
            case "CRITICAL":
                return POLI::CRITICAL;
        }
        return false;
    }

    public static function getSelectArray($include_none = true) {
        if ($include_none) {
            return array(
                'NONE' => 0,
                'DEBUG' => POLI::DEBUG,
                'INFO' => POLI::INFO,
                'NOTICE' => POLI::NOTICE,
                'WARN' => POLI::WARN,
                'ERROR' => POLI::ERROR,
                'CRITICAL' => POLI::CRITICAL,
            );
        }
        return array(
            'DEBUG' => POLI::DEBUG,
            'INFO' => POLI::INFO,
            'NOTICE' => POLI::NOTICE,
            'WARN' => POLI::WARN,
            'ERROR' => POLI::ERROR,
            'CRITICAL' => POLI::CRITICAL,
        );
    }

}
