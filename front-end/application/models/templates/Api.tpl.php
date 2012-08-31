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
 * Template file for the E3Api class serialization to XML
 *
 * Date: 3/28/12
 *
 */
?>
<api>
    <? if(!empty($this->id)): ?>
    <id><?=$this->id?></id>
    <? endif; ?>
    <status><?=$this->status?></status>

    <? if(!empty($this->displayName)): ?>
    <displayName><?=$this->displayName ?></displayName>
    <? endif; ?>

    <? if(!empty($this->version)): ?>
    <version><?=$this->version ?></version>
    <? endif; ?>

    <type><?=$this->type ?></type>
    
    <? if(!empty($this->subscriptionStep)): ?>
    <subscriptionStep><?=$this->subscriptionStep ?></subscriptionStep>
    <? endif; ?>
    
    <? if($this->subscriptionStep=="Notification"): ?>
    <notificationFormat><?=$this->notificationFormat ?></notificationFormat>
    <? endif; ?>
    
    <endpoint><?=$this->endpoint?></endpoint>

    <? if($this->https !== null): ?>
        <?= $this->https->toXML() ?>
    <? endif; ?>
    <?= $this->authentication->toXML() ?>
    <? if($this->tdrEnabled !== null): ?>
        <tdrEnabled>
            <enabled><?= $this->tdrEnabled?"true":"false" ?></enabled>
        </tdrEnabled>
    <? endif; ?>
    <contexts>
        <? foreach($this->contexts as $context){
            echo $context->toXML();
        } ?>
    </contexts>
    <? if($this->tdrOnUse !== null): ?>
        <tdrOnUse type="<?=$this->tdrOnUse?>" />
    <? endif; ?>
    <? if($this->tdrOnLimitReached !== null): ?>
        <tdrOnLimitReached type="<?=$this->tdrOnLimitReached?>" />
    <? endif; ?>
    <? if(!empty($this->tdrData)): ?>
        <?= $this->tdrData->toXML(); ?>
    <? endif; ?>
    <? if($this->validation !== null ): ?>
        <?= $this->validation->toXML(); ?>
    <? endif; ?>
    <? if (!empty($this->properties)) { ?>
        <properties>
        <? foreach ($this->properties as $key => $value) { ?>
            <?= "<property name=\"$key\">$value</property>" ?>
        <? } ?>
        </properties>
    <? } ?>

    <? if($this->headerTransformationEnabled !== null): ?>
        <headerTransEnabled>
            <?= $this->headerTransformationEnabled?"true":"false" ?>
        </headerTransEnabled>
    <? endif; ?>
    <? if(!empty($this->headerTransformations)): ?>
        <headerTransformations>
        <? foreach ($this->headerTransformations as $transform) { ?>
            <?= $transform->toXML(); ?>
        <? } ?>
        </headerTransformations>
    <? endif; ?>
    <? if(!empty($this->allowedHttpMethods)): ?>
        <allowedHttpMethods>
        <? foreach ($this->allowedHttpMethods as $method) { ?>
            <httpMethod><?=$method?></httpMethod>
        <? } ?>
        </allowedHttpMethods>
    <? endif; ?>

</api>
