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
 * Class useful to manage policy datas in the database
 * @author Guillaume Quemart
 *
 */

require_once 'RestClient/RestClient.class.php';
require_once APPLICATION_PATH . '/managers/ApiManager.class.php';
require_once APPLICATION_PATH . '/managers/AuthManager.class.php';

define('E3_PROV_URL_POLICY', '/cxf/e3/prov/v1/policies');

class PolicyManager {
    protected $restClient = null;

    public function __construct(){
        $config = new Zend_Config_Ini(APPLICATION_PATH . '/configs/manager.ini');

        $this->restClient = new RestClient($config->manager_host,
            $config->manager_protocol, $config->manager_port, $config->manager_basicauth);
    }

    public $currentPolicy;

    /**
     *
     * Retrieve policy Datas
     * @param string $policyID
     * @param boolean $allDatas
     * @throws Exception
     * @return Policy
     */
    public function getPolicy($policyID){
        $policy = new Policy();
        $policy->setId($policyID);
        $url = E3_PROV_URL_POLICY."/".rawurlencode($policy->getId());

        $policy = null;
        $xml = null;
        $reply = $this->restClient->makeCall($url, "GET");
        $xml = !empty($reply) ? simplexml_load_string($reply->getPayload()) : null;
        if($reply->getHTTPCode() === "200"){
            $policy = Policy::fromXML($xml->policy);
        } else {
            $error = "Unable to retrieve policy with id: ".$policyID;   // default error msg
            if ($xml != null) {
                $errorText = $xml->xpath('/response/error/errorText');  // error msg from response
                if (count($errorText) > 0) {
                    $error = (string)$errorText[0];
                }
            }
            PolicyManager::error($error);
        }
        return $policy;
    }

    /**
     *
     * Retrieve all Policy store in the DataBase
     * @param boolean $allDatas
     * @return array(E3Policy)
     */
    public function getAllPolicies($allDatas = FALSE){
        $allPolicies = array();
        $url = E3_PROV_URL_POLICY;
        $reply = $this->restClient->makeCall($url, "GET");
        $xml = simplexml_load_string($reply->getPayload());

        foreach($xml->ids->id as $id){
            $allPolicies[(string) $id] = $this->getPolicy((string) $id);
        }

        return $allPolicies;
    }

    /**
     *
     * Insert/Update an Policy on the database
     * @param Policy $policy
     * @throws Exception
     */
    private function _setPolicy(Policy &$policy, $insertMode = FALSE){

        $method = "PUT";
        $url = E3_PROV_URL_POLICY."/".rawurlencode($policy->getId());
        if($insertMode){
            $method = "POST";
            $url = E3_PROV_URL_POLICY;
        }
        /**
         * Send the XML payload the the Provisioning Backend
         */
        LoggerInterface::log(($insertMode ? "Creating" : "Updating") . " Policy: {$policy->toXML()}\nEndpoint: ($method) $url", LoggerInterface::INFO);
        $reply = $this->restClient->makeCall($url, $method, $policy->toXML());
        $xml = simplexml_load_string($reply->getPayload());
        if($reply->getHTTPCode() === "200"){
            if($insertMode){
                $policy = new Policy();
                if ($policy->getId() == NULL){
                    $policy->setId((string) $xml->id);
                }
            }
            return true;
        } else {
            $error = $insertMode ? "Unable to create policy" : "Unable to update policy with id: ".$policy->getId();   // default error msg
            if ($xml != null) {
                $errorText = $xml->xpath('/response/error/errorText');  // error msg from response
                if (count($errorText) > 0) {
                    $error = (string)$errorText[0];
                }
            }
            PolicyManager::error($error);
            return false;
        }
    }
    /**
     *
     * Delete a Policy from the backend
     * @param string $policyID	id of the policy
     * @throws Exception
     * @return bool
     */
    private function _deletePolicy($policyID){
        $method = "DELETE";
        $url = E3_PROV_URL_POLICY."/".rawurlencode($policyID);
        $reply = $this->restClient->makeCall($url, $method);
        if($reply->getHTTPCode() === "200"){
            return true;
        }
        else{
            $error = "Unable to delete policy with id: ".$policyID;   // default error msg
            $xml = simplexml_load_string($reply->getPayload());
            if ($xml != null) {
                $errorText = $xml->xpath('/response/error/errorText');  // error msg from response
                if (count($errorText) > 0) {
                    $error = (string)$errorText[0];
                }
            }
            PolicyManager::error($error);
            return false;
        }
    }

    /*
     * The following section contains methods moved from E3Interface
     */
    private static $error;

    /**
     * If no param, returns last error
     * If param is exception, logs exception and sets error
     * If param is String, sets error
     *
     * @static
     * @param null $err
     * @return mixed
     */
    public static function error($err = null)
    {
        if ($err == null) return PolicyManager::$error;
        if ($err instanceof Exception) {
            PolicyManager::$error = $err->getMessage();
            LoggerInterface::logException($err, LoggerInterface::ERROR);
        } else {
            PolicyManager::$error = $err;
            LoggerInterface::log($err, LoggerInterface::ERROR);
        }
    }

    /**
     * Gets all APIs or returns false on error
     *
     * @param bool $allData
     * @return array(E3Api)|bool
     */
    public function getAllApis($allData = false) {
        $manager = new ApiManager();
        try {
            return $manager->getAllApis($allData);
        } catch (Exception $e) {
            $this->error($e);
        }
        return false;
    }

    /**
     * Gets all Auths or returns false on error
     *
     * @return array(Auth)|bool
     */
    public function getAllAuths() {
        $manager = new AuthManager();
        try {
            return $manager->getAllAuths();
        } catch (Exception $e) {
            $this->error($e);
        }
        return false;
    }

    /**
     * Creates an Auth.  Does no validation; it is assumed that the controller
     * has validated the Auth, and that E3 will return errors if the Auth
     * is not valid.
     *
     * @param Policy $p
     * @return bool
     */
    public function createPolicy(Policy $p) {
        $success = false;
        try {
            $success = $this->_setPolicy($p, true);
        } catch (Exception $e) {
            $this->error($e);
        }
        return $success;
    }

    /**
     * Updates an auth.  Auth should already exist.
     *
     * @param Policy $p
     * @return bool
     */
    public function updatePolicy(Policy $p) {
        $success = false;
        try {
            $success = $this->_setPolicy($p);
        } catch (Exception $e) {
            $this->error($e);
        }
        return $success;

    }


    /**
     * Deletes a policy.  Accepts either policy object or string id.
     * Returns true on success or false on error.
     * @param $p
     * @return bool
     */
    public function deletePolicy($p) {
        $success = false;
        if ($p instanceof Policy) {
            $p = $p->getId();
        }

        try {
            $success = $this->_deletePolicy($p);
        } catch (Exception $e) {
            $this->error($e);
        }
        return $success;
    }

}