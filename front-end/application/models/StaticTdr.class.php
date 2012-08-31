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
 * File Description Goes Here
 *
 * Date: 3/28/12
 *
 */
class StaticTdr
{
    /**
     * @var array
     */
    public $types;

    /**
     * @var string
     */
    public $tdrPropName;

    /**
     * @var string
     */
    public $value;

    /**
     * @var string
     */
    public $property;

    /**
     * @return string
     */
    public function toXML(){
        ob_start();
        include('templates/StaticTdr.tpl.php');
        return ob_get_clean();
    }

    /**
     * @static
     * @param SimpleXMLElement $xml
     * @return StaticTdr
     */
    public static function fromXML(SimpleXMLElement $xml){
        $tdr = new StaticTdr();
        $tdr->tdrPropName = (string) $xml['tdrPropName'];
        $tdr->value = (string) $xml['value'];
        $tdr->property = (string) $xml['property'];

        foreach($xml->types->type as $type){
            $tdr->types[] = (string) $type;
        }

        return $tdr;
    }

    public function setTdrPropName($tdrPropName)
    {
        $this->tdrPropName = $tdrPropName;
    }

    public function getTdrPropName()
    {
        return $this->tdrPropName;
    }

    public function setTypes($types)
    {
        $this->types = $types;
    }

    public function getTypes()
    {
        return $this->types;
    }

    public function setValue($value)
    {
        $this->value = $value;
    }

    public function getValue()
    {
        return $this->value;
    }

    public function setProperty($value)
    {
        $this->property = $value;
    }

    public function getProperty()
    {
        return $this->property;
    }
}
