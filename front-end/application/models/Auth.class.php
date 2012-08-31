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
 * Class file for the Auth object
 *
 * Date: 4/12/12
 *
 */

require_once 'Status.class.php';
require_once 'IpWhiteListAuth.class.php';
require_once 'BasicAuth.class.php';
require_once 'AuthKeyAuth.class.php';
require_once 'WSSEAuth.class.php';
require_once 'AuthType.class.php';
require_once 'TdrData.class.php';
require_once 'DynamicTdr.class.php';
require_once 'StaticTdr.class.php';
require_once 'TdrType.class.php';
require_once 'HeaderTransformation.class.php';

class Auth{
    /**
     * The authentication ID. Could be provided.
     *
     * @var string
     */
    public $id;

    /**
     * The authentication status: active, inactive, pending
     * @required
     * @var string
     */
    public $status;

    /**
     * The authentication type: basic, authKey, IP white list
     * @required
     * @var string
     */
    public $type;

    /**
     * The policy context for which this Auth is applicable.
     * @required
     * @var String
     */
    public $policyContext;

    /**
     * The api context for which this Auth is applicable.
     * @required
     * @var String
     */
    public $apiContext;

    /**
     * The Basic Auth details if basic is selected as a type.
     * @required
     * @var BasicAuth
     */
    public $basicAuth;

    /**
     * The authKey details if authKey selected as a type
     * @required
     * @var AuthKeyAuth
     */
    public $authKeyAuth;

    /**
     * The IP white list if this authentication is selected as a type.
     * @required
     * @var IpWhiteListAuth $ipWhiteListAuth
     */
    public $ipWhiteListAuth;

    /**
     * The WSSE if this authentication is selected as a type.
     * @required
     * @var WsseAuth
     */
    public $wsseAuth;

    /**
     * TDR data that will be appended at TDR generation time.
     * @var TdrData
     */
    public $tdrData;

    /**
     * @var string
     */
    public $headerTransformations;

    /**
     * @var string
     */
    public $properties;

    /**
     * Constructor
     */
    public function __construct(){
        // Set some defaults
        $this->status = "active";
        $this->type = AuthType::$AUTHKEY;
        $this->basicAuth = new BasicAuth();
        $this->authKeyAuth = new AuthKeyAuth();
        $this->ipWhiteListAuth = new IpWhiteListAuth();
        $this->wsseAuth = new WSSEAuth();
    }

    public function toXML(){
        ob_start();
        include('templates/Auth.tpl.php');
        return ob_get_clean();
    }

    /**
     * @static
     * @param SimpleXMLElement $xml
     * @return Auth
     */
    public static function fromXML(SimpleXMLElement $xml){
        $auth = new Auth();
        $auth->id = (string) $xml->id;
        $auth->apiContext = (string) $xml->apiContext['id'];
        $auth->authKeyAuth = AuthKeyAuth::fromXML($xml->authKeyAuth);
        $auth->basicAuth = BasicAuth::fromXML($xml->basicAuth);
        $auth->ipWhiteListAuth = IpWhiteListAuth::fromXML($xml->ipWhiteListAuth);
        $auth->wsseAuth = WSSEAuth::fromXML($xml->wsseAuth);
        $auth->policyContext = (string) $xml->policyContext['id'];
        $auth->status = current($xml->status);
        $auth->type = AuthType::fromXML($xml->type);
        $auth->tdrData = TdrData::fromXML($xml->tdr);

        if ($xml->properties && $xml->properties->property) {
            foreach ($xml->properties->property as $prop) {
                $auth->properties[(string)$prop['name']] = (string) $prop;
            }
        }
        if ($xml->headerTransformations && $xml->headerTransformations->headerTransformation) {
            foreach ($xml->headerTransformations->headerTransformation as $transform) {
                $auth->headerTransformations[] = HeaderTransformation::fromXML($transform);
            }
        }

        return $auth;
    }

    /**
     * @param String $apiContext
     */
    public function setApiContext($apiContext)
    {
        $this->apiContext = $apiContext;
    }

    /**
     * @return String
     */
    public function getApiContext()
    {
        return $this->apiContext;
    }

    /**
     * @param \AuthKeyAuth $authKeyAuth
     */
    public function setAuthKeyAuth($authKeyAuth)
    {
        $this->authKeyAuth = $authKeyAuth;
    }

    /**
     * @return \AuthKeyAuth
     */
    public function getAuthKeyAuth()
    {
        return $this->authKeyAuth;
    }

    /**
     * @param \BasicAuth $basicAuth
     */
    public function setBasicAuth($basicAuth)
    {
        $this->basicAuth = $basicAuth;
    }

    /**
     * @return \BasicAuth
     */
    public function getBasicAuth()
    {
        return $this->basicAuth;
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

    /**
     * @param \IpWhiteListAuth $ipWhiteListAuth
     */
    public function setIpWhiteListAuth($ipWhiteListAuth)
    {
        $this->ipWhiteListAuth = $ipWhiteListAuth;
    }

    /**
     * @return \IpWhiteListAuth
     */
    public function getIpWhiteListAuth()
    {
        return $this->ipWhiteListAuth;
    }

    /**
     * @param \WSSEAuth $auth
     */
    public function setWsseAuth($auth)
    {
        $this->wsseAuth = $auth;
    }

    /**
     * @return \WSSEAuth
     */
    public function getWsseAuth()
    {
        return $this->wsseAuth;
    }

    /**
     * @param String $policyContext
     */
    public function setPolicyContext($policyContext)
    {
        $this->policyContext = $policyContext;
    }

    /**
     * @return String
     */
    public function getPolicyContext()
    {
        return $this->policyContext;
    }

    /**
     * @param \Status $status
     */
    public function setStatus($status)
    {
        $this->status = $status;
    }

    /**
     * @return \Status
     */
    public function getStatus()
    {
        return $this->status;
    }

    /**
     * @param \AuthType $type
     */
    public function setType($type)
    {
        $this->type = $type;
    }

    /**
     * @return \AuthType
     */
    public function getType()
    {
        return $this->type;
    }

    /**
     * @param \TdrData $tdrData
     */
    public function setTdrData($tdrData)
    {
        $this->tdrData = $tdrData;
    }

    /**
     * @return \TdrData
     */
    public function getTdrData()
    {
        return $this->tdrData;
    }

    public function setHeaderTransformations($transforms)
    {
    	$this->headerTransformations = $transforms;
    }

    public function getHeaderTransformations()
    {
    	return $this->headerTransformations;
    }

    public function setProperties($properties)
    {
    	$this->properties = $properties;
    }

    public function getProperties()
    {
    	return $this->properties;
    }
}
