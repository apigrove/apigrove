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
 * Policy class definition
 *
 * Date: 4/12/12
 *
 */

require_once 'TdrData.class.php';
require_once 'TdrType.class.php';
require_once 'Context.class.php';
require_once 'AuthIdsType.class.php';
require_once 'DynamicTdr.class.php';
require_once 'StaticTdr.class.php';
require_once 'Counter.class.php';
require_once 'Status.class.php';
require_once 'Action.class.php';

class Policy
{
    /**
     * @var string
     */
    public $id;

    /**
     * @var array
     */
    public $apiIds = array();

    /**
     * @var array of AuthIdsType
     * @required
     */
    public $authIds = array();

    /**
     * @var array
     * @required
     */
    public $contexts = array();

    /**
     * @var string
     */
    public $tdrOnLimitReached;

    /**
     * @var TdrData
     */
    public $tdr;

    /**
     * @var string
     */
    public $headerTransformations;

    /**
     * @var string
     */
    public $properties;

    public function toXML(){
        ob_start();
        include('templates/Policy.tpl.php');
        return ob_get_clean();
    }

    public static function fromXML(SimpleXMLElement $xml){
        $policy = new Policy();
        $policy->id = (string) $xml->id;
        if ($xml->apiIds->apiId) {
            foreach($xml->apiIds->apiId as $apiId){
                $policy->apiIds[] = current($apiId);
            }
        }
        if ($xml->authIds->quotaRLBucket) {
            foreach($xml->authIds->quotaRLBucket as $bucket){
                $policy->authIds[] = AuthIdsType::fromXML($bucket);
            }
        }
        if ($xml->contexts->context) {
            foreach($xml->contexts->context as $contextXML){
                $policy->contexts[] = Context::fromXML($contextXML);
            }
        }
        $policy->tdrOnLimitReached = (string) $xml->tdrOnLimitReached['type'];
        $policy->tdr = ($xml->tdr ? TdrData::fromXML($xml->tdr) : new TdrData());


        if ($xml->properties && $xml->properties->property) {
            foreach ($xml->properties->property as $prop) {
                $policy->properties[(string)$prop['name']] = (string) $prop;
            }
        }
        if(!empty($policy->properties)){
            ksort($policy->properties);
        }
        if ($xml->headerTransformations && $xml->headerTransformations->headerTransformation) {
            foreach ($xml->headerTransformations->headerTransformation as $transform) {
                $policy->headerTransformations[] = HeaderTransformation::fromXML($transform);
            }
        }

        return $policy;
    }

    /**
     * @param array $apiIds
     */
    public function setApiIds($apiIds)
    {
        $this->apiIds = $apiIds;
    }

