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
 * Date: 3/28/12
 *
 */
class Key
{
    /**
     * @var String
     * @required
     */
    public $name;

    /**
     * @var String
     */
    public $value;

    public function toXML(){
        ob_start();
        include('templates/Key.tpl.php');
        return ob_get_clean();
    }

    /**
     * @param String $name
     */
    public function setName($name)
    {
        $this->name = $name;
    }

    /**
     * @return String
     */
    public function getName()
    {
        return $this->name;
    }

    /**
     * @param String $value
     */
    public function setValue($value)
    {
        $this->value = $value;
    }

    /**
     * @return String
     */
    public function getValue()
    {
        return $this->value;
    }
}
