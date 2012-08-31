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
class Authentication
{
    /**
     * @var String
     */
    public $type;

    /**
     * @var Data
     */
    public $data;

    public function toXML(){
        ob_start();
        include('templates/Authentication.tpl.php');
        return ob_get_clean();
    }

    public static function fromXML(SimpleXMLElement $xml = null){
        $auth = null;
        if(!empty($xml)){
            $auth = new Authentication();
            $auth->setType((string)$xml->type);
            if ($data = Data::fromXML($xml->data))
                $auth->setData($data);
        }

        return $auth;
    }

    /**
     * @param Data $data
     */
    public function setData(Data $data)
    {
        $this->data = $data;
    }

    /**
     * @return \Data
     */
    public function getData()
    {
        return $this->data;
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
}
