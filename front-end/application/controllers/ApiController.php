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
 * Controller for all API related activities
 *
 * Date: 8/20/12
 *
 */
require_once APPLICATION_PATH . "/managers/ApiManager.class.php";
require_once APPLICATION_PATH . "/managers/PolicyManager.class.php";
require_once APPLICATION_PATH . "/models/Api.class.php";
require_once APPLICATION_PATH . "/models/Context.class.php";
require_once APPLICATION_PATH . "/models/Status.class.php";
require_once APPLICATION_PATH . "/controllers/AuthController.php";
require_once APPLICATION_PATH . "/controllers/PolicyController.php";

require_once "flow/FlowController.php";
require_once "Logging/LoggerInterface.php";
require_once "SharedViewUtility.php";


class ApiController extends FlowController {

    /**
     * @var ApiManager
     */
    protected $manager = null;

    /**
     *
     * @return ApiManager
     */
    public function getManager(){
        if($this->manager === null){
            $this->manager = new ApiManager();
        }
        return $this->manager;
    }

    /**
     * Function to handle requests to show the list of APIs
     */
    public function listAction(){
        $this->view->apis = $this->getManager()->getAllApis();
        $flashMessenger = $this->_helper->getHelper('FlashMessenger');

        // If I have the view available, set it directly.
        $this->view->messages = $flashMessenger->getMessages();
    }

    /**
     * Function to delete apis
     */
    public function deleteAction(){
        $registry = Zend_Registry::getInstance();
        $translate = $registry->get("Zend_Translate");
        $apiid = $this->_getParam("id");
        if(!empty($apiid)){
            try{
                $response = $this->getManager()->deleteApi($apiid);
                if($response){
                    if((string)$response->status === "SUCCESS"){
                        //success!
                        $this->_helper->FlashMessenger($translate->translate("Api Successfully Deleted"));
                    } else {
                        $this->_helper->FlashMessenger($translate->translate("Error deleting api ") . $apiid . " : ".$response->error->errorText);
                    }
                } else {
                    $this->_helper->FlashMessenger($translate->translate("The delete action errored but didn't say why!"));
                }
            } catch(Exception $e){
                $this->_helper->FlashMessenger($translate->translate("Exception when deleting api ") .$apiid . " : ".$e->getMessage());
            }
        } else {
            $this->_helper->FlashMessenger($translate->translate("Api id is empty!"));
        }
        $this->_helper->redirector("index", "api");
    }

    /**
     * This function and all below it are called by the flow module.
     *
     * @return string
     */
    public function getFlowFile(){
        return APPLICATION_PATH."/flows/api-flow.xml";
    }

    /**
     * This function will guarantee that an api object exists in the
     * view object and in the flowscope.
     *
     * This function will set a null apiid in the view and flowscope if
     * this is a new api.
     *
     * @param $action
     * @param $flowScope
     */
    function fill_form($action, &$flowScope){
        $registry = Zend_Registry::getInstance();
        $translate = $registry->get("Zend_Translate");
        $apiid = $this->_getParam("id");

        if(!isset($flowScope["api"])) {
            if($apiid === "create"){
                $flowScope["apiid"] = null;
                $api = new Api();
                $api->setStatus(Status::$ACTIVE);
                $provAuth = new ProvisionAuthentication();
                $provAuth->setAuths(array(AuthType::$NOAUTH));
                $api->setAuthentication($provAuth);
                $context = new ApiContext();
                $context->setId("actx");
                $context->setMaxRateLimitTPMThreshold(-1);
                $context->setMaxRateLimitTPMWarning(-1);
                $context->setMaxRateLimitTPSWarning(-1);
                $context->setMaxRateLimitTPSThreshold(-1);
                $api->setContexts(array($context));
                $methods = array("GET","POST","PUT","DELETE");
                $api->setAllowedHttpMethods($methods);
                $flowScope["api"] = $api;

            } else {
                $flowScope["apiid"] = $apiid;
                try{
                    $api = $this->getManager()->getApi($apiid);
                    $flowScope["api"] = $api;
                } catch (Exception $e){
                    $this->_helper->FlashMessenger($translate->translate("Error fetching api ") . $apiid . " : ".$e->getMessage());
                    $this->_helper->redirector("index", "api");
                }
            }
        }

        if(!isset($flowScope["validationErrors"])){
            $flowScope["validationErrors"] = array();
        }
    }

