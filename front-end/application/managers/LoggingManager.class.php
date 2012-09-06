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
 * @version $Id$
 * @copyright 5/2/12 9:34 AM
 */

require_once APPLICATION_PATH . '/models/LogConfig.class.php';
require_once APPLICATION_PATH . '/models/LoggingCategory.class.php';
require_once APPLICATION_PATH . '/models/LogCollection.class.php';
require_once 'RestClient/RestClient.class.php';

define('E3_SYS_LOGGING_URL', '/cxf/e3/system-manager/logging');

class LoggingManager
{
    protected $restClient = null;

    public function __construct(){
        $config = new Zend_Config_Ini(APPLICATION_PATH . '/configs/manager.ini');

        $this->restClient = new RestClient($config->manager_host,
            $config->manager_protocol, $config->manager_port, $config->manager_basicauth);
    }

    private function _getJavaLogLevel($instanceId = null) {
        if (empty($instanceId)) {
            $url = E3_SYS_LOGGING_URL."/logLevel";
        } else {
            $url = E3_SYS_LOGGING_URL."/instances/".$instanceId."/logLevel";
        }
        $reply = $this->restClient->makeCall($url, "GET");
        $xml = simplexml_load_string($reply->getPayload());
        $log = LogConfig::fromXml($xml->logLevel);
        return $log;

    }

    private function _setJavaLogLevel($level, $instanceId = null) {
        if ($level instanceof LogConfig) $level = $level->getLogLevel();
        if (empty($instanceId)) {
            $url = E3_SYS_LOGGING_URL."/logLevel";
        } else {
            $url = E3_SYS_LOGGING_URL."/instances/".$instanceId."/logLevel";
        }
        $this->restClient->makeCall($url, "PUT", $level);
        return true;

    }

    private function _getSMXLogLevel($instanceId = null) {
        if (empty($instanceId)) {
            $url = E3_SYS_LOGGING_URL."/smxlogLevel";
        } else {
            $url = E3_SYS_LOGGING_URL."/instances/".$instanceId."/smxlogLevel";
        }
        $reply = $this->restClient->makeCall($url, "GET");
        $xml = simplexml_load_string($reply->getPayload());
        $log = LogConfig::fromXml($xml->logLevel);
        return $log;

    }

    private function _setSMXLogLevel($level, $instanceId = null) {
        if ($level instanceof LogConfig) $level = $level->getLogLevel();
        if (empty($instanceId)) {
            $url = E3_SYS_LOGGING_URL."/smxlogLevel";
        } else {
            $url = E3_SYS_LOGGING_URL."/instances/".$instanceId."/smxlogLevel";
        }
        $this->restClient->makeCall($url, "PUT", $level);
        return true;

    }

    private function _getSyslogLogLevel($instanceId = null) {
        if (empty($instanceId)) {
            $url = E3_SYS_LOGGING_URL."/syslogLevel";
        } else {
            $url = E3_SYS_LOGGING_URL."/instances/".$instanceId."/syslogLevel";
        }
        $reply = $this->restClient->makeCall($url, "GET");
        $xml = simplexml_load_string($reply->getPayload());
        $log = LogConfig::fromXml($xml->logLevel);
        return $log;

    }

    private function _setSyslogLogLevel($level, $instanceId = null) {
        if ($level instanceof LogConfig) $level = $level->getLogLevel();
        if (empty($instanceId)) {
            $url = E3_SYS_LOGGING_URL."/syslogLevel";
        } else {
            $url = E3_SYS_LOGGING_URL."/instances/".$instanceId."/syslogLevel";
        }
        $this->restClient->makeCall($url, "PUT", $level);
        return true;
    }

    private function _getLoggingCategories() {

        $url = E3_SYS_LOGGING_URL."/loggingCategories";
        $reply = $this->restClient->makeCall($url, "GET");
        $xml = simplexml_load_string($reply->getPayload());
        $categories = array();
        if ($xml->loggingCategories) {
            foreach ($xml->loggingCategories->loggingCategory as $category) {
                $categories[] = LoggingCategory::fromXML($category);
            }
        }
        return $categories;

    }

