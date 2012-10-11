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
 * Authentication Controller
 *
 * Date: 8/24/12
 *
 */

class AuthenticationController extends Zend_Controller_Action{

    /**
     * Handles the display of the login form in the case of a GET request and
     * tries to log the user in in the case of a POST
     */
    public function loginAction(){
        $registry = Zend_Registry::getInstance();
        $translate = $registry->get("Zend_Translate");
        if($this->getRequest()->isPost()){
            $username = $this->_getParam('username');
            $password = $this->_getParam('password');

            // Do validation
            $validationErrors = array();
            if(empty($username)){
                $validationErrors['login']['username'] = "Username required";
            }
            if(empty($password)){
                $validationErrors['login']['password'] = "Password required";
            }

            if(empty($validationErrors)){
                $dbAdapter = Zend_Db_Table::getDefaultAdapter();
                $auth = Zend_Auth::getInstance();

                // Set up the authentication adapter
                $authAdapter = new Zend_Auth_Adapter_DbTable($dbAdapter, 'users', 'username', 'password');
                $authAdapter->setIdentity($username);
                $authAdapter->setCredential($password);

                // Attempt authentication, saving the result
                $result = $auth->authenticate($authAdapter);

                if (!$result->isValid()) {
                    // Authentication failed; print the reasons why

                    $this->view->messages = array("Invalid credentials, check your username and password and try again");

                } else {
                    // Succeeded, now redirect to the homepage
                    $this->_helper->redirector->gotoUrl('/');

                    // Authentication succeeded; the identity ($username) is stored
                    // in the session
                    // $result->getIdentity() === $authentication->getIdentity()
                    // $result->getIdentity() === $username
                }
            }

            $this->view->username = $username;

            $this->view->validationErrors = $validationErrors;
        }
        // else show the login form
    }

    /**
     * logoutAction removes the identity from the session and redirets to the login page
     */
    public function logoutAction(){
        Zend_Auth::getInstance()->clearIdentity();
        $this->_helper->redirector->gotoUrl('/');
    }

    /**
     * This function just initializes the database
     */
    public function createuserAction(){
        $dbAdapter = Zend_Db_Table::getDefaultAdapter();

        // Build a simple table creation query
        $sqlCreate = 'CREATE TABLE [users] ('
            . '[id] INTEGER  NOT NULL PRIMARY KEY, '
            . '[username] VARCHAR(50) UNIQUE NOT NULL, '
            . '[password] VARCHAR(32) NULL, '
            . '[real_name] VARCHAR(150) NULL)';

        // Create the authentication credentials table
        $dbAdapter->query($sqlCreate);

        // Build a query to insert a row for which authentication may succeed
        $sqlInsert = "INSERT INTO users (username, password, real_name) "
            . "VALUES ('admin', 'adminadmin', 'API Grove Admin')";

        // Insert the data
        $dbAdapter->query($sqlInsert);
    }

    /**
     * Show a list of users
     */
    public function indexAction(){
        $dbAadapter = Zend_Db_Table::getDefaultAdapter();

        $sql = 'SELECT * FROM [users]';

        /** @var Zend_Db_Statement_Interface $statement */
        $result = $dbAadapter->query($sql)->fetchAll();

        $this->view->users = $result;
    }

    /**
     * Delete a user from the DB
     */
    public function deleteAction(){
        $registry = Zend_Registry::getInstance();
        $translate = $registry->get("Zend_Translate");
        $dbAdapter = Zend_Db_Table::getDefaultAdapter();

        $sql = 'SELECT count(*) as count FROM [users]';

        /** @var Zend_Db_Statement_Interface $statement */
        $result = $dbAdapter->query($sql)->fetchAll();
        $count = (int) $result[0]['count'];
        if($count > 1){
            $id = $this->_getParam("id");
            if(empty($id)){
                throw new Zend_Controller_Action_Exception('Resource Not Found', 404);
            }
            $sql = "SELECT count(id) as count from [users] where id={$id}";
            $result = $dbAdapter->query($sql)->fetchAll();
            $count = (int) $result[0]['count'];
            if($count <= 0){
                throw new Zend_Controller_Action_Exception('Resource Not Found', 404);
            }
            else{
                $sql = "DELETE from [users] where id={$id}";
                $error = $dbAdapter->query($sql)->errorInfo();
                if(!empty($error)){
                    $this->view->flashMessage = $error;
                }
                else{
                    $this->view->flashMessage = "User removed";
                }
            }
        }
        else{
            $this->view->flashMessage = "Cannot delete last user";
        }

        return $this->_redirect("/user");
    }

    /**
     * Create/Modify a user
     */
    public function editAction(){
        $registry = Zend_Registry::getInstance();
        $translate = $registry->get("Zend_Translate");
        $id = 1;//$this->_getParam("id");
        $dbAdapter = Zend_Db_Table::getDefaultAdapter();
        $user = array();

        if($this->getRequest()->isPost()){
            $validationErrors = array();
            $sql = "";
            $user = $this->_getParam('user');
            /** Create Time */
            if($id === "create"){
                $this->validateUser($user, $validationErrors);
                $sql = "INSERT INTO [users](username, password, real_name) ".
                    " values('{$user['username']}', '{$user['password']}','{$user['real_name']}')";

            }
            else{
                $user['id'] = $id;
                $this->validateUser($user, $validationErrors);
                $sql = "UPDATE [users] set username='{$user['username']}', ".
                    "password='{$user['password']}', real_name='{$user['real_name']}' where id={$id}";
            }

            if(!empty($validationErrors)){
                $this->view->user = $user;
                $this->view->validationErrors = $validationErrors;
            }
            else{
                $dbAdapter->query($sql)->errorInfo();

                if($id === "create"){
                    $sql = "select last_insert_rowid() as id";
                    $result = $dbAdapter->query($sql)->fetch();
                    $newId = $result['id'];
                    $this->_redirect("/user/{$newId}");
                }
                else{
                    $this->_helper->getHelper("FlashMessenger")->addMessage("User updated");
                    $this->_redirect("/user");
                }

            }
        }

        if(empty($user)){
            $sql = "select * from [users] where id='{$id}'";
            $user = $dbAdapter->query($sql)->fetch();
        }

        if($id !== "create" && empty($user)){
            throw new Zend_Controller_Action_Exception('Resource Not Found', 404);
        }

        $this->view->user = $user;
        $this->view->messages = $this->_helper->getHelper("FlashMessenger")->getMessages();

    }

    /**
     * @param array $validationErrors
     * @param array $user
     */
    public function validateUser($user, &$validationErrors){
        $registry = Zend_Registry::getInstance();
        $translate = $registry->get("Zend_Translate");
        if(empty($user['username']))
            $validationErrors['username'] = "Username is required";
        if(empty($user['password']))
            $validationErrors['password'] = "Password is required";
        if($user['passwordConfirm'] !== $user['password'])
            $validationErrors['passwordConfirm'] = "Passwords do not match";
        if(empty($user['real_name']))
            $validationErrors['real_name'] = "Real name is required";
    }
}
