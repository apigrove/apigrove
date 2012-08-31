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

require_once "ForwardProxy.class.php";
require_once "ConnectionParameters.class.php";
/**
 *
 * Object representation of Target for an API
 * @author Guillaume Quemart
 *
 */
class TargetHost {

    /**
     * @var
     */
    public $url;

    /**
     * @var Authentication
     *
     */
    public $authentication;
    
    /**
     * @var string
     * 
     */
    public $site;

    /**
     * @var string
     *
     */
    public $proxy;


    public $connectionParameters;

    public function __construct(){
        $this->connectionParameters = new ConnectionParameters();
    }

    public function toXML(){
        ob_start();
        include('templates/TargetHost.tpl.php');
        return ob_get_clean();
    }

    public static function fromXML(SimpleXMLElement $xml){
        $th = new TargetHost();
        $th->setAuthentication(Authentication::fromXML($xml->authentication));
        $th->setUrl((string)$xml->url);
        $th->setSite((string)$xml->site);
        if ($xml->forwardProxy) $th->setProxy(ForwardProxy::fromXML($xml->forwardProxy));
        if ($xml->connectionParameters) $th->setConnectionParameters(ConnectionParameters::fromXML($xml->connectionParameters));

        return $th;
    }


    /**
     * @param string $url
     * @return TargetHost
     */
    public function setUrl($url)
    {/*
        if(!is_string($url)){
            throw new Exception(t('Variable $url must be a string.'));
        }
        if(strlen($url) > 255){
            throw new Exception(t('Max length for $url is 255 characters.'));
        }*/
        $this->url = $url;
    }

    /**
     * @return string
     */
    public function getUrl()
    {
        return $this->url;
    }

    /**
     * @param  $authentication
     */
    public function setAuthentication($authentication)
    {
        $this->authentication = $authentication;
    }

    /**
     * @return
     */
    public function getAuthentication()
    {
        return $this->authentication;
    }
    
    /**
     * @param string $site
     */
    public function setSite($site)
    {
        $this->site = $site;
    }
    
    /**
     * @return string
     */
    public function getSite()
    {
        return $this->site;
    }

    /**
     * @param ForwardProxy $proxy
     */
    public function setProxy($proxy)
    {
        $this->proxy = $proxy;
    }

    /**
     * @return string
     */
    public function getProxy()
    {
        return $this->proxy;
    }

    /**
     * @param ConnectionParameters $connectionParameters
     */
    public function setConnectionParameters($connectionParameters)
    {
        $this->connectionParameters = $connectionParameters;
    }

    /**
     * @return string
     */
    public function getConnectionParameters()
    {
        return $this->connectionParameters;
    }



}