    private function _setLoggingCategories($categories) {
        $url = E3_SYS_LOGGING_URL."/loggingCategories";
        $this->restClient->makeCall($url, "PUT", LoggingCategory::toXML($categories));
        return true;
    }

    /*
     * The following section contains methods moved from E3Interface
     */
    private static $error;

    /**
     * If no param, returns last error
     * If param is exception, logs exception and sets error
     * If param is String, sets error
     *
     * @static
     * @param null $err
     * @return mixed
     */
    public static function error($err = null)
    {
        if ($err == null) return LoggingManager::$error;
        if ($err instanceof Exception) {
            LoggingManager::$error = $err->getMessage();
            LoggerInterface::logException($err, LoggerInterface::ERROR);
        } else {
            LoggingManager::$error = $err;
            LoggerInterface::log($err, LoggerInterface::ERROR);
        }
    }

    public function getActiveLog($log_count = 50){
        $url = E3_SYS_LOGGING_URL."/activeLogLines/$log_count";
        $reply = $this->restClient->makeCall($url, "GET");
        $xml = simplexml_load_string($reply->getPayload());
        $log = LogCollection::fromXML($xml->logCollection);
        return $log;

    }

    public function getCollectedLog($log_count = 50){
        $url = E3_SYS_LOGGING_URL."/collectedLogLines/$log_count";
        $reply = $this->restClient->makeCall($url, "GET");
        $xml = simplexml_load_string($reply->getPayload());
        $log = LogCollection::fromXML($xml->logCollection);
        return $log;

    }

    /**
     * gets all active logs
     * @param $count
     * @return bool|LogCollection
     */
    public function getAllActiveLogs($count)
    {
        try {
            return $this->getActiveLog($count);
        } catch (Exception $e) {
            $this->error($e);
        }
        return false;
    }

    /**
     * gets the syslog log level
     * @return bool|LogConfig
     */
    public function getLoggingCategories()
    {
        try {
            return $this->_getLoggingCategories();
        } catch (Exception $e) {
            $this->error($e);
        }
        return false;
    }

    /**
     * @param $categories
     * @return bool
     */
    public function setLoggingCategories($categories)
    {
        try {
            $this->_setLoggingCategories($categories);
            return true;
        } catch (Exception $e) {
            $this->error($e);
        }
        return false;

    }

    /**
     * gets the java log level
     * @return bool|LogConfig
     */
    public function getJavaLogLevel($instanceId = null)
    {
        try {
            return $this->_getJavaLogLevel($instanceId);
        } catch (Exception $e) {
            $this->error($e);
        }
        return false;

    }

    /**
     * sets the log level for java logging
     * @param $level
     * @return bool
     */
    public function setJavaLogLevel($level, $instanceId = null)
    {
        try {
            $returnResultJava = $this->_setJavaLogLevel($level, $instanceId);
            return $returnResultJava;
        } catch (Exception $e) {
            $this->error($e);
        }
        return false;
    }

    /**
     * gets the servicemix log level
     * @return bool|LogConfig
     */
    public function getSMXLogLevel($instanceId = null)
    {
        try {
            return $this->_getSMXLogLevel($instanceId);
        } catch (Exception $e) {
            $this->error($e);
        }
        return false;

    }

    /**
     * sets the log levels for servicemix logging
     * @param $level
     * @return bool
     */
    public function setSMXLogLevel($level, $instanceId = null)
    {
        try {
            $returnResultSMX = $this->_setSMXLogLevel($level, $instanceId);
            return $returnResultSMX;
        } catch (Exception $e) {
            $this->error($e);
        }
        return false;
    }

    /**
     * gets the syslog log level
     * @return bool|LogConfig
     */
    public function getSyslogLogLevel($instanceId = null)
    {
        try {
            return $this->_getSyslogLogLevel($instanceId);
        } catch (Exception $e) {
            $this->error($e);
        }
        return false;

    }

    /**
     * sets the log levels for e3-specific syslog logging
     * @param $level
     * @return bool
     */
    public function setSyslogLogLevel($level, $instanceId = null)
    {
        try {
            $returnResultSyslog = $this->_setSyslogLogLevel($level, $instanceId);
            return $returnResultSyslog;
        } catch (Exception $e) {
            $this->error($e);
        }
        return false;
    }

}
