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
 * Object representation of TDR for an API
 * @author Guillaume Quemart
 *
 */
require_once("TdrRuleType.php");

class TdrData {
    /**
     * @var array of StaticTdr
     */
    public $staticTdrs = array();

    /**
     * @var array of DynamicTdr
     */
    public $dynamicTdrs = array();

    public function toXML(){
        ob_start();
        include('templates/TdrData.tpl.php');
        return ob_get_clean();
    }

    public static function fromXML(SimpleXMLElement $xml){
        $data = new TdrData();
        if ($xml->static)
            foreach($xml->static as $tdr){
                $data->staticTdrs[] = StaticTdr::fromXML($tdr);
            }
        if ($xml->dynamic)
            foreach($xml->dynamic as $tdr){
                $data->dynamicTdrs[] = DynamicTdr::fromXML($tdr);
            }

        return $data;
    }

    /**
     * @param array $dynamicTdrs
     */
    public function setDynamicTdrs($dynamicTdrs)
    {
        $this->dynamicTdrs = $dynamicTdrs;
    }

    /**
     * @return array
     */
    public function getDynamicTdrs()
    {
        return $this->dynamicTdrs;
    }

    /**
     * @param array $staticTdrs
     */
    public function setStaticTdrs($staticTdrs)
    {
        $this->staticTdrs = $staticTdrs;
    }

    /**
     * @return array
     */
    public function getStaticTdrs()
    {
        return $this->staticTdrs;
    }
}
?>