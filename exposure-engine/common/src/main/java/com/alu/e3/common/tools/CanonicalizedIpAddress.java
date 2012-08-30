/*
 * This file contains modified source code from the Google Guava library.
 * Modifications are copyright (C) 2012 Alcatel-Lucent.
 * The original source code copyright notice is pasted below.
 * The original source code can be found at : http://code.google.com/p/guava-libraries/source/browse/guava/src/com/google/common/net/InetAddresses.java
 */

/*
 * Copyright (C) 2008 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alu.e3.common.tools;

import java.util.Arrays;

public class CanonicalizedIpAddress {
	
	private static int IPV4_LEN = 4;
	private static int IPV6_HEXTET_LEN = 8;

	private String ip;
	
	public CanonicalizedIpAddress(String ip) {
		this.ip = canonicalize(ip); 
	}

	public String getIp() {
		return this.ip;
	}
	
	@Override
	public int hashCode() {
		return ip.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		if(this.ip == null){
			return ((CanonicalizedIpAddress) obj).getIp() == null;
		}
		return this.ip.equals(((CanonicalizedIpAddress) obj).getIp());
	}
	
	/**
	 * Returns the canonical representation of an IP address String.
	 * 
	 * <p>
	 * For IPv4 addresses, this is a passthrough, but for IPv6 addresses, the
	 * output follows <a href="http://tools.ietf.org/html/rfc5952">RFC 5952</a>
	 * section 4. The main difference is that this method uses "::" for zero
	 * compression, while Java's version uses the uncompressed form.
	 * 
	 * <p>
	 * This method uses hexadecimal for all IPv6 addresses, including
	 * IPv4-mapped IPv6 addresses such as "::c000:201". The output does not
	 * include a Scope ID.
	 * 
	 * @param ip
	 *            sting to be converted to a canonical string
	 * @return {@code String} containing the text-formatted IP address
	 */
	public static String canonicalize(String ip) throws NumberFormatException {
		if(ip == null)
			throw new NumberFormatException("null ip");
		if (ip.contains(":")) {
			// ipv6 - run it through the int[] grinder
			int[] hextets = ipv6ToHextets(ip);
			compressLongestRunOfZeroes(hextets);
			return hextetsToIPv6String(hextets);
		} else {
			StringBuilder buf = new StringBuilder(15);
			String[] octets = ip.split("\\.");
			if(octets.length != IPV4_LEN)
				throw new NumberFormatException("bad ipv4 format");
			for(int i=0; i < IPV4_LEN ; i++){
				int octet = Integer.parseInt(octets[i]);
				if(octet < 0 || octet > 255)
					throw new NumberFormatException("bad ipv4 octet");
				if(i != 0)
					buf.append(".");
				buf.append(Integer.toString(octet));
			}
			return buf.toString();
		}
	}
	
	public static boolean isValidIp(String ip) {
		try {
			CanonicalizedIpAddress.canonicalize(ip);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	private static int[] ipv6ToHextets(String ip) throws NumberFormatException {
		if (ip.matches("^[:0-9a-fA-F]{2,30}[0-9\\.]{7,15}$")) {
			// ipv4 within ipv6
			// change "::ffff:10.0.0.1" to "::ffff:0a00:0001"
			ip = ip.substring(0, ip.lastIndexOf(":") + 1)
					+ ipv4toipv6Segment(ip.substring(ip.lastIndexOf(":") + 1, ip.length()));
		} else if (ip.contains(".")) {
			throw new NumberFormatException("Bad ipv6 address: " + ip);
		}

		int[] ip_byte_arr = new int[IPV6_HEXTET_LEN];
		if (ip.startsWith("::")) {
			int[] end = ipv6SegmentToHextets(ip.substring(2));

			Arrays.fill(ip_byte_arr, 0, IPV6_HEXTET_LEN - end.length, 0);
			System.arraycopy(end, 0, ip_byte_arr, IPV6_HEXTET_LEN - end.length, end.length);

		} else if (ip.startsWith(":")) {
			throw new NumberFormatException("bad ipv6: " + ip);
		} else if (ip.endsWith("::")) {
			int[] start = ipv6SegmentToHextets(ip.substring(0, ip.length() - 2));

			System.arraycopy(start, 0, ip_byte_arr, 0, start.length);
			Arrays.fill(ip_byte_arr, start.length, IPV6_HEXTET_LEN, 0);
		} else if (ip.endsWith(":")) {
			throw new NumberFormatException("bad ipv6: " + ip);
		} else if (ip.contains("::")) {
			String[] split = ip.split("::", 2);
			int[] start = ipv6SegmentToHextets(split[0]);
			int[] end = ipv6SegmentToHextets(split[1]);
			
			if(start.length + end.length == IPV6_HEXTET_LEN)
				throw new NumberFormatException("ipv6 contains '::' without compression");

			System.arraycopy(start, 0, ip_byte_arr, 0, start.length);
			Arrays.fill(ip_byte_arr, start.length, IPV6_HEXTET_LEN - end.length, 0);
			System.arraycopy(end, 0, ip_byte_arr, IPV6_HEXTET_LEN - end.length, end.length);
		} else {
			int[] full = ipv6SegmentToHextets(ip);
			if (full.length != IPV6_HEXTET_LEN)
				throw new NumberFormatException("ipv6 address is a bad length");
			System.arraycopy(full, 0, ip_byte_arr, 0, full.length);
		}
		return ip_byte_arr;
	}
	
	/**
	 * convert "192.168.0.1" to "c0a8:0001"
	 * 
	 * @param in
	 * @return
	 * @throws NumberFormatException
	 */
	private static String ipv4toipv6Segment(String in) throws NumberFormatException {
		String[] bytes_str = in.split("\\.");
		int[] bytes_int = new int[IPV4_LEN];
		if (bytes_str.length != IPV4_LEN)
			throw new NumberFormatException("bad ipv4 address: " + in);
		for (int i = 0; i < IPV4_LEN; i++) {
			bytes_int[i] = Integer.parseInt(bytes_str[i]);
			if (bytes_int[i] > 0xff || bytes_int[i] < 0)
				throw new NumberFormatException("bad ipv4 address: " + in);
		}

		return String.format("%02x%02x:%02x%02x", bytes_int[0], bytes_int[1],
				bytes_int[2], bytes_int[3]);
	}

	private static int[] ipv6SegmentToHextets(String ip_segment) {
		if ("".equals(ip_segment))
			return new int[0];
		if (ip_segment == null || ip_segment.startsWith(":")
				|| ip_segment.endsWith(":") || ip_segment.contains("::")) {
			throw new NumberFormatException("IPv6 address contains a bad section");
		}

		String[] hextet_str_arr = ip_segment.split(":");
		int[] ret = new int[hextet_str_arr.length];

		for (int i = 0; i < hextet_str_arr.length; i++) {
			ret[i] = Integer.parseInt(hextet_str_arr[i], 16);
			if (ret[i] < 0 || ret[i] > 0xffff)
				throw new NumberFormatException("bad ipv6 segment");
		}

		return ret;
	}

	/**
	 * Identify and mark the longest run of zeroes in an IPv6 address.
	 * 
	 * <p>
	 * Only runs of two or more hextets are considered. In case of a tie, the
	 * leftmost run wins. If a qualifying run is found, its hextets are replaced
	 * by the sentinel value -1.
	 * 
	 * @param hextets
	 *            {@code int[]} mutable array of eight 16-bit hextets.
	 */
	private static void compressLongestRunOfZeroes(int[] hextets) {
		int bestRunStart = -1;
		int bestRunLength = -1;
		int runStart = -1;
		for (int i = 0; i < hextets.length + 1; i++) {
			if (i < hextets.length && hextets[i] == 0) {
				if (runStart < 0) {
					runStart = i;
				}
			} else if (runStart >= 0) {
				int runLength = i - runStart;
				if (runLength > bestRunLength) {
					bestRunStart = runStart;
					bestRunLength = runLength;
				}
				runStart = -1;
			}
		}
		if (bestRunLength >= 2) {
			Arrays.fill(hextets, bestRunStart, bestRunStart + bestRunLength, -1);
		}
	}

	/**
	 * Convert a list of hextets into a human-readable IPv6 address.
	 * 
	 * <p>
	 * In order for "::" compression to work, the input should contain negative
	 * sentinel values in place of the elided zeroes.
	 * 
	 * @param hextets
	 *            {@code int[]} array of eight 16-bit hextets, or -1s.
	 */
	private static String hextetsToIPv6String(int[] hextets) {
		/*
		 * While scanning the array, handle these state transitions: 
		 * start->num => "num"     start->gap => "::" 
		 * num->num   => ":num"    num->gap   => "::"
		 * gap->num   => "num"     gap->gap   => ""
		 */
		StringBuilder buf = new StringBuilder(39);
		boolean lastWasNumber = false;
		for (int i = 0; i < hextets.length; i++) {
			boolean thisIsNumber = hextets[i] >= 0;
			if (thisIsNumber) {
				if (lastWasNumber) {
					buf.append(':');
				}
				buf.append(Integer.toHexString(hextets[i]));
			} else {
				if (i == 0 || lastWasNumber) {
					buf.append("::");
				}
			}
			lastWasNumber = thisIsNumber;
		}
		return buf.toString();
	}

	

	/**
	 * this is an incomplete state-machine-ish implementation of ipv6ToByteArr that should
	 * be significantly faster than the original implementation. It's sometimes
	 * hard to find the optimal java coding pattern, so this might not actually
	 * be faster.
	 * 
	 * This implementation exclusively uses String.substring, String.startsWith,
	 * and String.indexOf are fast (they should be). No splits, no contains, and
	 * no lastIndexOf.
	 * 
	 * @param ip
	 * @return
	
	private int[] stateMachineImplA(String ip) {
		int[] start = new int[8];
		int startlen = 0;
		if (ip.startsWith("::")) {
			// only chop off one colon here
			ip = ip.substring(1);
			if (ip.startsWith("::")) {
				throw new NumberFormatException(
						"ipv6 address cannot start with \":::\"");
			}
		} else if (ip.startsWith(":")) {
			throw new NumberFormatException(
					"ipv6 address cannot start with a single \":\"");
		}

		while (startlen < 6) {
			// Stop after 6 hextets because that's when we need to check for ipv4
			if (ip.startsWith(":")) {
				// recall that the first ":" was chopped off. This block is
				// logic for "::"
				return stateMachineImplB(ip.substring(1), start, startlen);
			}
			int nextcolon = ip.indexOf(":");
			
			int stuff = Integer.parseInt(ip.substring(0, nextcolon), 16);
			if (stuff < 0 || stuff > 0xffff)
				throw new NumberFormatException("ipv6 address is bad");
			start[startlen] = stuff;
			startlen++;
			
			// "nextcolon+1" will chop off the next colon.
			ip = ip.substring(nextcolon + 1);
		}

		return start;
	}

	private int[] stateMachineImplB_(String ip, int[] start, int startlen) {
		
		// This method is only called after a "::" is found.
		// Fail if "::" is ever found again.
		
		return null;
	}
	
	 */
}