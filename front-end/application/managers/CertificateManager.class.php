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
 * Class definition for the Certificate Manager
 *
 * Will provide all CRUD functionality for Auths
 *
 * Date: 4/17/12
 *
 */
define('E3_PROV_URL_KEY', E3_PROV_URL . "/cxf/e3/prov/v1/keys");
define('E3_PROV_URL_TRUSTSTORE', E3_PROV_URL . "/cxf/e3/prov/v1/truststore");

class CertificateManager{

    public function getKey($id){
        $url = E3_PROV_URL_KEY."/".rawurlencode($id);
        $restClient = new RestClient();
        $reply = $restClient->makeCall($url, "GET");
        $xml = simplexml_load_string($reply->getPayload());
        $auth = SSLKey::fromXML($xml->key);
        return $auth;
    }

    public function getAllKeys(){
        $alKeys = array();
        $url = E3_PROV_URL_KEY;
        $restClient = new RestClient();
        $reply = $restClient->makeCall($url, "GET");
        $xml = simplexml_load_string($reply->getPayload());
        foreach($xml->ids->id as $id){
            $alKeys[] = $this->getKey((string) $id);
        }

        return $alKeys;
    }

    public function setKey(SSLKey &$key, $insertMode = FALSE){

        $method = "PUT";
        $url = E3_PROV_URL_KEY . "/" . rawurlencode($key->getId());
        if($insertMode){
            $method = "POST";
            $url = E3_PROV_URL_KEY;
        }
        /**
         * Send the XML payload the the Provisioning Backend
         */
        $restClient = new RestClient();
        POLI::log(($insertMode ? "Creating" : "Updating") . " SSLKey: {$key->toXML()}\nEndpoint: ($method) $url", POLI::INFO);
        $reply = $restClient->makeCall($url, $method, $key->toXML());
        if($insertMode){
            if ($key->getId() == NULL){
                $xml = simplexml_load_string($reply->getPayload());
                $key->setId((string) $xml->id);
            }
        }
        return $key;

    }

    public function deleteKey($key_id){
        $method = "DELETE";
        $url = E3_PROV_URL_KEY."/".rawurlencode($key_id);
        $restClient = new RestClient();
        POLI::log("Deleting SSLKey '$key_id' with DELETE: $url", POLI::INFO);
        $reply = $restClient->makeCall($url, $method);
        return true;
    }


    public function getCert($key_id, $cert_id){
        $url = E3_PROV_URL_KEY."/".rawurlencode($key_id)."/certs/".rawurlencode($cert_id);
        $restClient = new RestClient();
        $reply = $restClient->makeCall($url, "GET");
        $xml = simplexml_load_string($reply->getPayload());
        $cert = Cert::fromXML($xml->cert);
        
        // get certificate expiration date
        $content = (string) $xml->cert->content;
        $ts = $this->parseCertificateExpirationDate($content);
        if($ts)
        	$cert->setExpirationDate($ts);
        
        return $cert;
    }

    public function getAllCerts($key_id){
        $alCerts = array();
        $url = E3_PROV_URL_KEY."/".rawurlencode($key_id)."/certs";
        $restClient = new RestClient();
        $reply = $restClient->makeCall($url, "GET");
        $xml = simplexml_load_string($reply->getPayload());
        foreach($xml->ids->id as $id){
            $alCerts[] = $this->getCert($key_id, (string) $id);
        }

        return $alCerts;
    }

    public function setCert($key_id, Cert &$cert, $insertMode = FALSE){

        $method = "PUT";
        $url = E3_PROV_URL_KEY . "/".rawurlencode($key_id)."/certs/" . rawurlencode($cert->getId());
        if($insertMode){
            $method = "POST";
            $url = E3_PROV_URL_KEY . "/".rawurlencode($key_id)."/certs";
        }
        /**
         * Send the XML payload the the Provisioning Backend
         */
        $restClient = new RestClient();
        POLI::log(($insertMode ? "Creating" : "Updating") . " Cert: {$cert->toXML()}\nEndpoint: ($method) $url", POLI::INFO);
        $reply = $restClient->makeCall($url, $method, $cert->toXML());
        if($insertMode){
            if ($cert->getId() == NULL){
                $xml = simplexml_load_string($reply->getPayload());
                $cert->setId((string) $xml->id);
            }
        }
        return $cert;

    }

    public function deleteCert($key_id, $cert_id){
        $method = "DELETE";
        $url = E3_PROV_URL_KEY."/".rawurlencode($key_id)."/certs/" . $cert_id;
        $restClient = new RestClient();
        $reply = $restClient->makeCall($url, $method);
        return true;
    }
    
    
    /* Trust Store */
    public function getCA($id){
    	$url = E3_PROV_URL_TRUSTSTORE.'/certs/'.rawurlencode($id);
    	$restClient = new RestClient();
    	$reply = $restClient->makeCall($url, "GET");
    	$xml = simplexml_load_string($reply->getPayload());
    	$cert = Cert::fromXML($xml->cert);
    	
    	// get certificate expiration date
    	$content = (string) $xml->cert->content;
    	$ts = $this->parseCertificateExpirationDate($content);
    	if($ts)
    		$cert->setExpirationDate($ts);
    	
    	return $cert;
    }
    
