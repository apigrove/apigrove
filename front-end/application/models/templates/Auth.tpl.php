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
 * XML Template for Auth obejct
 *
 * Date: 4/12/12
 *
 */

// This line is just so I can get some type hinting in my IDE
$that = empty($this)?new Auth():$this;

?>
<auth>
    <? if(!empty($this->id)): ?>
    <id><?=$that->getId()?></id>
    <? endif; ?>
    <status><?=$that->getStatus()?></status>
    <type><?=$that->getType()?></type>
    <policyContext id="<?=$that->getPolicyContext()?>"/>
    <apiContext id="<?=$that->getApiContext()?>"/>
    <? if ($that->getType() == 'basic' ) echo $that->getBasicAuth()->toXML(); ?>
    <? if ($that->getType() == 'authKey' ) echo $that->getAuthKeyAuth()->toXML(); ?>
    <? if ($that->getType() == 'ipWhiteList' ) echo $that->getIpWhiteListAuth()->toXML(); ?>
    <? if ($that->getType() == 'wsse' ) echo $that->getWSSEAuth()->toXML(); ?>
    <? if($that->getTdrData() !== null): ?>
        <?= $that->getTdrData()->toXML(); ?>
    <? if (!empty($this->properties)) { ?>
        <properties>
        <? foreach ($this->properties as $key => $value) { ?>
            <?= "<property name=\"$key\">$value</property>" ?>
        <? } ?>
        </properties>
    <? } ?>
    <? if(!empty($this->headerTransformations)): ?>
        <headerTransformations>
        <? foreach ($this->headerTransformations as $transform) { ?>
            <?= $transform->toXML(); ?>
        <? } ?>
        </headerTransformations>
    <? endif; ?>
    <? endif; ?>
</auth>

