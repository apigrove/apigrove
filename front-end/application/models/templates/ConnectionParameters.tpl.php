<?
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
?>
<connectionParameters>
    <? if ($this->numConnections) {?>
        <numConnections><?=$this->numConnections?></numConnections>
    <? } ?>
    <? if ($this->maxConnections){ ?>
        <maxConnections><?=$this->maxConnections?></maxConnections>
    <? } ?>
    <? if ($this->connectionAcquisitionTimeout) {?>
        <connectionAcquisitionTimeout><?=$this->connectionAcquisitionTimeout?></connectionAcquisitionTimeout>
    <? } ?>
    <? if ($this->initialConnectionTimeout) {?>
        <initialConnectionTimeout><?=$this->initialConnectionTimeout?></initialConnectionTimeout>
    <? } ?>
    <? if ($this->initialConnectionTimeoutAttempts) {?>
        <initialConnectionTimeoutAttempts><?=$this->initialConnectionTimeoutAttempts?></initialConnectionTimeoutAttempts>
    <? } ?>
</connectionParameters>