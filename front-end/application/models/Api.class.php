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

require_once 'ProvisionAuthentication.class.php';
require_once 'TargetHost.class.php';
require_once 'TdrData.class.php';
require_once 'AuthType.class.php';
require_once 'ApiContext.class.php';
require_once 'EnvironmentType.class.php';
require_once 'Status.class.php';
require_once 'Authentication.class.php';
require_once 'Data.class.php';
require_once 'Key.class.php';
require_once 'DynamicTdr.class.php';
require_once 'StaticTdr.class.php';
require_once 'TdrType.class.php';
require_once 'ApiType.class.php';
require_once 'Validation.class.php';
require_once 'HTTPSType.class.php';

/**
 *
 * Object representation of an E3 API (Route definition)
 * @author Guillaume Quemart
 *
 */
class Api {
    /**
     * @var string
     */
    public $id = NULL;

    /**
     * @var string
     */
    public $displayName;

    /**
     * @var string
     */
    public $endpoint;

    /**
     * @var ProvisionAuthentication
     */
    public $authentication;

    /**
     * @var string
     */
    public $type;

    /**
     * @var string
     */
    public $version;

    /**
     * @var string
     */
    public $tdrEnabled;

    /**
     * @var TdrData
     */
    public $tdrData;

    /**
     * @var string
     */
    public $tdrOnLimitReached;

    /**
     * @var string
     */
    public $tdrOnUse;

    /**
     * @var string
     */
    public $status;

    /**
     * @var array
     */
    public $contexts;

    /**
     * @var HTTPSType
     */
    public $https;

    /**
     * @var Validation
     */
    public $validation;
    
    /**
     * @var string
     */
    public $subscriptionStep;

    /**
     * @var string
     */
    public $notificationFormat;

    /**
     * @var string
     */
    public $headerTransformations;

    /**
     * @var string
     */
    public $headerTransformationEnabled;

    /**
     * @var string
     */
    public $allowedHttpMethods = array();

    /**
     * @var string
     */
    public $properties;

    public function __construct(){
        $this->type = ApiType::$PASS_THROUGH;
        $this->contexts = array(/*$context = new ApiContext()*/);
        $this->authentication = new ProvisionAuthentication();
//        $this->https = new HTTPSType();
//        $this->validation = new Validation();
    }

    /**
     * Getters and Setters
     */

    /**
     * @param int $id
     */
    public function setId($id)
    {
        $this->id = $id;
    }

    /**
     * @return int
     */
    public function getId()
    {
        return $this->id;
    }


    /**
     * @param string $apiID
     */
    public function setDisplayName($apiName)
    {
        $this->displayName = $apiName;
    }

    /**
     * @return string
     */
    public function getDisplayName()
    {
        return $this->displayName;
    }

    /**
     * @param string $apiEndpoint
     */
    public function setEndpoint($apiEndpoint)
    {
        $this->endpoint = $apiEndpoint;
    }

    /**
     * @return string
     */
    public function getEndpoint()
    {
        return $this->endpoint;
    }


    /**
     * @param boolean $TDR
     */
    public function setTdrEnabled($TDR)
    {
        $this->tdrEnabled = $TDR;
    }

    /**
     * @return TdrData
     */
    public function getTdrEnabled()
    {
        return $this->tdrEnabled;
    }

    /**
     * The following fields are absolutely required before calling toXML:
     *      type
     *      endpoint
     *      authentication
     *      status
     *      contexts
     * Serialization function that we've moved to another file so it is cleaner/easier to read.
     * @return string
     */
    public function toXML(){
        ob_start();
        include('templates/Api.tpl.php');
        return ob_get_clean();
    }

    public function setAuthentication(ProvisionAuthentication $authentication)
    {
        $this->authentication = $authentication;
    }

    public function getAuthentication()
    {
        return $this->authentication;
    }

    public function setContexts($contexts)
    {
        $this->contexts = $contexts;
    }

    public function getContexts()
    {
        return $this->contexts;
    }

    public function setTdrData(TdrData $tdrData)
    {
        $this->tdrData = $tdrData;
    }

    public function getTdrData()
    {
        return $this->tdrData;
    }

    public function setType($type)
    {
        $this->type = $type;
    }

    public function getType()
    {
        return $this->type;
    }

    public function setVersion($version)
    {
        $this->version = $version;
    }

    public function getVersion()
    {
        return $this->version;
    }

    public function setStatus($status)
    {
        $this->status = $status;
    }

    public function getStatus()
    {
        return $this->status;
    }

