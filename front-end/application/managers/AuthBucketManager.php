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
 * Class useful to manage API datas in the database
 * @author Guillaume Quemart
 *
 */

require_once 'PolicyManager.class.php';
class AuthBucketManager {

    /**
     *
     * Retrieve an authentication bucket
     * @param string $policyID
     * @param string $abId
     * @throws Exception
     * @return AuthIdsType
     */
    public function getAuthBucket($policyId, $abID){
        $ab = new AuthIdsType();
        $ab->setId($abID);
        $url = E3_PROV_URL_POLICY."/".rawurlencode($policyId)."/quotaRLBuckets/".rawurlencode($ab->getId());
        $restClient = new RestClient();
        $reply = $restClient->makeCall($url, "GET");
        $xml = simplexml_load_string($reply->getPayload());
        $ab = AuthIdsType::fromXML($xml->api);
        return $ab;
    }

    /**
     *
     * Retrieve all AuthIdsType
     * @param boolean $allDatas
     * @return array(AuthIdsType)
     */
    public function getAllAuthBucketsForPolicy($policyId){
        $allABs = array();
        $url = E3_PROV_URL_POLICY."/".rawurlencode($policyId)."/quotaRLBuckets";
        $restClient = new RestClient();
        $reply = $restClient->makeCall($url, "GET");
        $xml = simplexml_load_string($reply->getPayload());

        foreach($xml->ids->id as $id){
            $allABs[] = $this->getAuthBucket($policyId, (string) $id);
        }

        return $allABs;
    }

    /**
     *
     * Insert/Update an AuthIdsType on the database
     * @param AuthIdsType $bucket
     * @throws Exception
     */
    public function setAuthIdsType($policyId, AuthIdsType &$bucket, $insertMode = FALSE){

        $method = "PUT";
        $url = E3_PROV_URL_POLICY."/".rawurlencode($policyId)."/quotaRLBuckets/".rawurlencode($bucket->getId());
        if($insertMode){
            $method = "POST";
            $url = E3_PROV_URL_POLICY."/".rawurlencode($policyId)."/quotaRLBuckets/";
        }
        /**
         * Send the XML payload the the Provisioning Backend
         */
        $restClient = new RestClient();
        $reply = $restClient->makeCall($url, $method, $bucket->toXML());
        if($insertMode){
            $bucket = new AuthIdsType();
            if ($bucket->getId() == NULL){
                $xml = simplexml_load_string($reply->getPayload());
                $bucket->setId((string) $xml->id);
            }
        }

    }
    /**
     *
     * Delete a AuthIdsType from the backend
     * @param string $policyID	id of the bucket
     * @throws Exception
     * @return bool
     */
    public function deleteAuthBucket($policyId, $bucketId){
        $method = "DELETE";
        $url = E3_PROV_URL_POLICY."/".rawurlencode($policyId)."/quotaRLBuckets/".rawurlencode($bucketId);
        $restClient = new RestClient();
        $reply = $restClient->makeCall($url, $method);
        if($reply->getHTTPCode() === "200"){
            return true;
        }
        else{
            $xml = simplexml_load_string($reply->getPayload());
            drupal_set_message((string)$xml->status,'error');
            return false;
        }
    }
}