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
 * Object representation of Authentication for an API
 * @author Guillaume Quemart
 *
 */
class ProvisionAuthentication {
    /**
     * List of AuthTypes
     * @var
     */
    public $auths = array();
    public $authKey = null;

    public function toXML(){
        ob_start();
        include('templates/ProvisionAuthentication.tpl.php');
        return ob_get_clean();
    }

    public static function fromXML(SimpleXMLElement $xml){
        $pa = new ProvisionAuthentication();
        $types = array();
        foreach($xml->supportedTypes->type as $typeXML){
            $type = AuthType::fromXML($typeXML);
            if($type !== null){
                $types[] = $type;
            }
        }
        $pa->setAuths($types);

        $pa->setAuthKey((string) $xml->authKey->keyName);

        return $pa;
    }


    public function setAuthKey($authKey)
    {
        $this->authKey = $authKey;
    }

    public function getAuthKey()
    {
        return $this->authKey;
    }

    /**
     * @param  $auths
     */
    public function setAuths($auths)
    {
        $this->auths = $auths;
    }

    /**
     * @return
     */
    public function getAuths()
    {
        return $this->auths;
    }
}