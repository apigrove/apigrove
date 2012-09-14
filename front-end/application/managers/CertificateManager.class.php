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

require_once 'RestClient/RestClient.class.php';
require_once APPLICATION_PATH . '/models/Cert.class.php';
require_once APPLICATION_PATH . '/models/SSLCRL.class.php';
require_once APPLICATION_PATH . '/models/SSLKey.class.php';

define('E3_PROV_URL_KEY', "/cxf/e3/prov/v1/keys");
define('E3_PROV_URL_TRUSTSTORE', "/cxf/e3/prov/v1/truststore");
define('UNDEFINED_ERROR_TEXT', "Undefined server error");

class CertificateManager{

    protected $restClient = null;
    private static $error;

    public function __construct(){
        $config = new Zend_Config_Ini(APPLICATION_PATH . '/configs/manager.ini');

        $this->restClient = new RestClient($config->manager_host,
            $config->manager_protocol, $config->manager_port, $config->manager_basicauth);
    }

    /**
     * If no param, returns last error
     * If param is exception, logs exception and sets error
     * If param is String, sets error
     *
     * @static
     * @param null $err
     * @return mixed
     */
    public static function error($err = null)
    {
        if ($err == null) return CertificateManager::$error;
        if ($err instanceof Exception) {
            CertificateManager::$error = $err->getMessage();
            LoggerInterface::logException($err, LoggerInterface::ERROR);
        } else {
            CertificateManager::$error = $err;
            LoggerInterface::log($err, LoggerInterface::ERROR);
        }
    }

    /*
     * Start functions transferred from E3Interface
     */
    /**
     * gets all keys or returns false on error
     * @return array|bool
     */
    public function getAllKeys()
    {
        try {
            return $this->_getAllKeys();
        } catch (Exception $e) {
            CertificateManager::error($e);
        }
        return false;
    }


    /**
     * gets a specific key by id
     * @param $p
     * @return bool|SSLKey
     */
    public function getKey($p)
    {
        if ($p instanceof SSLKey) {
            $p = $p->getId();
        }
        try {
            return $this->_getKey($p);
        } catch (Exception $e) {
            CertificateManager::error($e);
        }
        return false;
    }

    /**
     * creates a key.  should include key content
     * @param SSLKey $p
     * @return bool
     */
    public function createKey(SSLKey $p)
    {
        try {
            return $this->_setKey($p, true);
        } catch (Exception $e) {
            CertificateManager::error($e);
        }
        return false;
    }

    /**
     * updates a key object.  note, this shouldn't include
     * the content of a key.. only on post
     * @param SSLKey $p
     * @return bool
     */
    public function updateKey(SSLKey $p)
    {
        try {
            return $this->_setKey($p);
        } catch (Exception $e) {
            CertificateManager::error($e);
        }
        return false;

    }


    /**
     * deletes a key
     * @param $p
     * @return bool
     */
    public function deleteKey($p)
    {
        if ($p instanceof SSLKey) {
            $p = $p->getId();
        }

        try {
            return $this->_deleteKey($p);
        } catch (Exception $e) {
            CertificateManager::error($e);
        }
        return false;
    }


    /**
     * get all of key_id's certs
     * @param $key_id
     * @return array|bool
     */
    public function getAllCerts($key_id)
    {
        try {
            return $this->_getAllCerts($key_id);
        } catch (Exception $e) {
            CertificateManager::error($e);
        }
        return false;
    }


    /**
     * gets $key_id's cert $cert_id
     * @param $key_id
     * @param $cert_id
     * @return bool|Cert
     */
    public function getCert($key_id, $cert_id)
    {
        if ($cert_id instanceof Cert) {
            $cert_id = $cert_id->getId();
        }
        try {
            return $this->_getCert($key_id, $cert_id);
        } catch (Exception $e) {
            CertificateManager::error($e);
        }
        return false;
    }

    /**
     * creates $key_id's cert
     * @param $key_id
     * @param Cert $p
     * @return bool
     */
    public function createCert($key_id, Cert $p)
    {
        try {
            return $this->_setCert($key_id, $p, true);
        } catch (Exception $e) {
            CertificateManager::error($e);
        }
        return false;
    }

