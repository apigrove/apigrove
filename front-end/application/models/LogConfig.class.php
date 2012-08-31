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
 * @copyright 5/2/12 9:33 AM
 */

class LogConfig
{
    public $logLevel;

    public static $log4JLevels = array(
        "OFF",
        "FATAL",
        "ERROR",
        "WARN",
        "INFO",
        "DEBUG",
        "TRACE",
        "ALL"
    );

    public static $syslogLevels = array(
        "EMERG",
        "ALERT",
        "CRIT",
        "ERR",
        "WARNING",
        "NOTICE",
        "INFO",
        "DEBUG"
    );

    public static $logSources = array(
        "JAVA" => "Java",
        "SMX" => "ServiceMix",
        "SYSLOG" => "Syslog"
    );

    public function setLogLevel($level) {
        $this->logLevel = $level;
    }

    public function getLogLevel() {
        return $this->logLevel;
    }

    public static function fromXML($xml) {
        $lc = new LogConfig();
        $lc->logLevel = current($xml->level);
        return $lc;
    }
}
