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
 * WsseAuth class definition
 *
 * Date: 4/12/12
 *
 */
class WSSEAuth
{
    /**
     * @var string
     * @required
     */
    public $username;

    /**
     * @var string
     * @required
     */
    public $password;

    /**
     * @var string
     * @required
     */
    public $passwordType;

    public function toXML(){
        ob_start();
        include('templates/WSSEAuth.tpl.php');
        return ob_get_clean();
    }

    public static function fromXML(SimpleXMLElement $xml){
        $basic = new WSSEAuth();
        $basic->password = base64_decode((string) $xml->password);
        $basic->passwordType = (string) $xml->passwordType;
        $basic->username = (string) $xml->username;

        return $basic;
    }

    /**
     * @param string $password
     */
    public function setPassword($password)
    {
        $this->password = $password;
    }

    /**
     * @return string
     */
    public function getPassword()
    {
        return $this->password;
    }

    /**
     * @param string $password
     */
    public function setPasswordType($passwordType)
    {
        $this->passwordType = $passwordType;
    }

    /**
     * @return string
     */
    public function getPasswordType()
    {
        return $this->passwordType;
    }

    /**
     * @param string $username
     */
    public function setUsername($username)
    {
        $this->username = $username;
    }

    /**
     * @return string
     */
    public function getUsername()
    {
        return $this->username;
    }
}
