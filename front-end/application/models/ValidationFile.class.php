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
 * Date: 05/30/12
 *
 */
class ValidationFile
{
    /**
     * @var String
     */
    public $name;

    /**
     * @var Boolean
     */
    public $isMain;

    /**
     * @var String
     */
    public $grammar;

    public function toXML(){
        ob_start();
        include('templates/ValidationFile.tpl.php');
        return ob_get_clean();
    }

    public static function fromXML(SimpleXMLElement $xml = null){
        $file = new ValidationFile();
        if(!empty($xml)){
            $file->setName((string)$xml->name);
            $file->setIsMain((boolean) ($xml->isMain == "true") ? true : false);
            $file->setGrammar((string)$xml->grammar);
        }
        return $file;
    }

    /**
     * @param Boolean $isMain
     */
    public function setIsMain($isMain)
    {
        $this->isMain = $isMain;
    }

    /**
     * @return Boolean
     */
    public function getIsMain()
    {
        return $this->isMain;
    }

    /**
     * @param String $name
     */
    public function setName($name)
    {
        $this->name = $name;
    }

    /**
     * @return String
     */
    public function getName()
    {
        return $this->name;
    }
    
    /**
     * @param String $grammar
     */
    public function setGrammar($grammar)
    {
        $this->grammar = $grammar;
    }

    /**
     * @return String
     */
    public function getGrammar()
    {
        return $this->grammar;
    }
}
