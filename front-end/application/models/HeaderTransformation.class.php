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
 * @version $Id$
 * @copyright 6/25/12 4:41 PM
 */

require_once "HeaderTransformationAction.php";
require_once "HeaderTransformationType.php";
require_once "HeaderTransformationTiming.php";

class HeaderTransformation
{
    public $name;
    public $property;
    public $value;
    public $type;
    public $action;

    /**
     * This is a transient variable without a similar field on
     * ApiGrove.  It is used to differentiate between static and property types of
     * header transformations in UI.
     * @var string
     */
    public $kind;


    public function toXML() {
        ob_start();
        include('templates/HeaderTransformation.tpl.php');
        return ob_get_clean();

    }

    public static function fromXML(SimpleXMLElement $xml){
        $transform = new HeaderTransformation();
        $transform->name = (string) $xml['name'];
        $transform->property = (string) $xml['property'];
        $transform->value = (string) $xml['value'];
        $transform->type = (string) $xml['type'];
        $transform->action = (string) $xml['action'];

        if(!empty($transform->value)){
            $transform->kind = HeaderTransformationType::STATIC_VAL;
        }
        else if(!empty($transform->property)){
            $transform->kind = HeaderTransformationType::PROPERTY;
        }

        return $transform;
    }


    public function getName() {
        return $this->name;
    }
    public function setName($value) {
        $this->name = $value;
    }

    public function getProperty() {
        return $this->property;
    }
    public function setProperty($value) {
        $this->property = $value;
    }

    public function getValue() {
        return $this->value;
    }
    public function setValue($value) {
        $this->value = $value;
    }

    public function getType() {
        return $this->type;
    }
    public function setType($value) {
        $this->type = $value;
    }

    public function getAction() {
        return $this->action;
    }
    public function setAction($value) {
        $this->action = $value;
    }


}
