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
 * Created by JetBrains PhpStorm.
 *
 * @author Trevor Pesout <>
 * @version $Id$
 * @copyright 10/12/11 2:51 PM
 */


class PONotificationLogger extends POLogger {
    protected $curl;
    protected $format;
    protected $serverIdentifier;

    function __construct($serverIdentifier, $notificationServer = 'http://notification.productops.com/notification', $logLevel = POLI::INFO, $format = "%i -- %m\n") {
        $this->curl = new POCurl($notificationServer);
        $this->serverIdentifier = $serverIdentifier;
        $this->logLevel = $logLevel;
        $this->logFormat = $format;
    }

    function log($message, $priority = POLI::INFO) {
        $this->curl->post('', array(
            'message' => $this->format($message, $priority),
            'identifier' => $this->serverIdentifier,
            'severity' => $priority,
        ));
    }

}