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
        /**
         * @var Auth $auth
         */
        $auth = $flowScope['auth'];

        if($auth->type === AuthType::$AUTHKEY && empty($auth->authKeyAuth->keyValue)){
            $validationErrors['authKey'] = $translate->translate("For authKey auth, you must specify a key");
        }

        if(($auth->type === AuthType::$BASIC || $auth->type === AuthType::$WSSE)
            && empty($auth->basicAuth->username)){
            $validationErrors['username'] = $translate->translate("Username is required");
        }
        if(($auth->type === AuthType::$BASIC || $auth->type === AuthType::$WSSE)
            && empty($auth->basicAuth->password)){
            $validationErrors['password'] = $translate->translate("Password is required");
        }
        if($auth->type === AuthType::$IPWHITELIST && empty($auth->ipWhiteListAuth->ips)){
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
        $result = $authManager->setAuth($auth, $flowScope['isNew']);
        if($result->getHTTPCode() !== "200"){
            $xml = simplexml_load_string($result->getPayload());
            $validationErrors['default'] = (string) $xml->error->errorText;
            $flowScope['validationErrors'] = $validationErrors;
            return "invalid";
        }

        return "valid";
    }

}
