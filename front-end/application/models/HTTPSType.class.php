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

require_once 'TLSMode.class.php';

class HTTPSType {
    /**
     * @var boolean
     */
    public $enabled = false;

    /**
     * @var TLSMode
     */
    public $tlsMode;

    public function __construct(){
    }

    /**
     * Getters and Setters
     */

    /**
     * @param boolean $enabled
     */
    public function setEnabled($enabled)
    {
        $this->enabled = $enabled;
    }

    /**
     * @return boolean
     */
    public function getEnabled()
    {
        return $this->enabled;
    }


    /**
     * @param TLSMode $tlsMode
     */
    public function setTlsMode($tlsMode)
    {
        $this->tlsMode = $tlsMode;
    }

    /**
     * @return TLSMode
     */
    public function getTlsMode()
    {
        return $this->tlsMode;
    }

    /**
     * Serialization function that we've moved to another file so it is cleaner/easier to read.
     * @return string
     */
    public function toXML(){
        ob_start();
        include('templates/HTTPSType.tpl.php');
        return ob_get_clean();
    }


    public static function fromXML($xml){
        $https = new HTTPSType();
        
        $https->setEnabled( (bool) $xml);
        if($xml instanceof SimpleXMLElement) {
        	if($xml['tlsMode']) {
        		$https->setTlsMode(TLSMode::fromXML($xml['tlsMode']));
        	}
        }       
        
        return $https;
    }
}