    public function setTdrOnUse($tdrOnUse)
    {
        $this->tdrOnUse = $tdrOnUse;
    }

    public function getTdrOnUse()
    {
        return $this->tdrOnUse;
    }

    public function setTdrOnLimitReached($tdr)
    {
        $this->tdrOnLimitReached = $tdr;
    }

    public function getTdrOnLimitReached()
    {
        return $this->tdrOnLimitReached;
    }

    public function setSubscriptionStep($subscriptionStep)
    {
        $this->subscriptionStep = $subscriptionStep;
    }

    public function getSubscriptionStep()
    {
        return $this->subscriptionStep;
    }

    public function setNotificationFormat($notificationFormat)
    {
        $this->notificationFormat = $notificationFormat;
    }

    public function getNotificationFormat()
    {
        return $this->notificationFormat;
    }

    public static function fromXML(SimpleXMLElement $xml){
//        POLI::log("api xml: " . print_r($xml->saveXML(), true), POLI::DEBUG);
        $api = new Api();
        $api->setId( (string) $xml->id);
        $api->setDisplayName( (string) $xml->displayName);
        $api->setVersion((string) $xml->version );
        $api->setType(ApiType::fromXML($xml->type));
        $api->setEndpoint((string) $xml->endpoint);
        $api->setAuthentication(ProvisionAuthentication::fromXML($xml->authentication));
        $api->setTdrEnabled($xml->tdrEnabled->enabled == 'true' ? true : false);
        $api->setTdrOnLimitReached((string) $xml->tdrOnLimitReached['type']);
        $api->setTdrOnUse((string) $xml->tdrOnUse['type']);
        $api->setStatus((string) $xml->status);
        $v = Validation::fromXML($xml->validation);
        if($v !== null){
            $api->setValidation($v);
        }
        $api->setSubscriptionStep((string) $xml->subscriptionStep);
        $api->setNotificationFormat((string) $xml->notificationFormat);
        
        $https = self::get_bool_value((string)$xml->https);
        if($https) {
        	$api->setHttps(HTTPSType::fromXML($xml->https));
        	
        } else {
        	$httpsType = new HTTPSType();
        	$httpsType->setEnabled(false);
        	$api->setHttps($httpsType);
        }

        $contexts = array();
        foreach($xml->contexts->context as $contextXML){
            $context = ApiContext::fromXML($contextXML);
            $contexts[] = $context;
        }
        $api->setContexts($contexts);
        $api->setTdrData(TdrData::fromXML($xml->tdr));

        if ($xml->properties && $xml->properties->property) {
            foreach ($xml->properties->property as $prop) {
                $api->properties[(string)$prop['name']] = (string) $prop;
            }
        }
        $api->setHeaderTransformationEnabled(current($xml->headerTransEnabled) == "true");
        if ($xml->headerTransformations && $xml->headerTransformations->headerTransformation) {
            foreach ($xml->headerTransformations->headerTransformation as $transform) {
                $api->headerTransformations[] = HeaderTransformation::fromXML($transform);
            }
        }
        if ($xml->allowedHttpMethods && $xml->allowedHttpMethods->httpMethod) {
            foreach ($xml->allowedHttpMethods->httpMethod as $method) {
                $api->allowedHttpMethods[] = current($method);
            }
        }
        return $api;
    }

    /**
     * @param HTTPSType $https
     */
    public function setHttps($https)
    {
        $this->https = $https;
    }

    /**
     * @return HTTPSType
     */
    public function getHttps()
    {
        return $this->https;
    }
    
    public function setValidation(Validation $validation)
    {
    	$this->validation = $validation;
    }
    
    public function getValidation()
    {
    	return $this->validation;
    }

    public function setHeaderTransformations($transforms)
    {
    	$this->headerTransformations = $transforms;
    }

    public function getHeaderTransformations()
    {
    	return $this->headerTransformations;
    }

    public function setHeaderTransformationEnabled($enabled)
    {
        $this->headerTransformationEnabled = $enabled;
    }

    public function getHeaderTransformationEnabled()
    {
        return $this->headerTransformationEnabled;
    }

    public function setProperties($properties)
    {
    	$this->properties = $properties;
    }

    public function getProperties()
    {
    	return $this->properties;
    }

    public function getAllowedHttpMethods()
    {
        return $this->allowedHttpMethods;
    }

    public function setAllowedHttpMethods($val) {
        $this->allowedHttpMethods = $val;
    }
    
