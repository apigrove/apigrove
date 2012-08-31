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
 * Template file for ApiContext serialization to XML
 *
 * Date: 3/28/12
 *
 */
?>
<context id="<?=$this->id?>" default="<?=$this->default?"true":"false"?>">
    <status><?=$this->status ?></status>
    <? if($this->loadBalancing->type != "none"): ?>
        <?=$this->getLoadBalancing()->toXML();?>
    <? endif; ?>
    <? if(!empty($this->targetHosts)): ?>
    <targetHosts>
        <? foreach($this->targetHosts as $targetHost): ?>
            <?= $targetHost->toXML();?>
        <? endforeach; ?>
    </targetHosts>
    <? endif; ?>
    <maxRateLimitTPSThreshold><?= $this->maxRateLimitTPSThreshold ?></maxRateLimitTPSThreshold>
    <maxRateLimitTPSWarning><?= $this->maxRateLimitTPSWarning ?></maxRateLimitTPSWarning>
    <maxRateLimitTPMThreshold><?= $this->maxRateLimitTPMThreshold ?></maxRateLimitTPMThreshold>
    <maxRateLimitTPMWarning><?= $this->maxRateLimitTPMWarning ?></maxRateLimitTPMWarning>
</context>