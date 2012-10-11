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
 * Date: 9/25/12
 * Time: 10:41 AM
 */

require_once APPLICATION_PATH . "/managers/PolicyManager.class.php";
require_once APPLICATION_PATH . "/managers/AuthManager.class.php";
require_once APPLICATION_PATH . "/managers/ApiManager.class.php";
require_once APPLICATION_PATH . "/models/Policy.class.php";
require_once APPLICATION_PATH . "/models/Auth.class.php";
require_once APPLICATION_PATH . "/models/Api.class.php";

class JsonPropertyPrinter {

    public static function getRelatedFromApi(Api $api){
        $ret = Array();

        $pm = new PolicyManager();
        $am = new AuthManager();

        $allPolicies = $pm->getAllPolicies(true);
        $relatedPolicies = Array();
        $policyPropsArr = Array();

        $allAuths = $am->getAllAuths(true);
        $relatedAuthIds = Array();
        $relatedAuths = Array();
        $authPropsArr = Array();


        foreach($allPolicies as $policy){
            /**
             * @var Policy $policy
             */
            $apiIds = $policy->getApiIds();
            foreach($apiIds as $apiId){
                if($apiId === $api->getId()){
                    $relatedPolicies[] = $policy;
                }
            }
        }

        foreach($relatedPolicies as $policy){
            $props = $policy->getProperties();
            if(!empty($props)){
                $policyPropsArr[$policy->getId()] = array_keys($props);
            }

            foreach($policy->getAuthIds() as $authBucket){
                if($authBucket && $authBucket->getAuthIds()){
                    $relatedAuthIds = array_unique(array_merge($relatedAuthIds, $authBucket->getAuthIds()));
                }
            }
        }

        foreach($allAuths as $auth){
            /**
             * @var $auth Auth
             */
            if(in_array($auth->getId(), $relatedAuthIds)){
                $relatedAuths[] = $auth;
            }
        }

        foreach($relatedAuths as $auth){
            $props = $auth->getProperties();
            if(!empty($props)){
                $authPropsArr[$auth->getId()] = array_keys($props);
            }
        }

        if(!empty($policyPropsArr)){
            $ret["policy"] = $policyPropsArr;
        }
        if(!empty($authPropsArr)){
            $ret["auth"] = $authPropsArr;
        }
        return json_encode($ret);
    }

    public static function getRelatedFromAuth(Auth $auth){
        $ret = Array();

        $pm = new PolicyManager();
        $am = new ApiManager();

        $allPolicies = $pm->getAllPolicies(true);
        $relatedPolicies = Array();
        $policyPropsArr = Array();

        $allApis = $am->getAllApis(true);
        $relatedApiIds = Array();
        $relatedApis = Array();
        $apiPropsArr = Array();


        foreach($allPolicies as $policy){
            /**
             * @var Policy $policy
             */
            foreach($policy->getAuthIds() as $authBucket){
                if($authBucket && $authBucket->getAuthIds() && in_array($auth->getId(), $authBucket->getAuthIds())){
                    $relatedPolicies[] = $policy;
                }
            }
//            foreach($authIds as $authId){
//                if($authId === $auth->getId()){
//                }
//            }
        }

        foreach($relatedPolicies as $policy){
            $props = $policy->getProperties();
            if(!empty($props)){
                $policyPropsArr[$policy->getId()] = array_keys($props);
            }
            $relatedApiIds = array_unique(array_merge($relatedApiIds, $policy->getApiIds()));
        }

        foreach($allApis as $api){
            /**
             * @var $api Api
             */
            if(in_array($api->getId(), $relatedApiIds)){
                $relatedApis[] = $api;
            }
        }

        foreach($relatedApis as $api){
            $props = $api->getProperties();
            if(!empty($props)){
                if($api->getDisplayName()){
                $apiPropsArr[$api->getDisplayName()] = array_keys($props);
            } else {
                $apiPropsArr[$api->getId()] = array_keys($props);
            }
            }
        }

        if(!empty($policyPropsArr)){
            $ret["policy"] = $policyPropsArr;
        }
        if(!empty($apiPropsArr)){
            $ret["api"] = $apiPropsArr;
        }
        return json_encode($ret);
    }

    public static function getRelatedFromPolicy(Policy $policy){
        $ret = Array();

        $authm = new AuthManager();
        $apim = new ApiManager();

        $allAuths = $authm->getAllAuths(true);
        $relatedAuthIds = Array();
        $relatedAuths = Array();
        $authPropsArr = Array();

        $allApis = $apim->getAllApis(true);
        $relatedApiIds = Array();
        $relatedApis = Array();
        $apiPropsArr = Array();



        foreach($allAuths as $auth){
            /**
             * @var $auth Auth
             */
            foreach($policy->getAuthIds() as $authBucket){
                if($authBucket->getAuthIds() && in_array($auth->getId(), $authBucket->getAuthIds())){
                    $relatedAuths[] = $auth;
                }
            }
        }

        foreach($relatedAuths as $auth){
            $props = $auth->getProperties();
            if(!empty($props)){
                $authPropsArr[$auth->getId()] = array_keys($props);
            }
        }

        $relatedApiIds = $policy->getApiIds();

        foreach($allApis as $api){
            /**
             * @var $api Api
             */
            if(in_array($api->getId(), $relatedApiIds)){
                $relatedApis[] = $api;
            }
        }

        foreach($relatedApis as $api){
            $props = $api->getProperties();
            if(!empty($props)){
                if($api->getDisplayName()){
                    $apiPropsArr[$api->getDisplayName()] = array_keys($props);
                } else {
                    $apiPropsArr[$api->getId()] = array_keys($props);
                }
            }
        }


        if(!empty($authPropsArr)){
            $ret["auth"] = $authPropsArr;
        }
        if(!empty($apiPropsArr)){
            $ret["api"] = $apiPropsArr;
        }

        return json_encode($ret);
    }


}