    /**
     * updates $key_id's cert $cert_id
     * @param $key_id
     * @param Cert $cert
     * @return bool
     */
    public function updateCert($key_id, Cert $cert)
    {
        try {
            return $this->_setCert($key_id, $cert);
        } catch (Exception $e) {
            CertificateManager::error($e);
        }
        return false;

    }


    /**
     * deletes $key_id's cert $cert_id
     * @param $key_id
     * @param $cert_id
     * @return bool
     */
    public function deleteCert($key_id, $cert_id)
    {
        if ($cert_id instanceof Cert) {
            $cert_id = $cert_id->getId();
        }

        try {
            return $this->_deleteCert($key_id,$cert_id);
        } catch (Exception $e) {
            CertificateManager::error($e);
        }
        return false;
    }

    /**
     * gets ca $ca_id
     * @param $ca_id
     * @return bool|Cert
     */
    public function getCA($ca_id)
    {
        try {
            return $this->_getCA($ca_id);
        } catch (Exception $e) {
            CertificateManager::error($e);
        }
        return false;
    }

    /**
     * gets crl $crl_id
     * @param $crl_id
     * @return bool|Cert
     */
    public function getCRL($crl_id)
    {
        try {
            return $this->_getCRL($crl_id);
        } catch (Exception $e) {
            CertificateManager::error($e);
        }
        return false;
    }

    /**
     * gets all certificate authorities or returns false on error
     * @return array|bool
     */
    public function getAllCAs()
    {
        try {
            return $this->_getAllCAs();
        } catch (Exception $e) {
            CertificateManager::error($e);
        }
        return false;
    }

    /**
     * add new certificate authority
     * @param Cert $p
     * @return bool
     */
    public function createCA(Cert $p)
    {
        try {
            return $this->_setCA($p, true);
        } catch (Exception $e) {
            CertificateManager::error($e);
        }
        return false;
    }

    /**
     * add new revocation list
     * @param SSLCRL $p
     * @return bool
     */
    public function createCRL(SSLCRL $p)
    {
        try {
            return $this->_setCRL($p, true);
        } catch (Exception $e) {
            CertificateManager::error($e);
        }
        return false;
    }

    /**
     * gets all revocation lists or returns false on error
     * @return array|bool
     */
    public function getAllCRLs()
    {
        try {
            return $this->_getAllCRLs();
        } catch (Exception $e) {
            CertificateManager::error($e);
        }
        return false;
    }

    /**
     * updates ca $ca
     * @param Cert $cert
     * @return bool
     */
    public function updateCA(Cert $ca)
    {
        try {
            return $this->_setCA($ca);
        } catch (Exception $e) {
            CertificateManager::error($e);
        }
        return false;

    }

    /**
     * updates $key_id's cert $cert_id
     * @param $key_id
     * @param Cert $cert
     * @return bool
     */
    public function updateCRL(SSLCRL $crl)
    {
        try {
            return $this->_setCRL($crl);
        } catch (Exception $e) {
            CertificateManager::error($e);
        }
        return false;

    }

    /**
     * deletes $ca_id certificate authority
     * @param $ca_id
     * @return bool
     */
    public function deleteCA($p)
    {
        if ($p instanceof Cert) {
            $p = $p->getId();
        }

        try {
            return $this->_deleteCA($p);
        } catch (Exception $e) {
            CertificateManager::error($e);
        }
        return false;
    }

    /**
     * deletes $crl_id's crl
     * @param $crl_id
     * @return bool
     */
    public function deleteCRL($p)
    {
        if ($p instanceof SSLCRL) {
            $p = $p->getId();
        }

        try {
            return $this->_deleteCRL($p);
        } catch (Exception $e) {
            CertificateManager::error($e);
        }
        return false;
    }

    /*
    * End functions transferred from E3Interface
    */

    private function _getKey($id)
    {
        $url = E3_PROV_URL_KEY."/".rawurlencode($id);
        $reply = $this->restClient->makeCall($url, "GET");
        $xml = simplexml_load_string($reply->getPayload());
        if ($reply->getHTTPCode() === "200") {
            $auth = SSLKey::fromXML($xml->key);
            return $auth;
        } else {
            throw new Exception(!empty($xml->error) ? $xml->error->errorText : UNDEFINED_ERROR_TEXT);
        }
    }

