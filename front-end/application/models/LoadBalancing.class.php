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
 * Object representation of the target load balancer configuration
 * @author Guillaume Quemart
 *
 */
class LoadBalancing {
    /**
     * Type of load balancing
     */
    public $type = "roundRobin";

    
    /**
     * Target HealthCheck 
     */
    public $targetHealthCheck;

    /**
     * FailOver management activation
     */
    public $failOver = false;

    /**
     * Response codes used to make balancing
     */
    public $onResponseCode = array();
    

    public function toXML(){
        ob_start();
        include('templates/LoadBalancing.tpl.php');
        return ob_get_clean();
    }

    public function fromXML(SimpleXMLElement $xml){
        $loadBalancing = new LoadBalancing();
        $loadBalancing->setType((string)$xml['type']);
        $loadBalancing->setTargetHealthCheck((string)$xml->targetHealthCheck[type]);
        if(!empty($xml->failOver)){
            $loadBalancing->setFailOver(true);
            $onResponseCode = array();
            foreach($xml->failOver->onResponseCode as $responsCode){
                $onResponseCode[] = (string)$responsCode;
            }
            $loadBalancing->setOnResponseCode($onResponseCode);
        }

        return $loadBalancing;
    }
    
    
    public function setType($type)
    {
      $this->type = $type;
    }
    
    public function getType()
    {
      return $this->type;
    }
    
    public function setTargetHealthCheck($targetHealthCheck)
    {
      $this->targetHealthCheck = $targetHealthCheck;
    }
    
    public function getTargetHealthCheck()
    {
      return $this->targetHealthCheck;
    }
    
    public function setFailOver($failOver)
    {
      $this->failOver = $failOver;
    }
    
    public function getFailOver()
    {
      return $this->failOver;
    }
    
    public function setOnResponseCode($onResponseCode)
    {
      $this->onResponseCode = $onResponseCode;
    }
    
    public function getOnResponseCode()
    {
      return $this->onResponseCode;
    }
}