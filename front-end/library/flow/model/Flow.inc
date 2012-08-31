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
 * Flow Class
 *
 * A Class that represents all of the state for a flow
 */

require_once('FlowState.inc');

class Flow{

    /**
     * The user-unique flow id
     * @var string
     */
    public $id = '';

    /**
     * The 'drupal absolute' path to the flow definition
     * @var string
     */
    private $xmlPath = '';

    /**
     * The route to the flow (e.g. 'somemodule/someworkflow')
     * @var string
     */
    public $uri = '';

    /**
     * The 'flow scope' for the flow
     * This is just a place to put variables that should live for the entire flow lifetime.
     * @var array
     */
    public $flowScope = array();

    /**
     * This is the object that will be used to call callbacks against.
     * @var FlowController
     */
    public $controller;

    /**
     * The is the canonical name for the controller (lower-case, without the controller part).
     * @var string
     */
    public $controllerName;

    /**
     * The current state of the flow
     * @var FlowState
     */
    public $currentState;

    /**
     * Should just be a message that gets rendered a single time
     * @var String
     */
    public $flashMessage;

    public function __construct($xmlPath, $uri, &$controller, $controllerName){
        $this->uri = $uri;
        $this->xmlPath = $xmlPath;
        $this->controller = $controller;
        $this->controllerName = $controllerName;
    }

    /**
     * Process Flow logic for a given action
     * @param unknown_type $action
     */
    public function process($action){
        /**
         * If this state is actually a subflow then pass the processing down to the subflow
         */
        if(!empty($this->currentState->subflow)){
            $sfAction = $this->currentState->subflow->process($action);

            // We want the subflow to "use" the action up, but pass it's result up to the super flow if an end state
            if($this->currentState->subflow->currentState->type === FlowStateType::END){
                $action = $sfAction;
            }
            // Otherwise just "use" the action up.
            else{
                $action = "";
            }
        }

        /*
         * Special case when a flow is brand new...
         * no action and no transition
         */
        if($action === '__INIT__'){
            $flowxml = simplexml_load_file($this->xmlPath);
            $result = $flowxml->xpath('/flow/state');
            if(!empty($result)){
                // Give it a fairly random id
                $this->id = md5($this->xmlPath.time());
                // Pull the first state out of the XML
                // use it as the initial state
                $xmlState = $result[0];
                $state = new FlowState($xmlState);
                $result = $this->enterState($state, $action);
                if(!empty($result)){
                    return $this->process($result);
                }
            }
            else{
                echo("No states defined in $this->xmlPath");
                exit();
            }
        }
        // If there is a transition defined for this action and state then make the
        // appropriate transition
        else if(array_key_exists($action, $this->currentState->transitions)){
            $transition = $this->currentState->transitions[$action];

            // give the message to drupal
            if(!empty($transition->flashMessage)){
                $this->flashMessage = $transition->flashMessage;
            }

            // pull the new state definition out of the xml
            $flowxml = simplexml_load_file($this->xmlPath);
            $xpath = "/flow/state[@id='{$transition->to}']";
            $states = $flowxml->xpath($xpath);
            $newState = new FlowState($states[0]);

            // do exit processing
            $this->exitState($action);

            // do enter processing
            $result = $this->enterState($newState, $action);

            // If the result is not empty we will try to use the result as the current action
            // want to try to transition.
            if(!empty($result)){
                return $this->process($result);
            }
        }
        else if(empty($action)){
            return '';
        }
        // Else return the original action
        else{
            echo("Could not transition from state: {$this->currentState->id} on action: $action");
            return $action;
        }
    }

    /**
     * Function to do the processing when a user gets into
     * a particular state
     *
     * @param FlowState $newState
     */
    public function enterState(FlowState &$newState, $action=''){
        $this->currentState = $newState;
        $funcName = $this->currentState->onEnterCallback;
        $result = null;
        if(!empty($funcName) && method_exists($this->controller, $funcName)){
            $result = $this->controller->$funcName($action, $this->flowScope);
        }
        else if(!empty($this->currentState->onEnterExec)){
            $result = $this->doEval($this->currentState->onEnterExec);
        }

        // If this state is really a subflow
        if(!empty($this->currentState->subflowControllerName)){
            $this->currentState->subflow = $this->initSubFlow();
            $this->currentState->subflow->process("__INIT__");
        }



        return $result;
    }

    /**
     * Function to do the processing for when a user exits a state
     */
    public function exitState($action=''){
        $funcName = $this->currentState->onExitCallback;
        if(!empty($funcName) && method_exists($this->controller, $funcName)){
            $this->controller->$funcName($action, $this->flowScope);
        }
        // try to execute the code in the body of the on-exit tag
        else{
            if(!empty($this->currentState->onExitExec)){
                $this->doEval($this->currentState->onExitExec);
            }
        }
    }

    /**
     * Provides a shorthand way to get the current view
     */
    public function getCurrentView(){
        // if it is a redirect view then we don't want to append the controller and extension
        if(preg_match('/^redirect:(.*)$/', $this->currentState->view))
            $view = $this->currentState->view;
        else
            $view = $this->controllerName."/".$this->currentState->view.".phtml";

        if(!empty($this->currentState->subflow)){
            $view = $this->currentState->subflow->getCurrentView();
        }

        return $view;
    }

    /**
     * Provides an easy way for views to refer to the current flow
     */
    public function flowURI(){
        return $this->uri."?_fid={$this->id}";
    }

    /**
     * This is a helper function to create the right context and variable scope for
     * this kind of procedure
     *
     * @param string $code - some php that will get evaluated with
     * @return whatever the code evaluates to
     */
    private function doEval($code){
        return eval($code);
    }


    /**
     * Helper function to setup the subflow object
     * @return Flow
     */
    private function initSubFlow(){
        require_once APPLICATION_PATH."/controllers/{$this->currentState->subflowControllerName}Controller.php";
        $controllerClass = $this->currentState->subflowControllerName."Controller";

        $front = Zend_Controller_Front::getInstance();
        // @var FlowController
        $controller = new $controllerClass($front->getRequest(), $front->getResponse());

        $subflow = new Flow($controller->getFlowFile(), $this->uri, $controller, $this->currentState->subflowControllerName);
        $subflow->setFlowScope($this->flowScope);

        return $subflow;
    }

    /**
     * Setting function for subflows to be able to share flow scope with their parent
     * @param $flowScope
     */
    public function setFlowScope(&$flowScope){
        $this->flowScope = $flowScope;
    }


    public function render(){
        $view = $this->getCurrentView();
        if(empty($view)){
            $this->flashMessage = "No view name configured for state: {$this->currentState->id} in $this->xmlPath";
        }
        else{
            $matches = array();
            // If the flow uses the 'redirect:' keyword then we want to do a redirect
            if(preg_match('/^redirect:(.*)$/', $view, $matches)){
                $this->controller->redirectToUrl($matches[1]); // Everything after 'redirect:'
            }
            // No redirect then render the given view
            else{
                // Pass the flow context to the view
                $this->controller->view->flow = $this;
                $this->controller->view->flashMessage = $this->flashMessage;

                $flashMessenger = $this->controller->getZendFlashMessenger();
                $this->controller->view->messages = $flashMessenger->getMessages();

                $this->controller->renderScript($view);//_helper->viewRenderer($view);

                $this->flashMessage = "";
            }
        }
    }
}