    private function _getAllKeys()
    {
        $alKeys = array();
        $url = E3_PROV_URL_KEY;
        $reply = $this->restClient->makeCall($url, "GET");
        $xml = simplexml_load_string($reply->getPayload());
        if ($reply->getHTTPCode() === "200") {
            foreach($xml->ids->id as $id) {
                $alKeys[] = $this->getKey((string) $id);
            }
            return $alKeys;
        } else {
            throw new Exception(!empty($xml->error) ? $xml->error->errorText : UNDEFINED_ERROR_TEXT);
        }
    }

    private function _setKey(SSLKey &$key, $insertMode = FALSE)
    {
        $method = "PUT";
        $url = E3_PROV_URL_KEY . "/" . rawurlencode($key->getId());
        if ($insertMode) {
            $method = "POST";
            $url = E3_PROV_URL_KEY;
        }
        /**
         * Send the XML payload the the Provisioning Backend
         */
        $xmlKey = $key->toXML();
        LoggerInterface::log(($insertMode ? "Creating" : "Updating") . " SSLKey: {$xml}\nEndpoint: ($method) $url", LoggerInterface::INFO);
        $reply = $this->restClient->makeCall($url, $method, $xmlKey);
        $xml = simplexml_load_string($reply->getPayload());
        // TODO: figure out why successful adds sometime return code 100
        if (($reply->getHTTPCode() === "200") || ($reply->getHTTPCode() === "100")){
            if ($insertMode) {
                if ($key->getId() == NULL) {
                    $key->setId((string) $xml->id);
                }
            }
            return $key;
        } else {
            throw new Exception(!empty($xml->error) ? $xml->error->errorText : UNDEFINED_ERROR_TEXT);
        }
    }

    private function _deleteKey($key_id)
    {
        $method = "DELETE";
        $url = E3_PROV_URL_KEY."/".rawurlencode($key_id);
        LoggerInterface::log("Deleting SSLKey '$key_id' with DELETE: $url", LoggerInterface::INFO);
        $reply = $this->restClient->makeCall($url, $method);
        $xml = simplexml_load_string($reply->getPayload());
        if ($reply->getHTTPCode() === "200") {
            return true;
        } else {
            throw new Exception(!empty($xml->error) ? $xml->error->errorText : UNDEFINED_ERROR_TEXT);
        }
    }


    private function _getCert($key_id, $cert_id)
    {
        $url = E3_PROV_URL_KEY."/".rawurlencode($key_id)."/certs/".rawurlencode($cert_id);
        $reply = $this->restClient->makeCall($url, "GET");
        $xml = simplexml_load_string($reply->getPayload());
        if ($reply->getHTTPCode() === "200") {
            $cert = Cert::fromXML($xml->cert);
            // get certificate expiration date
            $content = (string) $xml->cert->content;
            $ts = $this->parseCertificateExpirationDate($content);
            if ($ts) {
        	    $cert->setExpirationDate($ts);
            }
            return $cert;
        } else {
            throw new Exception(!empty($xml->error) ? $xml->error->errorText : UNDEFINED_ERROR_TEXT);
        }
    }

    private function _getAllCerts($key_id)
    {
        $alCerts = array();
        $url = E3_PROV_URL_KEY."/".rawurlencode($key_id)."/certs";
        $reply = $this->restClient->makeCall($url, "GET");
        $xml = simplexml_load_string($reply->getPayload());
        if ($reply->getHTTPCode() === "200") {
            foreach($xml->ids->id as $id) {
                $alCerts[] = $this->_getCert($key_id, (string) $id);
            }
            return $alCerts;
        } else {
            throw new Exception(!empty($xml->error) ? $xml->error->errorText : UNDEFINED_ERROR_TEXT);
        }
    }