    function api_validate_form1($action, &$flowScope){
        $registry = Zend_Registry::getInstance();
        $translate = $registry->get("Zend_Translate");
        /**
         * @var Api $api
         */
        $api = $flowScope["api"];
        $validationErrors = array();

        if(isset($_POST["apiName"]) && !empty($_POST["apiName"])){
            $api->displayName = $_POST["apiName"];
        } else {
            $validationErrors["name"] = $translate->translate("Apis must have a name.");
        }

        if(isset($_POST["apiEndpoint"]) && !empty($_POST["apiEndpoint"])){
            $api->endpoint = $_POST["apiEndpoint"];
        } else {
            $validationErrors["endpoint"] = $translate->translate("Apis must have an endpoint.");
        }

        $targetHosts = array();
        foreach ($_POST as $k => $v){
            if(preg_match('/^targethost[0-9]+$/', $k) && !empty($v)){
                $th = new TargetHost();
                $th->url = $v;
                $targetHosts[] = $th;
            }
        }

        if(empty($targetHosts)) {
            $validationErrors["targethost0"] = $translate->translate("Apis must have at least one targethost.");
        } else {
            $contexts = $api->getContexts();
            if(empty($contexts)){
                $contexts = array(new ApiContext());
            }
            $context = $contexts[0];
            $context->setStatus(Status::$ACTIVE);
            $context->targetHosts = array();
            $i = 0;
            foreach($targetHosts as $th){
                $isbad = $this->target_host_is_bad($th);
                if($isbad){
                    $validationErrors["targethost".$i] = $isbad;
                }
                $context->targetHosts[] = $th;
            }
            $api->setContexts($contexts);
        }

        if($_POST["apienabled"]){
            $api->setStatus(Status::$ACTIVE);
        } else {
            $api->setStatus(Status::$INACTIVE);
        }

        $authTypes = array();
        $authkeykey = null;
        foreach ($_POST as $k => $v){
            $matches = array();
            if(preg_match('/^auth-(\w*)$/', $k, $matches) && $v == 1){
                $authType = isset($matches[1])?AuthType::fromString($matches[1]) : null;
                if(!empty($authType)){
                    $authTypes[] = $authType;
                    if($authType === AuthType::$AUTHKEY){
                        $authkeykey = $_POST["authkey-key"];
                        $isbad = $this->auth_key_key_is_bad($authkeykey);
                        if($isbad){
                            $validationErrors["authkey-key"] = $isbad;
                        }
                    }
                }
            }
        }

        if(!empty($authTypes)){
            $provAuth = new ProvisionAuthentication();
            $provAuth->setAuths($authTypes);
            $provAuth->setAuthKey($authkeykey);
            $api->setAuthentication($provAuth);
        } else {
            $validationErrors["auth"] = $translate->translate("Apis must have at least one auth type.");
        }

        if($_POST["https"]){
            $https = new HTTPSType();
            $https->setEnabled("true");
            $https_mode = $_POST["https-mode"];
            if(empty($https_mode) || TLSMode::fromString($https_mode) === null){
                $validationErrors["https-mode"] = $translate->translate("With https on, TLS Mode must be 1way or 2way");
            } else {
                $https->setTlsMode(TLSMode::fromString($https_mode));
            }
            $api->setHttps($https);
        } else {
            $https = new HTTPSType();
            $https->setEnabled(false);
            $api->setHttps($https);
        }

        foreach(array("tps-warn","tps-threshold","tpm-warn","tpm-threshold") as $tpx){
            if(isset($_POST[$tpx]) ){
                if(is_numeric($_POST[$tpx])){
                    $contexts = $api->getContexts();
                    /**
                     * @var ApiContext $context
                     */
                    $context = $contexts[0];
                    switch($tpx){
                        case "tps-warn":
                            $context->setMaxRateLimitTPSWarning($_POST[$tpx]);
                            break;
                        case "tps-threshold":
                            $context->setMaxRateLimitTPSThreshold($_POST[$tpx]);
                            break;
                        case "tpm-warn":
                            $context->setMaxRateLimitTPMWarning($_POST[$tpx]);
                            break;
                        case "tpm-threshold":
                            $context->setMaxRateLimitTPMThreshold($_POST[$tpx]);
                            break;
                    }
                } else {
                    switch($tpx){
                        case "tps-warn":
                            $validationErrors[$tpx] = $translate->translate("Transactions-per-second warning trigger must be a number");
                            break;
                        case "tps-threshold":
                            $validationErrors[$tpx] = $translate->translate("Transactions-per-second cutoff threshold must be a number");
                            break;
                        case "tpm-warn":
                            $validationErrors[$tpx] = $translate->translate("Transactions-per-minute warning trigger must be a number");
                            break;
                        case "tpm-threshold":
                            $validationErrors[$tpx] = $translate->translate("Transactions-per-minute cutoff threshold must be a number");
                            break;
                    }
                }
            }
        }

        $methods = $api->getAllowedHttpMethods();
        $methods = array_diff($methods, array("GET"));
        if($_POST["method-get"]){
            $methods[] = "GET";
        }
        $methods = array_diff($methods, array("POST"));
        if($_POST["method-post"]){
            $methods[] = "POST";
        }
        $methods = array_diff($methods, array("PUT"));
        if($_POST["method-put"]){
            $methods[] = "PUT";
        }
        $methods = array_diff($methods, array("DELETE"));
        if($_POST["method-delete"]){
            $methods[] = "DELETE";
        }
        $api->setAllowedHttpMethods($methods);

        $headerTransformations = SharedViewUtility::deserializeHeaderTransformations($this->getRequest());
        $api->setHeaderTransformations($headerTransformations);
        $api->setHeaderTransformationEnabled(count($headerTransformations) > 0 );
        SharedViewUtility::validateHeaderTransformations($api->getHeaderTransformations(), $validationErrors);

        $properties = SharedViewUtility::deserializeProperties($this->getRequest());
        $api->setProperties($properties);
        SharedViewUtility::validateProperties($properties, $validationErrors);

        $tdrsenabled = (boolean)$_POST["tdrsenabled"];
        $api->setTdrEnabled($tdrsenabled);

        $tdrRules = SharedViewUtility::deserializeTdrRules($this->getRequest());
        $api->setTdrData($tdrRules);
        SharedViewUtility::validateTdrRules($tdrRules, $validationErrors);

        // If I don't have access to the view, set error messages in the flow scope
        $flowScope["validationErrors"]= $validationErrors;

        $flowScope['gotoAuthsubflow'] = ($action === "submitAndAuth");
        return count($validationErrors) === 0 ? "valid" : "invalid";
    }

