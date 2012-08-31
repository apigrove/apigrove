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
 * Class Definition for DynamicTdr
 *
 * Date: 3/28/12
 *
 */
require_once "TdrRuleExtractFrom.php";

class DynamicTdr
{
    /**
     * @var array of TdrType
     */
    public $types;

    /**
     * @var String
     */
    public $tdrPropName;

    /**
     * @var String
     */
    public $httpHeaderName;

    /**
     * @var String
     */
    public $extractFrom;

    public function toXML(){
        ob_start();
        include('templates/DynamicTdr.tpl.php');
        return ob_get_clean();
    }


    public static function fromXML(SimpleXMLElement $xml){
        $tdr = new DynamicTdr();
        $tdr->tdrPropName = (string) $xml['tdrPropName'];
        $tdr->httpHeaderName = (string) $xml['httpHeaderName'];
        $tdr->extractFrom = (string) $xml['extractFrom'];

        foreach($xml->types->type as $type){
            $tdr->types[] = (string) $type;
        }

        return $tdr;
    }

    /**
     * @param String $httpHeaderName
     */
    public function setHttpHeaderName($httpHeaderName)
    {
        $this->httpHeaderName = $httpHeaderName;
    }

    /**
     * @return String
     */
    public function getHttpHeaderName()
    {
        return $this->httpHeaderName;
    }

    /**
     * @param String $tdrPropName
     */
    public function setTdrPropName($tdrPropName)
    {
        $this->tdrPropName = $tdrPropName;
    }

    /**
     * @return String
     */
    public function getExtractFrom()
    {
        return $this->extractFrom;
    }

    /**
     * @param String $from
     */
    public function setExtractFrom($from)
    {
        $this->extractFrom = $from;
    }

    /**
     * @return String
     */
    public function getTdrPropName()
    {
        return $this->tdrPropName;
    }

    /**
     * @param array $types
     */
    public function setTypes($types)
    {
        $this->types = $types;
    }

    /**
     * @return array
     */
    public function getTypes()
    {
        return $this->types;
    }
}
