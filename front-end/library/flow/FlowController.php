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

include_once('model/Flow.inc');

abstract class FlowController extends Zend_Controller_Action
{
    /**
     * @var Flow flow
     */
    protected $flow;
    /**
     * This is the callback function for ALL flows that the flow module responds to.
     * All flow processing starts here.
     *
     * Request Params supported:
     * _fid - the id of the flow
     * _faction - the name of the action that the user is taking
     *
     */
    function requestAction(){
        $url = parse_url($this->getRequest()->getRequestUri());
        $uri = $url['path'];

        $flowFile = $this->getFlowFile();

        // Pull the flowId out of the request
        $flowId = $this->_getParam('_fid');
        $action = $this->_getParam('_faction');

        $flow = null;

        $session = new Zend_Session_Namespace('flow');

        // If the flowId exists on the request then try to continue the existing flow
        if(!empty($flowId)){
            $flow = unserialize($session->$flowId);
            $this->flow = $flow;
        }

        // If no existing flow then create a new one and stick it in the session
        if(empty($flow)){
            $flow = new Flow($flowFile,  $uri, $this, $this->_request->getControllerName());
            $this->flow = $flow;
            $flow->process("__INIT__");
        }

        // Have to always set the controller, because we serialize it but the next request will have a
        // Different one
        $flow->controller = $this;
        $flowId = $flow->id;

        // If the user actually tried to do something other than refresh the page
        if(!empty($action)){
            // Process the action
            $flow->process($action);

            // Store the result in the session
            $session->$flowId = serialize($flow);

            // Redirect so that the user can refresh, without warnings from the browser
            $this->_helper->redirector->gotoUrl($uri."?_fid={$flow->id}");
        }

        // Otherwise, we will just render using the same state info.
        // Store the result in the session
        $session->$flowId = serialize($flow);

        $flow->render();

        $session->$flowId = serialize($flow);

    }

    abstract function getFlowFile();

    public function redirectToUrl($url){
        $this->_helper->redirector->gotoUrl($url);
    }

    /**
     * @return Zend_Controller_Action_Helper_Abstract
     */
    public function getZendFlashMessenger(){
        return $this->_helper->getHelper('FlashMessenger');
    }
}