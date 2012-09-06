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


require_once "RestClient/RestClient.class.php";
require_once "Logging/LoggerInterface.php";

/**
 *
 * Class useful to manage API datas in the database
 * @author Guillaume Quemart
 *
 */
class ApiManager {
    protected $currentApi;

    protected $restClient = null;

    public function __construct(){
        $config = new Zend_Config_Ini(APPLICATION_PATH . '/configs/manager.ini');

        $this->restClient = new RestClient($config->manager_host,
            $config->manager_protocol, $config->manager_port, $config->manager_basicauth);

    }

    /**
     *
     * Retrieve API Datas
     * @param string $apiID
     * @param boolean $allDatas
     * @throws Exception
     * @return Api
     */
    public function getApi($apiID){
        $api = new Api();
        $api->setId($apiID);
        $path = "/cxf/e3/prov/v1/apis/".rawurlencode($apiID);
        $reply = $this->restClient->makeCall($path, "GET");
        $xml = simplexml_load_string($reply->getPayload());
        if(!empty($xml->error)){
            throw new Exception($xml->error->errorText);
        }
        $api = Api::fromXML($xml->api);
        return $api;
    }

    /**
     *
     * Retrieve all Api ids the DataBase
     * @return array(String)
     */
    public function getAllApiIds(){
        $allApis = array();
        $path = "/cxf/e3/prov/v1/apis";
        $reply = $this->restClient->makeCall($path, "GET");
        $xml = simplexml_load_string($reply->getPayload());

        return $xml->ids;
    }
    
    /**
     *
     * Retrieve all Api store in the DataBase
     * @return array(E3Api)
     */
    public function getAllApis(){
        $allApis = array();
        $path = "/cxf/e3/prov/v1/apis";
        $reply = $this->restClient->makeCall($path, "GET");
        $xml = simplexml_load_string($reply->getPayload());

        if($xml->error){
            print_r("There was an error in ApiManager->getAllApis()!");
        }

        foreach($xml->ids->id as $id){
            $allApis[(string) $id] = $this->getApi((string) $id);
        }
        return $allApis;
    }

    /**
     *
     * Insert/Update an Api on the database
     * @param Api $api
     * @throws Exception
     */
    public function setApi(Api &$api, $insertMode = FALSE){

        $method = "PUT";
        $path = "/cxf/e3/prov/v1/apis/".rawurlencode($api->getId());
        if($insertMode){
            $method = "POST";
            $path = "/cxf/e3/prov/v1/apis/";
        }
        /**
         * Send the XML payload the the Provisioning Backend
         */
        $api->toXML();
        LoggerInterface::log(($insertMode ? "Creating" : "Updating") . " API: {$api->toXML()}\nEndpoint: ($method) $path", LoggerInterface::INFO);
        $reply = $this->restClient->makeCall($path, $method, $api->toXML());
        $xml = simplexml_load_string($reply->getPayload());

        return $xml;
    }
    /**
     *
     * Delete an API on the database
     * @param string $apiID	API_ID of the API
     * @throws Exception
     * @return bool
     */
    public function deleteApi($apiID){
        $method = "DELETE";
        $path = "/cxf/e3/prov/v1/apis/".rawurlencode($apiID);
        $reply = $this->restClient->makeCall($path, $method);
        if($reply->getHTTPCode() === "200"){
            return simplexml_load_string($reply->getPayload());
        } else {
            return false;
        }
    }
}
