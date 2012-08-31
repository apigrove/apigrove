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
 * Policy template file
 *
 * User: Jonathan Samples -
 * Date: 4/12/12
 *
 */

// This line is just so I can get some type hinting in my IDE
$that = empty($this)?new Policy():$this;
?>
<policy>
    <? if(!empty($that->id)): ?>
    <id><?=$that->id?></id>
    <? endif; ?>
    <? if(!empty($that->apiIds)): ?>
    <apiIds>
        <? foreach($that->apiIds as $apiId): ?>
        <apiId><?=$apiId?></apiId>
        <? endforeach; ?>
    </apiIds>
    <? endif; ?>
    <authIds>

        <? /** @var AuthIdsType $authId */
        foreach($that->authIds as $authId): ?>
        <?= $authId->toXML() ?>
        <? endforeach; ?>
    </authIds>
    <contexts>
        <? /** @var Context $context */
        foreach($that->contexts as $context): ?>
        <?= $context->toXML() ?>
        <? endforeach; ?>
    </contexts>
    <?if(!empty($that->tdrOnLimitReached)): ?>
    <tdrOnLimitReached type="<?=$that->tdrOnLimitReached ?>" />
    <? endif; ?>
    <?if(!empty($that->tdr)): ?>
    <?=$that->tdr->toXML(); ?>
    <? endif; ?>

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
</policy>