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
 * FlowState class
 * 
 * Represents a state in a flow
 */

require_once('StateTransition.inc');
require_once('FlowStateType.inc');

class FlowState{
	/**
	 * @var string - the flow-unique id of the state
	 */
	public $id = '';
	
	/**
	 * The FlowStateType of this state 
	 * @var FlowStateType
	 */
	public $type;
	
	/**
	 * An array of variables that exists as long as the user is in
	 * the state
	 * @var array
	 */
	public $stateScope = array();
	
	/**
	 * Transitions defined by XML
	 * @var array
	 */
	public $transitions = array();
	
	/**
	 * A callback that should be executed when 
	 * a user enters the state. If the callback is defined the onEnterExec will be ignored.
	 * @var string - function name
	 */
	public $onEnterCallback = '';
	
	/**
	 * A string that contains PHP code that may be executed
	 * If the callback function is defined this attribute will be ignored
	 * @var string 
	 */
	public $onEnterExec = '';
	
	/**
	 * A callback that will be executed just before the user transitions to a new state
	 * @var string - function name
	 */
	public $onExitCallback = '';
	
	/**
	 * A chunk of php that may be executed
	 * If the callback for onExit is defined then this attribute will be ignored.
	 * @var string - function name
	 */
	public $onExitExec = '';

	/**
	 * The name of the view template to use (also known as the theme definition for drupal)
	 * @var string
	 */
	public $view = '';

    /**
     * This is the Flow that represents the subflow entered in this state
     * @var Flow
     */
    public $subflow;

    /**
     * The name of the controller for this subflow
     * @var string
     */
    public $subflowControllerName;
	
	/**
	 * Constructor takes a SimpleXMLElement, and loads it's properties
	 * 
	 * @param SimpleXMLElement $element
	 */
	public function __construct(SimpleXMLElement $element){
		
		// If the xml element is not a 'state' element then don't know what to do, throw an error
		if(strcasecmp('state', $element->getName()) != 0){
			echo "FlowState __construct, cannot process SimpleXMLElement '{$element->getName()}'";
			exit();
		}

		$atts = $element->attributes();
		$this->id = (string) $atts->id;
		$this->view = (string) $atts->view;
		$this->type = (string) $atts->type;
        $this->subflowControllerName = (string) $atts->subflow;

		if(empty($this->type)){
			// Default to STANDARD types
		 	$this->type = FlowStateType::STANDARD;
		}
		
		// Pull the transitions out
		$trans = $element->xpath("transition");
		foreach($trans as $node){
			$t = new StateTransition($node);
			$this->transitions[$t->on] = $t;
		}
		
		// Pull the on-enter out
		$onEnters = $element->xpath('on-enter');
		// This element is not required
		if(!empty($onEnters)){
			$onEnter = $onEnters[0];
			$this->onEnterCallback = (string) $onEnter->attributes()->callback;
			$this->onEnterExec = (string) $onEnter; // This stuff is defined as the content of the tag
		}
		
		// Pull the on-exit out
		$onExits = $element->xpath('on-exit');
		// This element is not required
		if(!empty($onExits)){
			$onExit = $onExits[0];
			$this->onExitCallback = (string) $onExit->attributes()->callback;
			$this->onExitExec = (string) $onExit; // This stuff is defined as the content of the tag
		}
		
	}
	
}