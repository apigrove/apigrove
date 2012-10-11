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
 * File Description Goes Here
 *
 * Date: 8/27/12
 *
 */
require_once APPLICATION_PATH . "/controllers/JsonPropertyPrinter.php";
require_once APPLICATION_PATH."/models/Auth.class.php";
require_once APPLICATION_PATH."/managers/AuthManager.class.php";
require_once "flow/FlowController.php";
require_once "SharedViewUtility.php";

class AuthController extends FlowController
{
    public function getFlowFile(){
        return APPLICATION_PATH."/flows/auth-flow.xml";
    }

    public function indexAction(){
        $authManager = new AuthManager();
        $auths = $authManager->getAllAuths();
        $this->view->messages = $this->getZendFlashMessenger()->getMessages();
        $this->view->auths = $auths;
    }

    public function deleteAction(){
        $registry = Zend_Registry::getInstance();
        $translate = $registry->get("Zend_Translate");
        $authManager = new AuthManager();
        $authManager->deleteAuth($this->_getParam("id"));
        $this->_helper->FlashMessenger($translate->translate("Auth Deleted"));
        $this->redirectToUrl("/auth");
    }


    /**
     * This is the on-enter callback for the "form" state
     * It will load the object needed to back the form.
     * @param $action
     * @param $flowScope
     */
    public function loadFormBacker($action, &$flowScope){
        $registry = Zend_Registry::getInstance();
        $translate = $registry->get("Zend_Translate");
        // Default to using the id from the $flowScope
        $id = @$flowScope['authid'];
        // If that is empty then we use the one from the request
        if(empty($id))
            $id = $this->_getParam("id");

        if(empty($id))
            throw new Zend_Controller_Action_Exception($translate->translate('Resource Not Found'), 404);

        $howMany = !isset($flowScope['howMany'])?"1":$flowScope['howMany'];
        $flowScope['howMany'] = $howMany;

        // Set the id is the flowscope
        $flowScope['authid'] = $id;
        $auth = @$flowScope['auth'];

        /**
         * If the id is "create" and we haven't filled out the form before
         * then we need to create a new Auth and set some defaults
         */
        if($id === "create" && $auth === null){
            $auth = new Auth();
            $auth->id = $id;
            $auth->type = AuthType::$AUTHKEY;
            if(isset($flowScope['preSelectedAuthType'])){
                $auth->type = $flowScope['preSelectedAuthType'];
                $flowScope['authTypeLocked'] = true;
            }
            $auth->apiContext = "actx";
            $auth->policyContext = "pctx";
            $flowScope['isNew'] = true;
        }
        /**
         * If we are updating and we have not filled out the form before then load it from AG
         */
        else if($auth === null){
            $authManager = new AuthManager();
            $auth = $authManager->getAuth($id);
            $flowScope['isNew'] = false;

            $flowScope["relatedProperties"] = JsonPropertyPrinter::getRelatedFromAuth($auth);
        }

        /**
         * Put the auth in the flow scope for reference by other states and the views
         */
        $flowScope['auth'] = $auth;

    }

    /**
     * On-exit callback for the "form" state
     * it should take the form submission and deserialize it into an Auth object and
     * stick it on the flowScope.
     *
     * @param $action
     * @param $flowScope
     */
    public function deserializeForm($action, &$flowScope){
        /**
         * @var Auth $auth
         */
        $auth = $flowScope['auth'];
        $flowScope['howMany'] = $flowScope['isNew']?$this->_getParam('howMany'):"1";

        // Only accept the id if we are creating a new one
        if($flowScope['isNew'])
            $auth->id = $this->_getParam('authid');

        $auth->type = $this->_getParam('type');
        switch($auth->type){
            case AuthType::$AUTHKEY:
                $auth->authKeyAuth->keyValue = $this->_getParam("authKey");
                break;
            case AuthType::$BASIC:
                $auth->basicAuth->username = $this->_getParam("username");
                $auth->basicAuth->password = $this->_getParam("password");
                break;
            case AuthType::$WSSE:
                $auth->wsseAuth->username = $this->_getParam("username");
                $auth->wsseAuth->password = $this->_getParam("password");
                $auth->wsseAuth->passwordType = WSSEPasswordType::PLAINTEXT;
                break;
            case AuthType::$IPWHITELIST:
                $ipList = $this->_getParam("ipWhiteList");
                if(!empty($ipList))
                    $auth->ipWhiteListAuth->ips = explode(',',$ipList);

                break;
        }

        $auth->status = $this->_getParam('status');

        $auth->headerTransformations = SharedViewUtility::deserializeHeaderTransformations($this->getRequest());
        $auth->properties = SharedViewUtility::deserializeProperties($this->getRequest());
        $auth->tdrData = SharedViewUtility::deserializeTdrRules($this->getRequest());

        $flowScope['auth'] = $auth;
    }



