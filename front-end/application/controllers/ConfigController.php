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
 * Controller for the Preferences form
 *
 * Date: 8/30/12
 *
 */
class ConfigController extends Zend_Controller_Action
{
    public function indexAction(){
        $registry = Zend_Registry::getInstance();
        $translate = $registry->get("Zend_Translate");
        $config = new Zend_Config_Ini(APPLICATION_PATH . '/configs/manager.ini', null, true);
        $validationErrors = array();
        if($this->getRequest()->isPost()){
            $c = $this->_getParam('config');
            if(empty($c['manager_host'])){
                $validationErrors['manager_host'] = $translate->translate("Backend Host is required");
            }

            if(empty($validationErrors)){
                $config->manager_host = $c['manager_host'];
                $writer = new Zend_Config_Writer_Ini(
                    array('config'=>$config,
                          'filename' => APPLICATION_PATH . '/configs/manager.ini')
                );
                $writer->write();
                $this->view->flashMessage = $translate->translate("Preferences Updated");
            }
        }

        $this->view->config = $config;
        $this->view->validationErrors = $validationErrors;
    }

}
