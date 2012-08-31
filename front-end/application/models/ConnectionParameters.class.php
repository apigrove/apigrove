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
 * @version $Id$
 * @copyright 7/20/12 4:19 PM
 */

class ConnectionParameters
{
    public $numConnections;
    public $maxConnections;
    public $connectionAcquisitionTimeout;
    public $initialConnectionTimeout;
    public $initialConnectionTimeoutAttempts;

    public function toXML(){
        ob_start();
        include('templates/ConnectionParameters.tpl.php');
        return ob_get_clean();
    }

    public static function fromXML(SimpleXMLElement $xml){
        $th = new ConnectionParameters();
        if (empty($xml)) return $th;
        $th->setMaxConnections((string)$xml->numConnections);
        $th->setNumConnections((string)$xml->maxConnections);
        $th->setConnectionAcquisitionTimeout((string)$xml->connectionAcquisitionTimeout);
        $th->getInitialConnectionTimeout((string)$xml->initialConnectionTimeout);
        $th->getInitialConnectionTimeoutAttempts((string)$xml->initialConnectionTimeoutAttempts);

        return $th;
    }

    public function getMaxConnections() {
        return $this->maxConnections;
    }
    public function setMaxConnections($val) {
        $this->maxConnections = $val;
    }

    public function getNumConnections() {
        return $this->numConnections;
    }
    public function setNumConnections($val) {
        $this->numConnections = $val;
    }

    public function getConnectionAcquisitionTimeout() {
        return $this->connectionAcquisitionTimeout;
    }
    public function setConnectionAcquisitionTimeout($val) {
        $this->connectionAcquisitionTimeout = $val;
    }

    public function getInitialConnectionTimeout() {
        return $this->initialConnectionTimeout;
    }
    public function setInitialConnectionTimeout($val) {
        $this->initialConnectionTimeout = $val;
    }

    public function getInitialConnectionTimeoutAttempts() {
        return $this->initialConnectionTimeoutAttempts;
    }
    public function setInitialConnectionTimeoutAttempts($val) {
        $this->initialConnectionTimeoutAttempts = $val;
    }

    public function isEmpty() {
        return empty($this->maxConnections) &&
            empty($this->numConnections) &&
            empty($this->connectionAcquisitionTimeout) &&
            empty($this->initialConnectionTimeout) &&
            empty($this->initialConnectionTimeoutAttempts);
    }
}