    private function _setCert($key_id, Cert &$cert, $insertMode = FALSE){

        $method = "PUT";
        $url = E3_PROV_URL_KEY . "/".rawurlencode($key_id)."/certs/" . rawurlencode($cert->getId());
        if ($insertMode) {
            $method = "POST";
            $url = E3_PROV_URL_KEY . "/".rawurlencode($key_id)."/certs";
        }
        /**
         * Send the XML payload the the Provisioning Backend
         */
        $xmlCert = $cert->toXML();
        LoggerInterface::log(($insertMode ? "Creating" : "Updating") . " Cert: {$cert->toXML()}\nEndpoint: ($method) $url", LoggerInterface::INFO);
        $reply = $this->restClient->makeCall($url, $method, $xmlCert);
        $xml = simplexml_load_string($reply->getPayload());
        // TODO: figure out why successful adds sometime return code 100
        if (($reply->getHTTPCode() === "200") || ($reply->getHTTPCode() === "100")) {
            if ($insertMode) {
                if ($cert->getId() == NULL){
                    $cert->setId((string) $xml->id);
                }
            }
            return $cert;
        } else {
            throw new Exception(!empty($xml->error) ? $xml->error->errorText : UNDEFINED_ERROR_TEXT);
        }
    }

    private function _deleteCert($key_id, $cert_id)
    {
        $method = "DELETE";
        $url = E3_PROV_URL_KEY."/".rawurlencode($key_id)."/certs/" . $cert_id;
        $reply = $this->restClient->makeCall($url, $method);
        $xml = simplexml_load_string($reply->getPayload());
        if ($reply->getHTTPCode() === "200") {
            return true;
        } else {
            throw new Exception(!empty($xml->error) ? $xml->error->errorText : UNDEFINED_ERROR_TEXT);
        }
    }
    
    
    /* Trust Store */
    private function _getCA($id)
    {
    	$url = E3_PROV_URL_TRUSTSTORE.'/certs/'.rawurlencode($id);
    	$reply = $this->restClient->makeCall($url, "GET");
    	$xml = simplexml_load_string($reply->getPayload());
        if ($reply->getHTTPCode() === "200") {
            $cert = Cert::fromXML($xml->cert);
    	
    	    // get certificate expiration date
    	    $content = (string) $xml->cert->content;
    	    $ts = $this->parseCertificateExpirationDate($content);
    	    if ($ts) {
    		    $cert->setExpirationDate($ts);
            }
    	    return $cert;
        } else {
            throw new Exception(!empty($xml->error) ? $xml->error->errorText : UNDEFINED_ERROR_TEXT);
        }
    }
    
    private function _getCRL($id)
    {
    	$url = E3_PROV_URL_TRUSTSTORE.'/crls/'.rawurlencode($id);
    	$reply = $this->restClient->makeCall($url, "GET");
    	$xml = simplexml_load_string($reply->getPayload());
        if ($reply->getHTTPCode() === "200") {
            $crl = SSLCRL::fromXML($xml->crl);
    	
    	    // get crl expiration date
    	    // crl is base64_encoded
    	    $content = (string) $xml->crl->content;
    	    $ts = $this->parseCRLDates($content);
    	    if ($ts) {
    		    $crl->setNextUpdateDate($ts);
    	    }
            return $crl;
        } else {
            throw new Exception(!empty($xml->error) ? $xml->error->errorText : UNDEFINED_ERROR_TEXT);
        }
    }
    
    private function _getAllCAs()
    {
    	$allCAs = array();
    	$url = E3_PROV_URL_TRUSTSTORE.'/certs';
    	$reply = $this->restClient->makeCall($url, "GET");
    	$xml = simplexml_load_string($reply->getPayload());
        if ($reply->getHTTPCode() === "200") {
            foreach ($xml->ids->id as $id) {
        		$allCAs[] = $this->_getCA((string) $id);
        	}
    	    return $allCAs;
        } else {
            throw new Exception(!empty($xml->error) ? $xml->error->errorText : UNDEFINED_ERROR_TEXT);
        }
    }
    
    private function _getAllCRLs()
    {
    	$alCRLs = array();
    	$url = E3_PROV_URL_TRUSTSTORE.'/crls';
    	$reply = $this->restClient->makeCall($url, "GET");
    	$xml = simplexml_load_string($reply->getPayload());
        if ($reply->getHTTPCode() === "200") {
            foreach($xml->ids->id as $id) {
    		    $alCRLs[] = $this->_getCRL((string) $id);
    	    }
    	    return $alCRLs;
        } else {
            throw new Exception(!empty($xml->error) ? $xml->error->errorText : UNDEFINED_ERROR_TEXT);
        }
    }
    
