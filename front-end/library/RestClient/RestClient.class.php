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

require_once 'exceptions/CURLException.php';
require_once 'exceptions/RESTAppException.php';
require_once 'objects/RestResponse.class.php';
require_once "Logging/POFileLogger.php";
require_once "Logging/POLI.php";

/**
 *
 * Rest Client Implementation for E3 Project
 * @author Guillaume Quemart
 *
 */
class RestClient {
	
	protected $host = null;
	protected $port = null;
	protected $protocol = null;
	protected $basicauth = null;
	protected $peerCertificatePath = null;
	
	public function __construct($host, $protocol = null, $port = null, $basicauth = null,
	        $peerCertificatePath = null){
		$this->logger = new POFileLogger("/tmp/logstuff");
		
		if( empty($host) ){
			$err = "RestClient requires a hostname!";
			$this->logger->log($err, POLI::ERROR);
			throw new Exception($err);
		}
		$this->host = $host;
		
		if(empty($protocol)){
			$protocol = "http";
		}
		
		if(strtolower($protocol) === "http" ){
			if( $port === null ){
				$port = 80;
			}
		} else if (strtolower($protocol) === "https"){
			if($port === null){
				$port = 443;
			}
		} else {
			$err = "Protocol must be \"http\" or \"https\"!";
			$this->logger->log($err, POLI::ERROR);
			throw new Exception($err);
		}
		$this->protocol = strtolower($protocol);
		
		if( $port <= 0 || $port > 65535 ){
			$err = "Port \"$port\" is out of range!";
			$this->logger->log($err, POLI::ERROR);
			throw new Exception($err);
		}
		$this->port = $port;
		$this->peerCertificatePath = $peerCertificatePath;
		
	}
	
  /**
   *
   * Make a cURL call
   * @param string $endpoint		URL to call
   * @param string $method			HTTP Method used for the call
   * @param string/Array $payload	Body message to send with the call
   * @throws Exception
   * @return RestResponse
   */
  public function makeCall($path, $method = "GET", $payload = ""){
    try {
      if( strpos($path, "/") !== 0 ){
        $path = "/".$path;
      }
      $endpoint = $this->protocol."://".$this->host.":".$this->port.$path;
      // Initialize the curl_session with targeted URL
      $ch = curl_init($endpoint);
      // Throw exception if curl is not initialize
      if($ch == FALSE){
        throw new CURLException("CURL Not available");
      }

      // Set the content type depending the payload type
      if(is_array($payload)){
        $contentType = "multipart/form-data";
      }else{
        $contentType = "application/xml; charset=UTF-8";
      }

      // Set global headers for the call
      $headers = array(
        'Cache-Control: nocache',
        'Accept: application/xml',
        'Content-Type: '.$contentType,
        'Connection: Keep-Alive',
      );
		  
	  
	  if(!empty($this->basicauth)){
	  	$arr =  explode(":", $this->basicauth, 2);
		$username = $arr[0];
		$password = $arr[1];
		$headers[] = $this->genAuthHeaderBasic($username, $password);
	  }
	  
	  if(!empty($this->peerCertificatePath)){
	  	// This will probably work, but I won't know until it's tested.
	  	// Until it's reliably tested, skip execution with an exception.
	  	throw new Exception("Not tested!");
		curl_setopt($ch, CURLOPT_CAINFO, $this->$peerCertificatePath);
		curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, 1);
		curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0);	
	  }
	  
      
      // Set cURL options for the call
      curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
      curl_setopt($ch, CURLOPT_CUSTOMREQUEST, strtoupper($method));
      curl_setopt($ch, CURLOPT_RETURNTRANSFER, TRUE);
      curl_setopt($ch, CURLOPT_HEADER, TRUE);
      curl_setopt($ch, CURLOPT_POSTFIELDS, $payload);

      // Execute the call
      $return = curl_exec($ch);

      // Close cURL session
      curl_close($ch);

      // Check if endpoint is unavailable
      if($return == FALSE){
      	$err = "No Server Responding" ;
      	$this->logger->log($err, POLI::ERROR);
        throw new CURLException($err);
      }
      
      // Implements a new RestResponse to store useful informations
      $response = new RestResponse();
      
      // Retrieve useful informations from REST call response
      // Extract the HTTP Code
      $response->setHTTPCode($this->getHTTPCode($return));
      // Extract the specific X-E3-Application-Error-Code used for functionnal errors  
      $response->setXApplicationCode($this->getSpecificApplicationCode($return));
      // Extract the payload
      $response->setPayload($this->getBody($return));
      
      // Throw Exception if BackEnd retrieve functionnal error
      if($response->getXApplicationCode() !== NULL && $response->getXApplicationCode() != 200){
        $errorMessage = "E3_API_Err_".$response->getXApplicationCode();
        throw new RESTAppException($errorMessage, $response->getXApplicationCode());
      }
      
      // Throw Exception if BackEnd retrieve HTTP error
      if(false && !in_array($response->getHTTPCode(), array(200, 201, 202, 203, 204))){
        if($response->getHTTPCode() == 404){
          $errorMessage = '404 - Server Unreachable';
        }else{
          $errorXML = simplexml_load_string($response->getPayload());
          if($errorXML->status == "SUCCESS") {
              POLI::log("Response successful with improper return code ({$response->getHTTPCode()}): " . print_r($response->getPayload(), true), POLI::WARN);
              return $response;
          }
          POLI::log("Errored response: ({$response->getHTTPCode()}) " . print_r($response->getPayload(), true), POLI::DEBUG);
          $errorMessage = $errorXML->error->errorText;
          //$errorMessage = htmlspecialchars($response->getPayload());
        }
        throw new Exception($errorMessage, $response->getHTTPCode());
      }
      
      // Return the formatted response object 
      return $response;
    }catch(Exception $e){
      $this->logger->log('Rest Curl Call', $e->getMessage(), array(), POLI::ERROR);
      //drupal_set_message('An error as occured during the operation, please retry or contact an administrator.', 'error');
      throw new Exception($e->getMessage());
    }
  }
	
	/**
	 * Generate Basic Authorization header.
	 *
	 * @param string $username username for authentication
	 * @param string $password password for authentication
	 * @return string containing header
	 */
	protected function genAuthHeaderBasic($username, $password) {
		return "Authorization: Basic " . base64_encode("$username:$password");
	}
	
  protected function getHTTPCode($message){
    $extracts = array();
    preg_match('/HTTP\\/1.[01] ([0-9]{3}) /i', $message, $extracts);
    return $extracts[1];
  }
  
  protected function getSpecificApplicationCode($message){
    $extracts = array();
    preg_match('/X-E3-Application-Error-Code: ([0-9]{3}) /i', $message, $extracts);
    // This errored on "Undefined offset: 1"
    // I don't know what it's supposed to do, so I'll just null-check that
//    return $extracts[1];
    if( count($extracts) > 1 ){
    	return $extracts[1];
    } else {
    	return null;
    }
  }
  
  public function getBody($message){
    $pos = strpos($message, "<response>");
    return substr($message, $pos);
  }
}

?>