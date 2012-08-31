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

require_once 'ValidationFile.class.php';

/**
 * File Description Goes Here
 *
 * Date: 05/30/12
 *
 */
class Validation
{
    /**
     * @var Boolean
     */
    public $xml;
    /**
     * @var String
     */
    public $soapVersion;

    /**
     * @var String
     */
    public $type;

    /**
     * @var array
     */
    public $validationFiles;

    public function toXML(){
        ob_start();
        include('templates/Validation.tpl.php');
        return ob_get_clean();
    }

    public static function fromXML(SimpleXMLElement $xml = null){
        $valid = null;
        if(!empty($xml)){
            $valid = new Validation();
            $valid->setXML((boolean)$xml->xml);
            $valid->setSOAPVersion((string)$xml->soap->version);
            $valid->setType((string)$xml->schema->type);
            $validationFiles = array();
            if(!empty($xml->schema->resources->resource)){
              foreach($xml->schema->resources->resource as $resource){
                  $validationFile = ValidationFile::fromXML($resource);
                  $validationFiles[] = $validationFile;
              }
              $valid->setValidationFiles($validationFiles);
            }
        }
        return $valid;
    }

    /**
     * @param Boolean $xml
     */
    public function setXML($xml)
    {
        $this->xml = $xml;
    }

    /**
     * @return Boolean
     */
    public function getXML()
    {
        return $this->xml;
    }

    /**
     * @param String $soapVersion
     */
    public function setSOAPVersion($soapVersion)
    {
        $this->soapVersion = $soapVersion;
    }

    /**
     * @return String
     */
    public function getSOAPVersion()
    {
        return $this->soapVersion;
    }

    /**
     * @param String $type
     */
    public function setType($type)
    {
        $this->type = $type;
    }

    /**
     * @return String
     */
    public function getType()
    {
        return $this->type;
    }

    /**
     * @param Array[ValidationFile] $validationFile
     */
    public function setValidationFiles($validationFile)
    {
        $this->validationFiles = $validationFile;
    }

    public function getValidationFiles()
    {
        return $this->validationFiles;
    }

}
