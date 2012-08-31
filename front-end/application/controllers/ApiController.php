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
require_once APPLICATION_PATH . "/models/Api.class.php";
require_once APPLICATION_PATH . "/models/Context.class.php";
require_once APPLICATION_PATH . "/models/Status.class.php";

require_once "flow/FlowController.php";
require_once "Logging/POFileLogger.php";
require_once "SharedViewUtility.php";

class ApiController extends FlowController {

    /**
     * @var POLogger
     */
    protected $logger = null;

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
     * @return POLogger
     */
    public function getLogger(){
        if($this->logger === null) {
            $this->logger = new POFileLogger("/tmp/ApiController.log");
        }
        return $this->logger;
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
        $apiid = $this->_getParam("id");
        if(!empty($apiid)){
            try{
                $response = $this->getManager()->deleteApi($apiid);
                if($response){
                    if((string)$response->status === "SUCCESS"){
                        //success!
                    } else {
                        $this->_helper->FlashMessenger("Error deleting api $apiid : ".$response->error->errorText);
                    }
                } else {
                    $this->_helper->FlashMessenger("The delete action errored but didn't say why!");
                }
            } catch(Exception $e){
                $this->_helper->FlashMessenger("Exception when deleting api $apiid : ".$e->getMessage());
            }
        } else {
            $this->_helper->FlashMessenger("Api id is empty!");
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
        $apiid = $this->_getParam("id");

        if(!isset($flowScope["api"])) {
            if($apiid === "create"){
                $flowScope["apiid"] = null;
                $api = new Api();
                $flowScope["api"] = $api;
            } else {
                $flowScope["apiid"] = $apiid;
                try{
                    $api = $this->getManager()->getApi($apiid);
                    $flowScope["api"] = $api;
                } catch (Exception $e){
                    $this->_helper->FlashMessenger("Error fetching api $apiid : ".$e->getMessage());
                    $this->_helper->redirector("index", "api");
                }
            }
        }

        if(!isset($flowScope["validationErrors"])){
            $flowScope["validationErrors"] = array();
        }
    }

    function api_validate_form1($action, &$flowScope){
        /**
         * @var Api $api
         */
        $api = $flowScope["api"];
        $validationErrors = array();

        if(isset($_POST["apiName"]) && !empty($_POST["apiName"])){
            $api->displayName = $_POST["apiName"];
        } else {
            $validationErrors["name"] = "Apis must have a name.";
        }

        if(isset($_POST["apiEndpoint"]) && !empty($_POST["apiEndpoint"])){
            $api->endpoint = $_POST["apiEndpoint"];
        } else {
            $validationErrors["endpoint"] = "Apis must have an endpoint.";
        }

        $targetHosts = array();
        foreach ($_POST as $k => $v){
            if(preg_match('/^targetHost[0-9]+$/', $k) && !empty($v)){
                $th = new TargetHost();
                $th->url = $v;
                $targetHosts[] = $th;
            }
        }

        if(empty($targetHosts)) {
            $validationErrors["targethost0"] = "Apis must have at least one targethost.";
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

        // If I don't have access to the view, set error messages in the flow scope
        $flowScope["validationErrors"]= $validationErrors;
        return count($validationErrors) === 0 ? "valid" : "invalid";
    }

    function target_host_is_bad($th){
        if(empty($th)){
            return "This targetHost cannot be empty";
        }
        return false;
    }

    function api_validate_form2($action, &$flowScope){
        /**
         * @var Api $api
         */
        $api = $flowScope["api"];
        $validationErrors = array();

        $this->getLogger()->log(print_r($_POST, true));

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
            $validationErrors["auth"] = "Apis must have at least one auth type.";
        }

        if($_POST["https"]){
            $https = new HTTPSType();
            $https->setEnabled("true");
            $https_mode = $_POST["https-mode"];
            if(empty($https_mode) || TLSMode::fromString($https_mode) === null){
                $validationErrors["https-mode"] = "With https on, TLS Mode must be 1way or 2way";
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
            if(isset($_POST[$tpx]) && !empty($_POST[$tpx]) ){
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
                            $validationErrors[$tpx] = "Transactions-per-second warning trigger must be a number";
                            break;
                        case "tps-threshold":
                            $validationErrors[$tpx] = "Transactions-per-second cutoff threshold must be a number";
                            break;
                        case "tpm-warn":
                            $validationErrors[$tpx] = "Transactions-per-minute warning trigger must be a number";
                            break;
                        case "tpm-threshold":
                            $validationErrors[$tpx] = "Transactions-per-minute cutoff threshold must be a number";
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
        SharedViewUtility::validateHeaderTransformations($api->getHeaderTransformations, $validationErrors);

        $properties = SharedViewUtility::deserializeProperties($this->getRequest());
        $api->setProperties($properties);
        SharedViewUtility::validateProperties($properties, $validationErrors);

        $tdrRules = SharedViewUtility::deserializeTdrRules($this->getRequest());
        $api->setTdrData($tdrRules);
        SharedViewUtility::validateTdrRules($tdrRules, $validationErrors);

        // If I don't have access to the view, set error messages in the flow scope
        $flowScope["validationErrors"]= $validationErrors;
        return count($validationErrors) === 0 ? "valid" : "invalid";
    }

    function auth_key_key_is_bad($authkeykey){
        if(empty($authkeykey)){
            return "Auth Key Key cannot be empty";
        }
        return null;
    }

    function api_post_new($action, &$flowScope){
        $manager = $this->getManager();

        /**
         * @var Api $api
         */
        $api = $flowScope["api"];
        $apiid = $api->getId();
        $xmlresponse = array();
        try{
            $xmlresponse = $manager->setApi($api, empty($apiid));
        } catch(Exception $e){
            $xmlresponse->error = array("errorText"=>$e->getMessage());
        }

        if( (string)$xmlresponse->status === "SUCCESS" && isset($xmlresponse->id) ) {
            return "success";
        } else {
            if(isset($xmlresponse->error)) {
                $flowScope["validationErrors"]= array("default"=>"Error: ".(string)$xmlresponse->error->errorText);
            } else {
                $flowScope["validationErrors"]= array("default"=>"Unknown error when posting this Api");
            }
            return "failed";
        }

    }

    function api_success($action, &$flowScope){
        $this->_helper->FlashMessenger("Success");
        $this->_helper->redirector("index", "api");
    }

    public function callAction() {
        $apiid = $this->_getParam("id");
        $api = $this->getManager()->getApi($apiid);
        $config = new Zend_Config_Ini(APPLICATION_PATH . '/configs/manager.ini');
        $this->view->url = "http://" . $config->manager_host . "/" . $api->endpoint;
    }

    public function makecallAction(){
        $ch = curl_init();
        $url = $this->_getParam('url');
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
        $output = curl_exec($ch);
        curl_close($ch);
        $this->_helper->layout()->disableLayout();
        $this->_helper->viewRenderer->setNoRender(true);
        echo $output;
    }
}

