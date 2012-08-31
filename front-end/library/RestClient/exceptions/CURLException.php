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
 * Specific exception throwed by cURL mechanism
 * @author Guillaume Quemart
 *
 */
class CURLException extends Exception{
  /**
   * @param string $message
   * @param int $code
   * @param Exception $cause
   */
  public function CURLException($message, $code = 0, Exception &$previous = NULL){
    $message="cURL Error : ".$message;
    parent::__construct($message, $code, $previous);
  }
}
?>