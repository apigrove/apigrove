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
 * Utility class to centralize some of the form elements that are shared
 *
 * Date: 8/29/12
 *
 */

require_once APPLICATION_PATH . "/models/HeaderTransformation.class.php";

class SharedViewUtility
{
    /**
     * Will include the snippet of HTML
     * @static
     * @param array $headerTransformations
     * @param array $validationErrors
     */
    public static function includeHeaderTransformationsSnippet($headerTransformations, $validationErrors){
        include APPLICATION_PATH."/views/scripts/snippets/headerTransformations.phtml";
    }

    /**
     * Utility function to pull a list of headerTransformations out of the request
     * @param Zend_Controller_Request_Abstract $request
     * @return mixed
     */
    public static function deserializeHeaderTransformations($request){
        $result = array();
        foreach($request->getParam('header') as $header){
            $ht = new HeaderTransformation();
            $ht->action = $header['action'];
            $ht->name = $header['name'];
            $ht->type = $header['timing'];
            if($header['type'] === HeaderTransformationType::PROPERTY)
                $ht->property = $header['value'];
            else if($header['type'] === HeaderTransformationType::STATIC_VAL)
                $ht->value = $header['value'];

            if(!empty($ht->name))
                $result[] = $ht;
        }

        return $result;
    }

    /**
     * Utility function to validate the list of header transformations
     * @param $hts
     * @param $validationErrors
     */
    public static function validateHeaderTransformations($hts, &$validationErrors){
        /** @var HeaderTransformation $ht */
        for($i = 0; $i < count($hts); $i++){
            $ht = $hts[$i];
            if(empty($ht->name)){
                $validationErrors['header'][$i]['name'] = "Name is required";
            }
            if(empty($ht->type) ||
                ($ht->type !== HeaderTransformationTiming::REQUEST && $ht->type !== HeaderTransformationTiming::RESPONSE )){
                $validationErrors['header'][$i]['timing'] = "Timing is required";
            }

            if(empty($ht->action) ||
                ($ht->action !== HeaderTransformationAction::ADD && $ht->action !== HeaderTransformationAction::REMOVE)){
                $validationErrors['header'][$i]['action'] = "Action is required";
            }
            if($ht->action === HeaderTransformationAction::ADD &&
                empty($ht->property) && empty($ht->value)){
                $validationErrors['header'][$i]['timing'] = "Type and Value are required";
            }
        }
    }

    /**
     * @static
     * @param $properties
     * @param $validationErrors
     */
    public static function includePropertiesSnippet($properties, $validationErrors){
        include APPLICATION_PATH."/views/scripts/snippets/properties.phtml";
    }

    /**
     * Utility function to pull a list of properties out of the request
     * @param Zend_Controller_Request_Abstract $request
     * @return mixed
     */
    public static function deserializeProperties($request){
        $result = array();
        foreach($request->getParam('property') as $prop){
            if(!empty($prop['name']) || !empty($prop['value']))
                $result[$prop['name']] = $prop['value'];
        }

        return $result;
    }

    /**
     * Utility function to validate the list of properties
     * @param $props
     * @param $validationErrors
     */
    public static function validateProperties($props, &$validationErrors){
        $count = 0;
        foreach($props as $key=>$val){
            if(empty($key))
                $validationErrors['property'][$count]['name'] = "Key is required";
            if(empty($val))
                $validationErrors['property'][$count]['value'] = "Value is required";

            $count++;
        }
    }



    /**
     * @static
     * @param TdrData $tdrData
     * @param $validationErrors
     */
    public static function includeTdrRulesSnippet($tdrData, $validationErrors){
        include APPLICATION_PATH."/views/scripts/snippets/tdrRules.phtml";
    }

    /**
     * Utility function to pull a list of TdrRules out of the request
     * @param Zend_Controller_Request_Abstract $request
     * @return TdrData
     */
    public static function deserializeTdrRules($request){
        $result = new TdrData();
        foreach($request->getParam('tdr') as $rule){
            if($rule['type'] === TdrRuleType::DYNAMIC){
                $tdrRule = new DynamicTdr();
                $tdrRule->httpHeaderName = $rule['value'];
                $tdrRule->tdrPropName = $rule['name'];
                $tdrRule->extractFrom = $rule['extractFrom'];
                $result->dynamicTdrs[] = $tdrRule;
            }

            if($rule['type'] === TdrRuleType::STATIC_VAL || $rule['type'] === TdrRuleType::PROPERTY){
                $tdrRule = new StaticTdr();
                $tdrRule->tdrPropName = $rule['name'];
                if($rule['type'] === TdrRuleType::STATIC_VAL){
                    $tdrRule->value = $rule['value'];
                }
                else{
                    $tdrRule->property = $rule['value'];
                }

                $result->staticTdrs[] = $tdrRule;
            }
        }

        return $result;
    }

    /**
     * Utility function to validate the list of tdrRules
     * @param $props
     * @param $validationErrors
     */
    public static function validateTdrRules(TdrData $tdrData, &$validationErrors){
        $count = 0;

        /** @var DynamicTdr $rule */
        foreach($tdrData->dynamicTdrs as $rule){
            if(empty($rule->extractFrom)){
                $validationErrors['tdr'][$count]['extractFrom'] = "Extract from is required";
            }
            if(empty($rule->httpHeaderName)){
                $validationErrors['tdr'][$count]['httpHeaderName'] = "Http Header Name is required";
            }
            if(empty($rule->tdrPropName)){
                $validationErrors['tdr'][$count]['tdrPropName'] = "Name is required";
            }
            $count++;
        }

        /** @var StaticTdr $rule */
        foreach($tdrData->staticTdrs as $rule){
            if(empty($rule->property) && empty($rule->value)){
                $validationErrors['tdr'][$count]['value'] = "Value or Property Name required";
            }
            if(empty($rule->tdrPropName)){
                $validationErrors['tdr'][$count]['tdrPropName'] = "Name is required";
            }
        }

    }
}