    /**
     * on-enter callback for the "submit" state.
     * Should validate the data and then submit the data to AG
     *
     * @param $action
     * @param $flowScope
     */
    public function validateFormAndSubmit($action, &$flowScope){
        $registry = Zend_Registry::getInstance();
        $translate = $registry->get("Zend_Translate");
        $validationErrors = array();
        $isNew = $flowScope['isNew'];
        /**
         * @var Auth $auth
         */
        $auth = $flowScope['auth'];
        $howMany = $flowScope['howMany'];
        if($isNew && empty($howMany))
            $validationErrors['howMany'] = "Please choose how many Auths you would like";

        $shouldValidateCreds = ($isNew && $howMany==="1") || !$isNew;

        if($shouldValidateCreds && $auth->type === AuthType::$AUTHKEY && empty($auth->authKeyAuth->keyValue)){
            $validationErrors['authKey'] = $translate->translate("For authKey auth, you must specify a key");
        }

        if($shouldValidateCreds && ($auth->type === AuthType::$BASIC || $auth->type === AuthType::$WSSE)
            && empty($auth->basicAuth->username) && empty($auth->wsseAuth->username)){
            $validationErrors['username'] = $translate->translate("Username is required");
        }
        if($shouldValidateCreds && ($auth->type === AuthType::$BASIC || $auth->type === AuthType::$WSSE)
            && empty($auth->basicAuth->password) && empty($auth->wsseAuth->password)){
            $validationErrors['password'] = $translate->translate("Password is required");
        }
        if($shouldValidateCreds && $auth->type === AuthType::$IPWHITELIST && empty($auth->ipWhiteListAuth->ips)){
            $validationErrors['ipWhiteList'] = $translate->translate("IP list is required");
        }

        SharedViewUtility::validateHeaderTransformations($auth->headerTransformations, $validationErrors);
        SharedViewUtility::validateProperties($auth->properties, $validationErrors);
        SharedViewUtility::validateTdrRules($auth->tdrData, $validationErrors);

        if(count($validationErrors) > 0){
            $flowScope['validationErrors'] = $validationErrors;
            return "invalid";
        }

        /**
         * Submit to AG
         */
        $authManager = new AuthManager();

        $creds = array();
        for($i = 0; $i < $howMany; $i++){
            // If we are doing a batch create, then generate the credentials
            if($howMany > 1){
                switch($auth->type){
                    case AuthType::$AUTHKEY:
                        $auth->authKeyAuth->keyValue = uniqid();
                        break;
                    case AuthType::$BASIC:
                        $auth->basicAuth->username = uniqid();
                        $auth->basicAuth->password = uniqid();
                        break;
                    case AuthType::$WSSE:
                        $auth->wsseAuth->username = uniqid();
                        $auth->wsseAuth->password = uniqid();
                        break;
                }
                $auth->id = "";
            }

            $result = $authManager->setAuth($auth, $isNew);
            if($result->getHTTPCode() !== "200"){
                $xml = simplexml_load_string($result->getPayload());
                $validationErrors['default'] = (string) $xml->error->errorText;
                $flowScope['validationErrors'] = $validationErrors;
                return "invalid";
            }

                       // Pull the credentials out for display
            switch($auth->type){
                case AuthType::$AUTHKEY:
                    $creds[$auth->id] = "key: ".$auth->authKeyAuth->keyValue;
                    break;
                case AuthType::$BASIC:
                    $creds[$auth->id] = "u/p: ".$auth->basicAuth->username." / ".$auth->basicAuth->password;
                    break;
                case AuthType::$WSSE:
                    $creds[$auth->id] = "u/p: ".$auth->wsseAuth->username." / ".$auth->wsseAuth->password;
                    break;
                case AuthType::$IPWHITELIST:
                    $creds[$auth->id] = implode("; ",$auth->ipWhiteListAuth->ips);
                    break;
            }
        }

        if($isNew){
            $this->_helper->FlashMessenger("Successfully Created Auth(s)");
            foreach($creds as $key=>$value)
                $this->_helper->FlashMessenger("$key ($value)");
        }
        else{
            $this->_helper->FlashMessenger("Successfully Updated Auth");
        }

        $flowScope['authIds'] = array_keys($creds);

        return "valid";
    }

}
