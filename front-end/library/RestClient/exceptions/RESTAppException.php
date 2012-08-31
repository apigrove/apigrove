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
 * Specific exception throwed by REST server application
 * @author Guillaume Quemart
 *
 */
class RESTAppException extends Exception{
  
  // General codes
  const E3_API_Err_500 = "System Error Occured";
  const E3_API_Err_403 = "Not authorized / Authentication Failed";
  
  // Asynchronous Provisionning API codes
  const E3_API_Err_100 = "API Provisionning - POST - Response OK";
  const E3_API_Err_101 = "API Provisionning - POST - XML Body not provided";
  const E3_API_Err_102 = "API Provisionning - POST - XML Validation failed";
  const E3_API_Err_103 = "API Provisionning - POST - API ID not provided";
  const E3_API_Err_110 = "API Provisionning - GET - Response OK";
  const E3_API_Err_111 = "API Provisionning - GET - Provisionning ID not provided";
  const E3_API_Err_112 = "API Provisionning - GET - No Process found for this provisionning ID";
  const E3_API_Err_120 = "API Provisionning - DELETE - Response OK";
  const E3_API_Err_121 = "API Provisionning - DELETE - Provisionning ID not provided";
  const E3_API_Err_122 = "API Provisionning - DELETE - No Process running for this provisionning ID";
  
  // Synchronous Provisionning API codes
  const E3_API_Err_200 = "API Provisionning - POST - Response OK";
  const E3_API_Err_201 = "API Provisionning - POST - XML Body not provided";
  const E3_API_Err_202 = "API Provisionning - POST - XML Validation failed";
  const E3_API_Err_210 = "API Provisionning - PUT - Response OK";
  const E3_API_Err_211 = "API Provisionning - PUT - XML Body not provided";
  const E3_API_Err_212 = "API Provisionning - PUT - XML Validation failed";
  const E3_API_Err_213 = "API Provisionning - PUT - API ID not provided";
  const E3_API_Err_214 = "API Provisionning - PUT - Update operation failed";
  const E3_API_Err_220 = "API Provisionning - DELETE - Response OK";
  const E3_API_Err_221 = "API Provisionning - DELETE - API ID not provided";
  const E3_API_Err_222 = "API Provisionning - DELETE - Delete operation failed";
  const E3_API_Err_230 = "API Provisionning - GET ALL - Response OK";
  const E3_API_Err_231 = "API Provisionning - GET ALL - No API ID found";
  const E3_API_Err_240 = "API Provisionning - GET - Response OK";
  const E3_API_Err_241 = "API Provisionning - GET - API ID not provided";
  
  
  /**
  * @param string $message
  * @param int $code
  * @param Exception $cause
  */
  public function RESTAppException($message, $code = 0, Exception &$previous = NULL){
    $message="Functionnal Error : ".constant("RESTAppException::$message");
    parent::__construct($message, $code, $previous);
  }
}
?>