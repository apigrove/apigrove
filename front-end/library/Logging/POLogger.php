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


require_once "POLI.php";

/**
 * Created by JetBrains PhpStorm.
 *
 * @author Trevor Pesout <>
 * @version $Id$
 * @copyright 10/12/11 2:49 PM
 */
abstract class POLogger
{
    protected $logLevel;
    protected $logFormat;
    protected $formatArray = array(
        '%m' => 'return $message;',
        '%i' => 'return $_SERVER[\'REMOTE_ADDR\'];',
        '%d' => 'return date("Y-m-j");',
        '%t' => 'return date("H:i:s");',
        '%l' => 'return POLI::logLevelString($priority);',
    );

    abstract function log($message, $priority = POLI::INFO);

    public function format($message, $priority = 0) {
        $keys = array();
        $values = array();
        foreach ($this->formatArray as $k => $v) {
            if (strpos($this->logFormat, $k) !== false) {
                $keys[] = $k;
                $values[] = @eval($v);
            }
        }
        return str_replace($keys, $values, $this->logFormat);

    }
    
    public function getLogLevel() {
        return $this->logLevel;
    }
    public function setLogLevel($level) {
        $this->logLevel = $level;
    }
}
