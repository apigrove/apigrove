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


 
class LoggingCategory {
    public $name;
    public $fullName;
    public $enabled;
    public $description;

    public static function fromXML($xml) {
        $lc = new LoggingCategory();
        $lc->name = (string) $xml->name;
        $lc->fullName = (string) $xml->fullname;
        $lc->enabled = (string) $xml->enabled == "true";
        $lc->description = (string) $xml->description;

        return $lc;
    }

    /*
     * Note: this function takes in an array of LoggingCategories and
     * outputs the whole xml doc (incluing the root "categories" tag)
     */
    public static function toXML($categories) {
        ob_start();
        include('templates/LoggingCategory.tpl.php');
        return ob_get_clean();
    }

    public function getName() {
        return $this->name;
    }
    public function setName($val) {
        $this->name = $val;
    }

    public function getFullName() {
        return $this->fullName;
    }
    public function setFullName($val) {
        $this->fullName = $val;
    }

    public function getEnabled() {
        return $this->enabled;
    }
    public function setEnabled($val) {
        $this->enabled = $val;
    }

    public function getDescription() {
        return $this->description;
    }
    public function setDescription($val) {
        $this->description = $val;
    }
}