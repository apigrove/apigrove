/**
 * Copyright © 2012 Alcatel-Lucent.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * Licensed to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.alu.e3.data.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "passwordType")
@XmlEnum
public enum WSSEAuthPassType {
	@XmlEnumValue("PasswordDigest")
	PASSWORD_DIGEST("PasswordDigest"),
	@XmlEnumValue("PlainText")
	PLAIN_TEXT("PlainText");

	private final String value;

	WSSEAuthPassType(String v) {
		value = v;
	}

	public static WSSEAuthPassType fromValue(String v) {
		for (WSSEAuthPassType c: WSSEAuthPassType.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}
}