    private function _setCA(Cert &$cert, $insertMode = FALSE)
    {
    	$method = "PUT";
    	$url = E3_PROV_URL_TRUSTSTORE . "/certs/" . rawurlencode($cert->getId());
    	if ($insertMode) {
    		$method = "POST";
    		$url = E3_PROV_URL_TRUSTSTORE . "/certs";
    	}
    	/**
    	 * Send the XML payload the the Provisioning Backend
    	 */
    	LoggerInterface::log(($insertMode ? "Creating" : "Updating") . " Cert: {$cert->toXML()}\nEndpoint: ($method) $url", LoggerInterface::INFO);
    	$reply = $this->restClient->makeCall($url, $method, $cert->toXML());
        $xml = simplexml_load_string($reply->getPayload());
        if ($reply->getHTTPCode() === "200") {
            if ($insertMode) {
    		    if ($cert->getId() == NULL) {
    			    $cert->setId((string) $xml->id);
    		    }
    	    }
    	    return $cert;
        } else {
            throw new Exception(!empty($xml->error) ? $xml->error->errorText : UNDEFINED_ERROR_TEXT);
        }
    }
    
    private function _setCRL(SSLCRL &$crl, $insertMode = FALSE)
    {
    	$method = "PUT";
    	$url = E3_PROV_URL_TRUSTSTORE . "/crls/".rawurlencode($crl->getId());
    	if ($insertMode) {
    		$method = "POST";
    		$url = E3_PROV_URL_TRUSTSTORE . "/crls";
    	}
    	/**
    	 * Send the XML payload the the Provisioning Backend
    	 */
    	LoggerInterface::log(($insertMode ? "Creating" : "Updating") . " CRL: {$crl->toXML()}\nEndpoint: ($method) $url", LoggerInterface::INFO);
    	$reply = $this->restClient->makeCall($url, $method, $crl->toXML());
        $xml = simplexml_load_string($reply->getPayload());

        if ($reply->getHTTPCode() === "200") {
            if ($insertMode) {
    		    if ($crl->getId() == NULL) {
    			    $crl->setId((string) $xml->id);
    		    }
    	    }
    	    return $crl;
        } else {
            throw new Exception(!empty($xml->error) ? $xml->error->errorText : UNDEFINED_ERROR_TEXT);
        }
    }
    
    private function _deleteCA($ca_id)
    {
    	$method = "DELETE";
    	$url = E3_PROV_URL_TRUSTSTORE."/certs/".rawurlencode($ca_id);
    	LoggerInterface::log("Deleting Certificate Authority '$ca_id' with DELETE: $url", LoggerInterface::INFO);
    	$reply = $this->restClient->makeCall($url, $method);
        $xml = simplexml_load_string($reply->getPayload());
        if ($reply->getHTTPCode() === "200") {
            return true;
        } else {
            throw new Exception(!empty($xml->error) ? $xml->error->errorText : UNDEFINED_ERROR_TEXT);
        }
    }
    
    private function _deleteCRL($crl_id)
    {
    	$method = "DELETE";
    	$url = E3_PROV_URL_TRUSTSTORE."/crls/".rawurlencode($crl_id);
    	LoggerInterface::log("Deleting CRL '$crl_id' with DELETE: $url", LoggerInterface::INFO);
    	$reply = $this->restClient->makeCall($url, $method);
        $xml = simplexml_load_string($reply->getPayload());
        if ($reply->getHTTPCode() === "200") {
            return true;
        } else {
            throw new Exception(!empty($xml->error) ? $xml->error->errorText : UNDEFINED_ERROR_TEXT);
        }
    }
    
    
//     private function parseCertificateExpirationDate($content) {
//     	$array = openssl_x509_parse($content, false);
//     	if($array)
//     		return $array['validTo_time_t'];
//     	else
//     		return false;
//     }
    
    private function parseCertificateExpirationDate($content)
    {
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
    
    private function parseCRLDates($content)
    {
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
