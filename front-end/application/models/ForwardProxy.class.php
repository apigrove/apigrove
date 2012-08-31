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
 * @version $Id$
 * @copyright 7/19/12 4:46 PM
 */

class ForwardProxy
{
    public $host;
    public $port;
    public $user;
    public $pass;

    public function toXML(){
        ob_start();
        include('templates/ForwardProxy.tpl.php');
        return ob_get_clean();
    }

    public static function fromXML(SimpleXMLElement $xml){
        $th = new ForwardProxy();
        $th->setHost((string)$xml->proxyHost);
        $th->setPort((string)$xml->proxyPort);
        $th->setUser((string)$xml->proxyUser);
        $th->setPass((string)$xml->proxyPass);

        return $th;
    }

    public function getHost() {
        return $this->host;
    }
    public function setHost($val) {
        $this->host = $val;
    }

    public function getPort() {
        return $this->port;
    }
    public function setPort($val) {
        $this->port = $val;
    }

    public function getUSer() {
        return $this->user;
    }
    public function setUser($val) {
        $this->user = $val;
    }

    public function getPass() {
        return $this->pass;
    }
    public function setPass($val) {
        $this->pass = $val;
    }

    public function toString() {
        return "{$this->user}:{$this->pass}@{$this->host}:{$this->port}";
    }

    public static function fromString($str) {
        if (empty($str)) return null;
        $str = explode("@", $str);
        $up = explode(":", $str[0]);
        $hp = explode(":", $str[1]);

        $prox = new ForwardProxy();
        $prox->user = $up[0];
        $prox->pass = $up[1];
        $prox->host = $hp[0];
        $prox->port = $hp[1];
        return $prox;
    }

}
