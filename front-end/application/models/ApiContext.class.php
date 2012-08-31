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

require_once 'LoadBalancing.class.php';
/**
 * Context class to match that on the provisioning backend.
 *
 * Date: 3/28/12
 *
 */
class ApiContext
{
    /**
     * @var String
     */
    public $id;

    /**
     * @var boolean
     */
    public $default = true;

    /**
     * The environment status: active, inactive, pending
     */
    public $status;

    /**
     * The target hosts defined for the environment.
     * Array of E3TargetHost
     */
    public $targetHosts = array();

    /**
     * Max Rate Limit Transaction Per Second (TPS) Threshold
     * @required
     */
    public $maxRateLimitTPSThreshold = 0;

    /**
     * Max Rate Limit Transaction Per Second (TPS) Warning
     * @required
     */
    public $maxRateLimitTPSWarning = 0;

    /**
     * Max Rate Limit Transaction per Minute (TPM) Threshold
     * @required
     */
    public $maxRateLimitTPMThreshold = 0;

    /**
     * Max Rate Limit TPM Warning
     * @required
     */
    public $maxRateLimitTPMWarning = 0;

    /**
     * Load Balancing management
     * @required
     */
    public $loadBalancing;
    
    /**
     * Constructor to initialize the LoadBalancing object
     */
    public function ApiContext(){
        $this->loadBalancing = new LoadBalancing();
    }

    /**
     * Serialization function that we've moved to another file so it is cleaner/easier to read.
     * @return string
     */
    public function toXML(){
        ob_start();
        include('templates/ApiContext.tpl.php');
        return ob_get_clean();
    }

    public static function fromXML(SimpleXMLElement $xml){
        $context = new ApiContext();
        $context->setDefault(($xml['default'] == "true"));
        $context->setId((string)$xml['id']);
        $context->setStatus(Status::fromXML($xml->status));
        $context->setMaxRateLimitTPMThreshold((int)$xml->maxRateLimitTPMThreshold);
        $context->setMaxRateLimitTPMWarning((int)$xml->maxRateLimitTPMWarning);
        $context->setMaxRateLimitTPSThreshold((int)$xml->maxRateLimitTPSThreshold);
        $context->setMaxRateLimitTPSWarning((int)$xml->maxRateLimitTPSWarning);

        if(!empty($xml->loadBalancing)){
          $context->setLoadBalancing(LoadBalancing::fromXML($xml->loadBalancing));
        }
        
        $ths= array();
        foreach($xml->targetHosts->targetHost as $targetHostXML){
            $targetHost = TargetHost::fromXML($targetHostXML);
            $ths[] = $targetHost;
        }

        $context->setTargetHosts($ths);
        return $context;
    }

    public function setMaxRateLimitTPMThreshold($maxRateLimitTPMThreshold)
    {
        $this->maxRateLimitTPMThreshold = $maxRateLimitTPMThreshold;
    }

    public function getMaxRateLimitTPMThreshold()
    {
        return $this->maxRateLimitTPMThreshold;
    }

    public function setMaxRateLimitTPMWarning($maxRateLimitTPMWarning)
    {
        $this->maxRateLimitTPMWarning = $maxRateLimitTPMWarning;
    }

    public function getMaxRateLimitTPMWarning()
    {
        return $this->maxRateLimitTPMWarning;
    }

    public function setMaxRateLimitTPSThreshold($maxRateLimitTPSThreshold)
    {
        $this->maxRateLimitTPSThreshold = $maxRateLimitTPSThreshold;
    }

    public function getMaxRateLimitTPSThreshold()
    {
        return $this->maxRateLimitTPSThreshold;
    }

    public function setMaxRateLimitTPSWarning($maxRateLimitTPSWarning)
    {
        $this->maxRateLimitTPSWarning = $maxRateLimitTPSWarning;
    }

    public function getMaxRateLimitTPSWarning()
    {
        return $this->maxRateLimitTPSWarning;
    }

    public function setStatus($status)
    {
        $this->status = $status;
    }

    public function getStatus()
    {
        return $this->status;
    }
    
    public function setLoadBalancing($loadBalancing)
    {
        $this->loadBalancing = $loadBalancing;
    }

    public function getLoadBalancing()
    {
        return $this->loadBalancing;
    }

    public function setTargetHosts($targetHosts)
    {
        $this->targetHosts = $targetHosts;
    }

    public function getTargetHosts()
    {
        return $this->targetHosts;
    }

    /**
     * @param boolean $default
     */
    public function setDefault($default)
    {
        $this->default = $default;
    }

    /**
     * @return boolean
     */
    public function getDefault()
    {
        return $this->default;
    }

    /**
     * @param String $id
     */
    public function setId($id)
    {
        $this->id = $id;
    }

    /**
     * @return String
     */
    public function getId()
    {
        return $this->id;
    }

}
