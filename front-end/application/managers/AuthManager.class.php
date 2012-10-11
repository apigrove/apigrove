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
 * Class definition for the AuthManager
 *
 * Will provide all CRUD functionality for Auths
 *
 * Date: 4/17/12
 *
 */
define('E3_PROV_URL_AUTH', "/cxf/e3/prov/v1/auths");

require_once "RestClient/RestClient.class.php";
require_once "Logging/LoggerInterface.php";

class AuthManager{

    protected $currentApi;
    protected $restClient = null;

    public function __construct(){
        $config = new Zend_Config_Ini(APPLICATION_PATH . '/configs/manager.ini');

        $this->restClient = new RestClient($config->manager_host,
            $config->manager_protocol, $config->manager_port, $config->manager_basicauth);

    }

    /**
     *
     * Retrieve Auth Datas
     * @param string $authID
     * @throws Exception
     * @return Auth
     */
    public function getAuth($authID){
        $auth = new Auth();
        $auth->setId($authID);
        $url = E3_PROV_URL_AUTH."/".rawurlencode($auth->getId());
        $reply = $this->restClient->makeCall($url, "GET");
        $xml = simplexml_load_string($reply->getPayload());
        $auth = Auth::fromXML($xml->auth);
        return $auth;
    }

    /**
     *
     * Retrieve all Auth store in the DataBase
     * @return array(E3Api)
     */
    public function getAllAuths(){
        $allAuths = array();
        $url = E3_PROV_URL_AUTH;
        $reply = $this->restClient->makeCall($url, "GET");
        $xml = simplexml_load_string($reply->getPayload());

        foreach($xml->ids->id as $id){
            $allAuths[(string) $id] = $this->getAuth((string) $id);
        }

        return $allAuths;
    }

    /**
     * Insert/Update an Api on the database
     * @param Auth $auth
     * @throws Exception
     * @return RestResponse
     */
    public function setAuth(Auth &$auth, $insertMode = FALSE){

        $method = "PUT";
        $url = E3_PROV_URL_AUTH . "/" . rawurlencode($auth->getId());
        if($insertMode){
            $method = "POST";
            $url = E3_PROV_URL_AUTH;
        }
        /**
         * Send the XML payload the the Provisioning Backend
         */
        LoggerInterface::log(($insertMode ? "Creating" : "Updating") . " Auth: {$auth->toXML()}\nEndpoint: ($method) $url", LoggerInterface::INFO);
        $reply = $this->restClient->makeCall($url, $method, $auth->toXML());
        if($insertMode){
            if ($auth->getId() == NULL){
                $xml = simplexml_load_string($reply->getPayload());
                $auth->setId((string) $xml->id);
            }
        }

        return $reply;
    }
    /**
     *
     * Delete an API on the database
     * @param string $authID	API_ID of the API
     * @throws Exception
     * @return bool
     */
    public function deleteAuth($authID){
        $method = "DELETE";
        $url = E3_PROV_URL_AUTH."/".rawurlencode($authID);
        $reply = $this->restClient->makeCall($url, $method);
        if($reply->getHTTPCode() === "200"){
            return true;
        }
        else{
            $xml = simplexml_load_string($reply->getPayload());
            error_log((string)$xml->status);
            return false;
        }
    }
}