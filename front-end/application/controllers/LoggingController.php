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
 * User: David
 * Date: 8/22/12
 * Time: 6:49 PM
 */
require_once APPLICATION_PATH . '/managers/LoggingManager.class.php';

define('DEFAULT_LOG_DEPTH', 50);

class LoggingController extends Zend_Controller_Action
{
    private $loggingManager;

    public function init()
    {
        /* Initialize action controller here */
        $this->loggingManager = new LoggingManager();
    }

    /**
     * Handles index request, which retrieves active log lines from all E3 instances and sources
     */
    public function indexAction()
    {
        $logDepth = DEFAULT_LOG_DEPTH;
        $request = $this->getRequest();
        //$data = $request->getParams();
        //print_r($data);
        if ($request->isPost()) {
            $logDepth = (is_array($_POST) && isset($_POST['log_depth']) ) ? $_POST['log_depth'] : DEFAULT_LOG_DEPTH;
            //print_r("Log depth: " . $log_depth);
        }
        $this->view->logDepth = $logDepth;
        $logCollection = $this->loggingManager->getAllActiveLogs($logDepth);
        if (!$logCollection) {
            //drupal_set_message("There was an error getting logs: " . $interface->error(), 'error');
            print_r("There was an error getting logs: " . $this->loggingManager->error(), 'error');
            $this->view->logCollection = null;
        } else {
            $logs = array();
            foreach ($logCollection->logs as $log) {
                $logs[$log->ipAddress][$log->source] = $log->lines;
            }
            $this->view->logCollection = $logs;
        }
    }

    /**
     * Handles /logging/config request and displays logging configuration page.
     */
    public function configAction()
    {
        $loggingCategories = $this->loggingManager->getLoggingCategories();
        $this->view->loggingCategories = $loggingCategories;

        $request = $this->getRequest();
        if ($request->isPost()) {
            //print_r($_POST);
            //$data = $request->getParams();
            //print_r($data);
            if (isset($_POST['log_level']['java'])) {
                $this->loggingManager->setJavaLogLevel($_POST['log_level']['java']);
            }
            if (isset($_POST['log_level']['smx'])) {
                $this->loggingManager->setSMXLogLevel($_POST['log_level']['smx']);
            }
            if (isset($_POST['log_level']['syslog'])) {
                $this->loggingManager->setSyslogLogLevel($_POST['log_level']['syslog']);
            }
            if (isset($_POST['enabled_logging_category'])) {
                foreach ($loggingCategories as $category) {
                    $enabled = false;
                    foreach ($request->getPost('enabled_logging_category') as $formCategory => $formEnabled) {
                        if ($formCategory == $category->getName()) {
                            $enabled = ($formEnabled === "1");
                            break;
                        }
                    }
                    if ($enabled) {
                        //print_r(" Enable category: " . $category->getName() . ", ");
                        $category->setEnabled(1);
                    } else {
                        //print_r(" Disable category: " . $category->getName() . ", ");
                        $category->setEnabled(0);
                    }
                }
                //print_r($loggingCategories);
                $this->loggingManager->setLoggingCategories($loggingCategories);
            }

        }

        // Update view fields with current E3 state
        $javaLogLevel = $this->getGlobalLogLevelWithManagerDefault('JAVA');
        $this->view->javaLogLevel = $javaLogLevel->getLogLevel();
        $smxLogLevel = $this->getGlobalLogLevelWithManagerDefault('SMX');
        $this->view->smxLogLevel = $smxLogLevel->getLogLevel();
        $syslogLogLevel = $this->getGlobalLogLevelWithManagerDefault('SYSLOG');
        $this->view->syslogLogLevel = $syslogLogLevel->getLogLevel();
    }

    /**
     * Gets the global log-level setting for the specified source, and falls
     * back to the level on the Manager instance if no global level is set.
     *
     * @param $logSource    One of 'JAVA', 'SMX' or 'SYSLOG'
     * @return LogConfig|null   The current log-level setting
     */
    private function getGlobalLogLevelWithManagerDefault($logSource)
    {
        $logLevel = null;
        switch ($logSource) {
            case 'JAVA':
                $logLevel = $this->loggingManager->getJavaLogLevel();
                if (empty($logLevel) || !$logLevel->getLogLevel()) {
                    $logLevel = $this->loggingManager->getJavaLogLevel("1");    // 1 -> manager instance
                }
                break;

            case 'SMX':
                $logLevel = $this->loggingManager->getSMXLogLevel();
                if (empty($logLevel) || !$logLevel->getLogLevel()) {
                    $logLevel = $this->loggingManager->getSMXLogLevel("1");    // 1 -> manager instance
                }
                break;

            case 'SYSLOG':
                $logLevel = $this->loggingManager->getSyslogLogLevel();
                if (empty($logLevel) || !$logLevel->getLogLevel()) {
                    $logLevel = $this->loggingManager->getSyslogLogLevel("1");    // 1 -> manager instance
                }
                break;
        }
        return $logLevel;
    }

}