    function target_host_is_bad($th){
        $registry = Zend_Registry::getInstance();
        $translate = $registry->get("Zend_Translate");
        if(empty($th)){
            return $translate->translate("This targethost cannot be empty");
        }
        return false;
    }

    function auth_key_key_is_bad($authkeykey){
        $registry = Zend_Registry::getInstance();
        $translate = $registry->get("Zend_Translate");
        if(empty($authkeykey)){
            return $translate->translate("Auth Key Key cannot be empty");
        }
        return null;
    }

    function api_post_new($action, &$flowScope){
        $registry = Zend_Registry::getInstance();
        $translate = $registry->get("Zend_Translate");
        $manager = $this->getManager();

        /**
         * @var Api $api
         */
        $api = $flowScope["api"];
        $apiid = $api->getId();
        $xmlresponse = new stdClass();
        try{
            $xmlresponse = $manager->setApi($api, empty($apiid));
        } catch(Exception $e){
            $xmlresponse->status = "FAILED";
            $xmlresponse->error = new stdClass();
            $xmlresponse->error->errorText = $e->getMessage();
        }

        if( (string)$xmlresponse->status === "SUCCESS" && isset($xmlresponse->id) ) {
            if($flowScope['gotoAuthsubflow']){
                return "successAuth";
            }
            else
                return "success";
        } else {
            if(isset($xmlresponse->error)) {
                $flowScope["validationErrors"]= array("default"=>$translate->translate("Error: ").(string)$xmlresponse->error->errorText);
            } else {
                $flowScope["validationErrors"]= array("default"=>$translate->translate("Unknown error when posting this Api"));
            }
            return "failed";
        }

    }

