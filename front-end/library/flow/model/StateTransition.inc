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
 * Transition Class
 * Meant to represent a transition that should occur given 
 * a particular action and should end in the state indicated.
 */

class StateTransition{
	/**
	 * The action that indicates that this transition should be taken
	 * @var string
	 */
	public $on = '';
	
	/**
	 * The state that the transition should end with
	 * @var string
	 */
	public $to = '';
	
	/**
	 * The optional message that will be injected into the drupal system 
	 * when the transition completes
	 * @var string
	 */
	public $flashMessage = '';

	
	public function __construct(SimpleXMLElement $element){
		if(strcasecmp('transition', $element->getName()) != 0){
			echo "StateTransition __construct, cannot process SimpleXMLElement '{$element->getName()}'";
			exit();
		}
		
		$atts = $element->attributes();
		
		$this->on = (string) $atts->on;
		$this->to = (string) $atts->to;
		$this->flashMessage = (string) $atts->flashMessage;
	}
}