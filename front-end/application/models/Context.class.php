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
 * Context class definition
 *
 * Date: 4/12/12
 *
 */
class Context
{
    /**
     * @required
     * @var string
     */
    public $id;
    /**
     * @required
     * @var string
     */
    public $status;

    /**
     * @var Counter
     */
    public $quotaPerDay;
    /**
     * @var Counter
     */
    public $quotaPerWeek;
    /**
     * @var Counter
     */
    public $quotaPerMonth;
    /**
     * @var Counter
     */
    public $rateLimitPerSecond;
    /**
     * @var Counter
     */
    public $rateLimitPerMinute;

    /**
     * @return string
     */
    public function toXML(){
        ob_start();
        include('templates/Context.tpl.php');
        return ob_get_clean();
    }

    /**
     * @static
     * @param SimpleXMLElement $xml
     */
    public static function fromXML(SimpleXMLElement $xml){
        $context = new Context();
        $context->id = (string) $xml['id'];
        $context->status = (string) $xml->status;
        if($xml->quotaPerDay) $context->quotaPerDay = Counter::fromXML($xml->quotaPerDay);
        if($xml->quotaPerMonth) $context->quotaPerMonth = Counter::fromXML($xml->quotaPerMonth);
        if($xml->quotaPerWeek) $context->quotaPerWeek = Counter::fromXML($xml->quotaPerWeek);
        if($xml->rateLimitPerMinute) $context->rateLimitPerMinute = Counter::fromXML($xml->rateLimitPerMinute);
        if($xml->rateLimitPerSecond) $context->rateLimitPerSecond = Counter::fromXML($xml->rateLimitPerSecond);

        return $context;
    }

    /**
     * @param string $id
     */
    public function setId($id)
    {
        $this->id = $id;
    }

    /**
     * @return string
     */
    public function getId()
    {
        return $this->id;
    }

    /**
     * @param \Counter $quotaPerDay
     */
    public function setQuotaPerDay($quotaPerDay)
    {
        $this->quotaPerDay = $quotaPerDay;
    }

    /**
     * @return \Counter
     */
    public function getQuotaPerDay()
    {
        return $this->quotaPerDay;
    }

    /**
     * @param \Counter $quotaPerMonth
     */
    public function setQuotaPerMonth($quotaPerMonth)
    {
        $this->quotaPerMonth = $quotaPerMonth;
    }

    /**
     * @return \Counter
     */
    public function getQuotaPerMonth()
    {
        return $this->quotaPerMonth;
    }

    /**
     * @param \Counter $quotaPerWeek
     */
    public function setQuotaPerWeek($quotaPerWeek)
    {
        $this->quotaPerWeek = $quotaPerWeek;
    }

    /**
     * @return \Counter
     */
    public function getQuotaPerWeek()
    {
        return $this->quotaPerWeek;
    }

    /**
     * @param \Counter $rateLimitPerMinute
     */
    public function setRateLimitPerMinute($rateLimitPerMinute)
    {
        $this->rateLimitPerMinute = $rateLimitPerMinute;
    }

    /**
     * @return \Counter
     */
    public function getRateLimitPerMinute()
    {
        return $this->rateLimitPerMinute;
    }

    /**
     * @param \Counter $rateLimitPerSecond
     */
    public function setRateLimitPerSecond($rateLimitPerSecond)
    {
        $this->rateLimitPerSecond = $rateLimitPerSecond;
    }

    /**
     * @return \Counter
     */
    public function getRateLimitPerSecond()
    {
        return $this->rateLimitPerSecond;
    }

    /**
     * @param string $status
     */
    public function setStatus($status)
    {
        $this->status = $status;
    }

    /**
     * @return string
     */
    public function getStatus()
    {
        return $this->status;
    }
}