    static function get_bool_value($str) {
    	switch(strtolower($str)) {
    		case 'true':
    			return true;
    		case 'false':
    		default:
    			return false;
    	}
    }
}

/**
 * unit test for serialization

$api = new Api();
$api->setDisplayName("Test");
$api->setEndpoint("/test");
$api->setId("234");
$api->setVersion("1234");

$authentication = new ProvisionAuthentication();
$authentication->setAuths(array(AuthType::$AUTHKEY, AuthType::$BASIC, AuthType::$IPWHITELIST));
$authentication->setAuthKey("authkey");
$api->setAuthentication($authentication);

$ac = new ApiContext();
$ac->setType(EnvironmentType::$PRODUCTION);
$ac->setMaxRateLimitTPMThreshold(20);
$ac->setMaxRateLimitTPMWarning(20);
$ac->setMaxRateLimitTPSThreshold(20);
$ac->setMaxRateLimitTPSWarning(20);
$ac->setStatus(Status::$ACTIVE);
$th = new TargetHost();
$th->setUrl("http://productops.com");

$thAuth = new Authentication();
$thAuth->setType("HTTPBASIC");
$data = new Data();
$key = new Key();
$key->setName("keyName");
$key->setValue("keyValue");
$data->setKeys(array($key));
$thAuth->setData($data);
$th->setAuthentication($thAuth);

$ac->setTargetHosts(array($th));
$api->setContexts(array($ac));
$tdrData = new TdrData();


$dt = new DynamicTdr();
$dt->setHttpHeaderName("HTTP_HEADER");
$dt->setTdrPropName("propname");
$dt->setTypes(array(TdrType::$API_MAX_RATE_LIMIT));

$tdrData->setDynamicTdrs(array($dt));

$st = new StaticTdr();
$st->setValue("staticValue");
$st->setTdrPropName("staticName");

$st->setTypes(array(TdrType::$API_MAX_RATE_LIMIT));

$tdrData->setStaticTdrs(array($st));
$api->setTdrData($tdrData);

$api->setTdrEnabled(true);
$api->setType(ApiType::$PASS_THROUGH);

echo $api->toXML();
 */

//$xmlString = "<api><id>1</id><displayName>1</displayName><version>0.0.1</version><type>PassThrough</type><endpoint>/1</endpoint><authentication><supportedTypes><type>authKey</type><type>basic</type><type>ipWhiteList</type></supportedTypes><authKey><keyName>authkey</keyName></authKey></authentication><tdrEnabled><enabled>true</enabled></tdrEnabled><contexts><context id=\"production\" default=\"true\"><status>active</status><targetHosts><targetHost><url>http://productops.com</url></targetHost></targetHosts><maxRateLimitTPSThreshold>1234</maxRateLimitTPSThreshold><maxRateLimitTPSWarning>1233.95</maxRateLimitTPSWarning><maxRateLimitTPMThreshold>1234</maxRateLimitTPMThreshold><maxRateLimitTPMWarning>1233.95</maxRateLimitTPMWarning></context><context id=\"production\" default=\"true\"><status>active</status><targetHosts><targetHost><url>http://productops.com</url></targetHost></targetHosts><maxRateLimitTPSThreshold>1234</maxRateLimitTPSThreshold><maxRateLimitTPSWarning>1233.95</maxRateLimitTPSWarning><maxRateLimitTPMThreshold>1234</maxRateLimitTPMThreshold><maxRateLimitTPMWarning>1233.95</maxRateLimitTPMWarning></context><context id=\"production\" default=\"true\"><status>active</status><targetHosts><targetHost><url>http://productops.com</url></targetHost></targetHosts><maxRateLimitTPSThreshold>1234</maxRateLimitTPSThreshold><maxRateLimitTPSWarning>1233.95</maxRateLimitTPSWarning><maxRateLimitTPMThreshold>1234</maxRateLimitTPMThreshold><maxRateLimitTPMWarning>1233.95</maxRateLimitTPMWarning></context><context id=\"production\" default=\"true\"><status>active</status><targetHosts><targetHost><url>http://productops.com</url></targetHost></targetHosts><maxRateLimitTPSThreshold>1234</maxRateLimitTPSThreshold><maxRateLimitTPSWarning>1233.95</maxRateLimitTPSWarning><maxRateLimitTPMThreshold>1234</maxRateLimitTPMThreshold><maxRateLimitTPMWarning>1233.95</maxRateLimitTPMWarning></context></contexts></api>";
//$api = Api::fromXML(simplexml_load_string($xmlString));
//echo $api->toXML();