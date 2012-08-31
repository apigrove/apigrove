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
 * File Description Goes Here
 *
 * Date: 4/13/12
 *
 */
class Counter
{
    /**
     * @required
     * @var string
     */
    public $status;
    /**
     * @required
     * @var string
     */
    public $action;

    /**
     * @var float
     */
    public $warning;

    /**
     * @var int
     */
    public $threshold;

    /**
     * Constructor
     */
    public function __construct(){
        $this->status = Status::$ACTIVE;
        $this->action = Action::$REJECT;
    }

    public function toXML(){
        ob_start();
        include('templates/Counter.tpl.php');
        return ob_get_clean();
    }

    public static function fromXML(SimpleXMLElement $xml){
        $counter = new Counter();
        $counter->status = Status::fromXML($xml->status);
        $counter->action = Action::fromXML($xml->action);
        $counter->warning = floatval($xml->warning);
        $counter->threshold = intval($xml->threshold);
        return $counter;
    }

    /**
     * @param string $action
     */
    public function setAction($action)
    {
        $this->action = $action;
    }

    /**
     * @return string
     */
    public function getAction()
    {
        return $this->action;
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

    /**
     * @param int $threshold
     */
    public function setThreshold($threshold)
    {
        $this->threshold = $threshold;
    }

    /**
     * @return int
     */
    public function getThreshold()
    {
        return $this->threshold;
    }

    /**
     * @param float $warning
     */
    public function setWarning($warning)
    {
        $this->warning = $warning;
    }

    /**
     * @return float
     */
    public function getWarning()
    {
        return $this->warning;
    }
}
