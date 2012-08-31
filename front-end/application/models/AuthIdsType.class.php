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
 * AuthIdsType class definition
 * This should really be called AuthIdBucket... or something else more descriptive.
 *
 * Date: 4/12/12
 *
 */
class AuthIdsType
{
    /**
     * @var array
     * @required
     */
    public $authIds = array();

    /**
     * @var string
     * @required
     */
    public $id;

    /**
     * @return string
     */
    public function toXML(){
        ob_start();
        include('templates/AuthIdsType.tpl.php');
        return ob_get_clean();
    }

    /**
     * @static
     * @param SimpleXMLElement $xml
     * @return AuthIdsType
     */
    public static function fromXML(SimpleXMLElement $xml){
        $authIds = new AuthIdsType();
        foreach($xml->authId as $authId){
            $authIds->authIds[] = (string) current($authId);
        }
        $authIds->id = current($xml['id']);

        return $authIds;
    }

    /**
     * @param array $authIds
     */
    public function setAuthIds($authIds)
    {
        $this->authIds = $authIds;
    }

    /**
     * @return array
     */
    public function getAuthIds()
    {
        return $this->authIds;
    }

    /**
     * @param string $id
     */
    public function setId($id)
    {
        $this->id = $id;
    }

    /**
     * @return string
     */
    public function getId()
    {
        return $this->id;
    }
}