    /**
     * Callback function for the on-exit of the Auth subflow
     * We have already created the API and the Auth(s) so now we need to create a Policy
     * to hook them together
     * @param $action
     * @param $flowScope
     */
    public function postAuthSubflow($action, &$flowScope){
        $registry = Zend_Registry::getInstance();
        $translate = $registry->get("Zend_Translate");
        $authIds = $flowScope['authIds'];
        /**  @var Api $api*/
        $api = $flowScope['api'];
        $policy = PolicyController::createBasicPolicy(null, array($api->id), $authIds);

        $pm = new PolicyManager();
        $result = $pm->createPolicy($policy);
        if($result){
            $this->_helper->FlashMessenger($translate->translate("Policy Created: ").$policy->id);
        }
        else{
            $this->_helper->FlashMessenger($translate->translate("Error creating Policy"));
        }
    }

    /**
     * Callback function for the on-enter of the Auth subflow state
     * @param $action
     * @param $flowScope
     */
    public function preAuthSubflow($action, &$flowScope){
        // Pass the id to the subflow and tell it to do create
        $flowScope['authid'] = 'create';
        /** @var $api Api */
        $api = $flowScope['api'];
        $authTypes = $api->getAuthentication()->getAuths();
        foreach($authTypes as $authType){
            // Find the first type that is not noAuth
            if($authType !== AuthType::$NOAUTH){
                $flowScope['preSelectedAuthType'] = $authType;
                break;
            }
        }
    }

    public function callAction() {
        $apiid = $this->_getParam("id");
        $api = $this->getManager()->getApi($apiid);
        $config = new Zend_Config_Ini(APPLICATION_PATH . '/configs/manager.ini');
        $this->view->url = "http://" . $config->gateway_host . "/" . $api->endpoint;
    }

    public function makecallAction(){
        $ch = curl_init();
        $url = $this->_getParam('url');
        $params = $this->_getParam('params');
        $method = $this->_getParam('method');
        $headers = $this->_getParam('headers');
        $body = $this->_getParam('body');
        if(empty($headers)) $headers = array();
        if(empty($params)) $params = array();

        $url .= "?";
        $queryString = "";
        foreach($params as $key => $value){
            $queryString .= urlencode($key)."=".urlencode($value)."&";
        }

        if($method !== "GET"){

            if($method === "POST"){
                curl_setopt($ch, CURLOPT_POST, 1);
            }
            else if($method === "PUT"){
                curl_setopt($ch, CURLOPT_PUT, 1);
            }
            curl_setopt($ch, CURLOPT_CUSTOMREQUEST, $method);

            curl_setopt($ch, CURLOPT_POSTFIELDS, $body);

        }

        $url .= $queryString;

        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
        curl_setopt($ch, CURLOPT_VERBOSE, 1);
        curl_setopt($ch, CURLOPT_HEADER, 1);


        $response = curl_exec($ch);
        $header_size = curl_getinfo($ch, CURLINFO_HEADER_SIZE);
        $header = substr($response, 0, $header_size);
        $body = substr($response, $header_size);

        curl_close($ch);
        $this->_helper->layout()->disableLayout();
        $this->_helper->viewRenderer->setNoRender(true);

        echo json_encode(array('header'=>$header,'body'=>$body));;
    }
}