    public function getCRL($id){
    	$url = E3_PROV_URL_TRUSTSTORE.'/crls/'.rawurlencode($id);
    	$restClient = new RestClient();
    	$reply = $restClient->makeCall($url, "GET");
    	$xml = simplexml_load_string($reply->getPayload());
    	$crl = SSLCRL::fromXML($xml->crl);
    	
    	// get crl expiration date
    	// crl is base64_encoded
    	$content = (string) $xml->crl->content;
    	$ts = $this->parseCRLDates($content);
    	if($ts) {
    		$crl->setNextUpdateDate($ts);
    	}
    	
    	return $crl;
    }
    
    public function getAllCAs(){
    	$allCAs = array();
    	$url = E3_PROV_URL_TRUSTSTORE.'/certs';
    	$restClient = new RestClient();
    	$reply = $restClient->makeCall($url, "GET");
    	$xml = simplexml_load_string($reply->getPayload());
    	foreach($xml->ids->id as $id){
    		$allCAs[] = $this->getCA((string) $id);
    	}
    
    	return $allCAs;
    }
    
    public function getAllCRLs(){
    	$alCRLs = array();
    	$url = E3_PROV_URL_TRUSTSTORE.'/crls';
    	$restClient = new RestClient();
    	$reply = $restClient->makeCall($url, "GET");
    	$xml = simplexml_load_string($reply->getPayload());
    	foreach($xml->ids->id as $id){
    		$alCRLs[] = $this->getCRL((string) $id);
    	}
    
    	return $alCRLs;
    }
    
    public function setCA(Cert &$cert, $insertMode = FALSE){
    
    	$method = "PUT";
    	$url = E3_PROV_URL_TRUSTSTORE . "/certs/" . rawurlencode($cert->getId());
    	if($insertMode){
    		$method = "POST";
    		$url = E3_PROV_URL_TRUSTSTORE . "/certs";
    	}
    	/**
    	 * Send the XML payload the the Provisioning Backend
    	 */
    	$restClient = new RestClient();
    	POLI::log(($insertMode ? "Creating" : "Updating") . " Cert: {$cert->toXML()}\nEndpoint: ($method) $url", POLI::INFO);
    	$reply = $restClient->makeCall($url, $method, $cert->toXML());
    	if($insertMode){
    		if ($cert->getId() == NULL){
    			$xml = simplexml_load_string($reply->getPayload());
    			$cert->setId((string) $xml->id);
    		}
    	}
    	return $cert;
    }
    
    public function setCRL(SSLCRL &$crl, $insertMode = FALSE){
    
    	$method = "PUT";
    	$url = E3_PROV_URL_TRUSTSTORE . "/crls/".rawurlencode($crl->getId());
    	if($insertMode){
    		$method = "POST";
    		$url = E3_PROV_URL_TRUSTSTORE . "/crls";
    	}
    	/**
    	 * Send the XML payload the the Provisioning Backend
    	 */
    	$restClient = new RestClient();
    	POLI::log(($insertMode ? "Creating" : "Updating") . " CRL: {$crl->toXML()}\nEndpoint: ($method) $url", POLI::INFO);
    	$reply = $restClient->makeCall($url, $method, $crl->toXML());
    	if($insertMode){
    		if ($crl->getId() == NULL){
    			$xml = simplexml_load_string($reply->getPayload());
    			$crl->setId((string) $xml->id);
    		}
    	}
    	return $cert;
    }
    
    public function deleteCA($ca_id){
    	$method = "DELETE";
    	$url = E3_PROV_URL_TRUSTSTORE."/certs/".rawurlencode($ca_id);
    	$restClient = new RestClient();
    	POLI::log("Deleting Certificate Authority '$ca_id' with DELETE: $url", POLI::INFO);
    	$reply = $restClient->makeCall($url, $method);
    	return true;
    }
    
    public function deleteCRL($crl_id){
    	$method = "DELETE";
    	$url = E3_PROV_URL_TRUSTSTORE."/crls/".rawurlencode($crl_id);
    	$restClient = new RestClient();
    	POLI::log("Deleting CRL '$crl_id' with DELETE: $url", POLI::INFO);
    	$reply = $restClient->makeCall($url, $method);
    	return true;
    }
    
    
//     private function parseCertificateExpirationDate($content) {
//     	$array = openssl_x509_parse($content, false);
//     	if($array)
//     		return $array['validTo_time_t'];
//     	else
//     		return false;
//     }
    
    private function parseCertificateExpirationDate($content) {
        $tmpfile = tempnam(sys_get_temp_dir(), 'crt');
		file_put_contents($tmpfile, $content);
		$cmd = 'openssl x509 -noout -in '.$tmpfile.' -enddate';
		$exec = shell_exec($cmd);
		unlink($tmpfile);
		if($exec) {
			$pattern = '/^notAfter=(.*)$/';
			preg_match($pattern, $exec, $matches);
			if(sizeof($matches) > 0) {
				return $matches[1];
			}
		} else {
			return false;
		}
    }
    
    private function parseCRLDates($content) {
    	$tmpfile = tempnam(sys_get_temp_dir(), 'crl');
		file_put_contents($tmpfile, $content);
		$cmd = 'openssl crl -noout -in '.$tmpfile.' -nextupdate';
		$exec = shell_exec($cmd);
		unlink($tmpfile);
		if($exec) {
			$pattern = '/^nextUpdate=(.*)$/';
			preg_match($pattern, $exec, $matches);
			if(sizeof($matches) > 0) {
				//$time = strtotime($matches[1]);
				return $matches[1];
			}
		} else {
			return false;
		}
    }
    
}