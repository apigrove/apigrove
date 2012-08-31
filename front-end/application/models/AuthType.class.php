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
 * AuthType model class
 *
 */
class AuthType{
    public static $BASIC = "basic";
    public static $AUTHKEY = "authKey";
    public static $IPWHITELIST = "ipWhiteList";
    public static $NOAUTH = "noAuth";
    public static $WSSE = "wsse";

    public static function fromXML(SimpleXMLElement $xml){
        return self::fromString((string) $xml);
    }

    public static function fromString($str){
        $result = null;
        if($str === self::$BASIC ||
            $str === self::$AUTHKEY ||
            $str === self::$IPWHITELIST ||
            $str === self::$NOAUTH ||
            $str === self::$WSSE ){

            $result = $str;
        }

        return $result;
    }
}