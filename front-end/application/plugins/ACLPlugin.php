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
 * Will enforce authentication on all requests except the login form
 *
 * User: Jonathan Samples -
 * Date: 8/24/12
 *
 */
class ACLPlugin extends Zend_Controller_Plugin_Abstract
{
    /**
     * @param $request
     */
    public function preDispatch(Zend_Controller_Request_Abstract $request){
        $id = Zend_Auth::getInstance()->getIdentity();
        if(empty($id)){

            // If it is not the login action of the authentication controller then forward to the login form
            if(!($request->getControllerName() === 'authentication' ||
                $request->getControllerName() === 'favicon.ico' ||
                $request->getControllerName() === 'error' ||
                $request->getControllerName() === 'index')){

                $this->_response->setRedirect('/login');
            }
        }
    }

}