    /**
     * @return array
     */
    public function getApiIds()
    {
        return $this->apiIds;
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
     * @param array $contexts
     */
    public function setContexts($contexts)
    {
        $this->contexts = $contexts;
    }

    /**
     * @return array
     */
    public function getContexts()
    {
        return $this->contexts;
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
     * @param \TdrData $tdr
     */
    public function setTdr($tdr)
    {
        $this->tdr = $tdr;
    }

    /**
     * @return \TdrData
     */
    public function getTdr()
    {
        return $this->tdr;
    }

    /**
     * @param string $tdrOnLimitReached
     */
    public function setTdrOnLimitReached($tdrOnLimitReached)
    {
        $this->tdrOnLimitReached = $tdrOnLimitReached;
    }

    /**
     * @return string
     */
    public function getTdrOnLimitReached()
    {
        return $this->tdrOnLimitReached;
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

/*
$policy = new Policy();
$policy->id="abcdef";
$tdrData = new TdrData();

$dt = new DynamicTdr();
$dt->httpHeaderName = "HTTP_HEADER";
$dt->setTdrPropName("propname");
$dt->types[] = TdrType::$API_MAX_RATE_LIMIT;

$tdrData->dynamicTdrs[] = $dt;

$st = new StaticTdr();
$st->setValue("staticValue");
$st->setTdrPropName("staticName");

$st->types[] = TdrType::$API_MAX_RATE_LIMIT;

$tdrData->staticTdrs[] = $st;
$policy->setTdr($tdrData);

$policy->setTdrOnLimitReached("SomeString");

$policy->apiIds[] = "auth123";

$bucket1 = new AuthIdsType();
$bucket1->setId("bucket1");
$bucket1->authIds[] = "auth1";

$bucket2 = new AuthIdsType();
$bucket2->setId("bucket1");
$bucket2->authIds[] = "auth1";
$policy->authIds[] = $bucket1;
$policy->authIds[] = $bucket2;

$counter1 = new Counter();
$counter1->setAction(Action::$IGNORE);
$counter1->setStatus(Status::$ACTIVE);
$counter1->setThreshold(500);
$counter1->setWarning(250);
$c1 = new Context();
$c1->setId("context1");
$c1->setQuotaPerDay($counter1);
$c1->setQuotaPerMonth($counter1);
$c1->setQuotaPerWeek($counter1);
$c1->setRateLimitPerMinute($counter1);
$c1->setRateLimitPerSecond($counter1);
$c1->setStatus(Status::$ACTIVE);

$policy->contexts[] = $c1;
echo $policy->toXML();
    <policy>
        <id>9f124744-5151-4277-8b94-c18c2ce6fd57</id>
        <apiIds>
            <apiId>f1ddabae-62fa-4889-8296-76665b640c8b</apiId>
        </apiIds>
        <authIds>
            <quotaRLBucket id="d9063092-76ee-486b-bb0c-9d938c9b855c">
                <authId>6e8620d4-9432-41c8-9992-aba313db875b</authId>
            </quotaRLBucket>
        </authIds>
        <contexts>
            <context id="cf52550f-f18f-40df-ac6a-af1a30c523cb">
                <status>pending</status>
                <quotaPerDay>
                    <status>pending</status>
                    <action>reject</action>
                    <warning>0.15</warning>
                    <threshold>1121010532</threshold>
                </quotaPerDay>
                <quotaPerWeek>
                    <status>inactive</status>
                    <action>ignore</action>
                    <warning>0.38</warning>
                    <threshold>1983882444</threshold>
                </quotaPerWeek>
                <quotaPerMonth>
                    <status>pending</status>
                    <action>reject</action>
                    <warning>0.78</warning>
                    <threshold>194412587</threshold>
                </quotaPerMonth>
                <rateLimitPerSecond>
                    <status>inactive</status>
                    <action>ignore</action>
                    <warning>0.63</warning>
                    <threshold>2064032808</threshold>
                </rateLimitPerSecond>
                <rateLimitPerMinute>
                    <status>inactive</status>
                    <action>reject</action>
                    <warning>0.11</warning>
                    <threshold>1584186911</threshold>
                </rateLimitPerMinute>
            </context>
        </contexts>
        <tdrOnLimitReached type="05bc4ed8-eacb-43f5-9bb9-8235e2f84f21"/>
        <tdr>
            <static tdrPropName="c427b786-e731-4f8f-8df5-482ab5a25b11" value="857340ac-d571-4442-b0c1-602007ca5ad4">
                <types>
                    <type>CompanyQuota</type>
                    <type>Billing</type>
                    <type>DeveloperApplicationQuotaRateLimit</type>
                    <type>Billing</type>
                    <type>Billing</type>
                    <type>APIMaxRateLimit</type>
                    <type>APIMaxRateLimit</type>
                    <type>APIMaxRateLimit</type>
                    <type>DeveloperApplicationQuotaRateLimit</type>
                    <type>CompanyQuota</type>
                </types>
            </static>
            <dynamic tdrPropName="cc59c997-6429-4673-b4b7-09dcc7bc41cc" httpHeaderName="d0784881-33b5-45f0-b4b8-28a51c5ef252">
                <types>
                    <type>DeveloperApplicationQuotaRateLimit</type>
                    <type>Billing</type>
                    <type>APIMaxRateLimit</type>
                    <type>APIMaxRateLimit</type>
                    <type>APIMaxRateLimit</type>
                    <type>Billing</type>
                    <type>CompanyQuota</type>
                    <type>APIMaxRateLimit</type>
                    <type>CompanyQuota</type>
                    <type>DeveloperApplicationQuotaRateLimit</type>
                </types>
            </dynamic>
        </tdr>
    </policy>
*/