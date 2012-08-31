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
 * Manage useful informations for REST response
 * @author Guillaume Quemart
 *
 */
class RestResponse {
  protected $HTTPCode = NULL;
  protected $XApplicationCode = NULL;
  protected $Payload;
  
  public function getHTTPCode(){
    return $this->HTTPCode;
  }
  
  public function setHTTPCode($value){
    $this->HTTPCode = $value;
  }
  
  public function getXApplicationCode(){
    return $this->XApplicationCode;
  }
  
  public function setXApplicationCode($value){
    $this->XApplicationCode = $value;
  }
  
  public function getPayload(){
    return $this->Payload;
  }
  
  public function setPayload($value){
    $this->Payload = $value;
  }
}