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


require_once APPLICATION_PATH."/plugins/ACLPlugin.php";

class Bootstrap extends Zend_Application_Bootstrap_Bootstrap
{
    /**
     * Initialize all of the custom routes
     */
    protected  function _initRoutes(){

        $router = Zend_Controller_Front::getInstance()->getRouter();
        $router->addRoute('api', // Name
            new Zend_Controller_Router_Route(
                'api', // Defines the url.  Could be something like api/:id
                array(
                    'controller' => 'api',
                    'action'=>'list'
                )));

        $router->addRoute('makecall', //Name
            new Zend_Controller_Router_Route(
                'makeCall', //Defines the url
                array( //defaults
                    'controller' => 'api', // Directs to use the ApiController class
                    'action' => 'makecall' // Directs to use the ApiController::makeCallAction method
                )
            )
        );

        $router->addRoute('apiDetail', // Name
            new Zend_Controller_Router_Route(
                'api/:id',
                array(
                    'controller' => 'api',
                    'action'=>'request'
                )));

        $router->addRoute('apiDelete', // Name
            new Zend_Controller_Router_Route(
                'api/:id/delete', // Defines the url.  Could be something like api/:id
                array(
                    'controller' => 'api',
                    'action'=>'delete'
                )));

        $router->addRoute('callApi', //Name
            new Zend_Controller_Router_Route(
                'api/:id/call', //Defines the url
                array(
                    'controller' => 'api',
                    'action' => 'call'
                )
            )
        );

        /**
         * Routes for Auth provisioning
         */
        $router->addRoute('auth', // Name
            new Zend_Controller_Router_Route(
                'auth', // Defines the url.  Could be something like api/:id
                array( // defaults
                    'controller' => 'auth', // Directs to use the ApiController class
                    'action'=>'index'       // Directs to use the ApiController::indexAction method
                )));

        $router->addRoute('authEdit', // Name
            new Zend_Controller_Router_Route(
                'auth/:id', // Defines the url.  Could be something like api/:id
                array( // defaults
                    'controller' => 'auth', // Directs to use the ApiController class
                    'action'=>'request'       // Directs to use the ApiController::indexAction method
                )));

        $router->addRoute('authDelete', // Name
            new Zend_Controller_Router_Route(
                'auth/:id/delete', // Defines the url.  Could be something like api/:id
                array( // defaults
                    'controller' => 'auth', // Directs to use the ApiController class
                    'action'=>'delete'       // Directs to use the ApiController::indexAction method
                )));

        /**
         * Routes for Policy provisioning
         */
        $router->addRoute('policyForm', // Name
            new Zend_Controller_Router_Route(
                'policy/:id', // Defines the url.  Could be something like policy/:id or policy/create
                array( // defaults
                    'controller' => 'policy', // Directs to use the PolicyController class
                    'action'=>'form'       // Directs to use the PolicyController::formAction method
                )));

        $router->addRoute('policyDelete', // Name
            new Zend_Controller_Router_Route(
                'policy/:id/delete', // Defines the url.  Could be something like api/:id
                array( // defaults
                    'controller' => 'policy', // Directs to use the ApiController class
                    'action'=>'delete'       // Directs to use the ApiController::indexAction method
                )));



        /**
         * Routes for login and logout
         */
        $router->addRoute('login', // Name
            new Zend_Controller_Router_Route(
                'login', // Defines the url.  Could be something like api/:id
                array( // defaults
                    'controller' => 'authentication', // Directs to use the ApiController class
                    'action'=>'login'       // Directs to use the ApiController::indexAction method
                )));

        $router->addRoute('logout', // Name
            new Zend_Controller_Router_Route(
                'logout', // Defines the url.  Could be something like api/:id
                array( // defaults
                    'controller' => 'authentication', // Directs to use the ApiController class
                    'action'=>'logout'       // Directs to use the ApiController::indexAction method
                )));

        $router->addRoute('userList', // Name
            new Zend_Controller_Router_Route(
                'user', // Defines the url.  Could be something like api/:id
                array( // defaults
                    'controller' => 'authentication', // Directs to use the ApiController class
                    'action'=>'index'       // Directs to use the ApiController::indexAction method
                )));


        $router->addRoute('userDelete', // Name
            new Zend_Controller_Router_Route(
                'user/:id/delete', // Defines the url.  Could be something like api/:id
                array( // defaults
                    'controller' => 'authentication', // Directs to use the ApiController class
                    'action'=>'delete'       // Directs to use the ApiController::indexAction method
                )));

        $router->addRoute('userEdit', // Name
            new Zend_Controller_Router_Route(
                'user/:id', // Defines the url.  Could be something like api/:id
                array( // defaults
                    'controller' => 'authentication', // Directs to use the ApiController class
                    'action'=>'edit'       // Directs to use the ApiController::indexAction method
                )));


    }

    /**
     * Initialize our ACL
     */
    protected function _initACL(){
        Zend_Controller_Front::getInstance()->registerPlugin(new ACLPlugin());
    }
}

