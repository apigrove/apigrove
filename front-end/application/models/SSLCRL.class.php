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


class SSLCRL
{
    public $id;
    public $displayName;
    public $content;
    public $nextUpdateDate; // used by frontend only, not part of payloads
    public $lastUpdateDate; // used by frontend only, not part of payloads

    public function toXml() {
        ob_start();
        include('templates/SSLCRL.tpl.php');
        return ob_get_clean();
    }

    public static function fromXml(SimpleXMLElement $xml){
        $key = new SSLCRL();
        $key->id = (string) $xml['id'];
        $key->displayName = current($xml->displayName);
        return $key;
    }

    public function getId() {
        return $this->id;
    }
    public function setId($x) {
        $this->id = $x;
    }

    public function getDisplayName() {
        return $this->displayName;
    }
    public function setDisplayName($x) {
        $this->displayName = $x;
    }

    public function getContent() {
        return $this->content;
    }
    public function setContent($x) {
        $this->content = $x;
    }
    
    public function setNextUpdateDate($x) {
    	$this->nextUpdateDate = $x;
    }
    
    public function getNextUpdateDate() {
    	return $this->nextUpdateDate;
    }
    
    public function setLastUpdateDate($x) {
    	$this->lastUpdateDate = $x;
    }
    
    public function getLastUpdateDate() {
    	return $this->lastUpdateDate;
    }

}
