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
package com.alu.e3.installer.model;

/**
 * Class Configuration
 */
public class Configuration {
	
	private String name;
	private String packageUrl;
	private String installerCmd, sanityCheckCmd, generateNatureCmd;
	private String type;
	private String version;
	private final String defaultRemotePath="/tmp";
	
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getPackageUrl() {
		return packageUrl;
	}
	
	public void setPackageUrl(String packageUrl) {
		this.packageUrl = packageUrl;
	}
	
	public String getInstallerCmd() {
		return installerCmd;
	}
	
	public void setInstallerCmd(String installerCmd) {
		this.installerCmd = installerCmd;
	}
	
	public String getSanityCheckCmd() {
		return sanityCheckCmd;
	}
	
	public void setSanityCheckCmd(String sanityCheckCmd) {
		this.sanityCheckCmd = sanityCheckCmd;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getGenerateNatureCmd() {
		return generateNatureCmd;
	}
	
	public void setGenerateNatureCmd(String generateNatureCmd) {
		this.generateNatureCmd = generateNatureCmd;
	}
	
	/**
	 * Get the remote path, built from the module name and version.
	 * @return
	 */
	public String getRemotePath() {
		if (name == null || name.isEmpty())
			return defaultRemotePath;
		return "/su/" + name + "-" + version;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	@Override
	public String toString() {
		return "configuration [" + name + " " + version + " " + type + " " + packageUrl + " " + getRemotePath() + " " + installerCmd + " " + sanityCheckCmd + "]";
	}
	
	/*
	try {
		URL urlPackage = new URL(packageUrl);
		String strFile = new File(urlPackage.getFile()).getName();
		
		remotePath = strFile.substring(0, strFile.lastIndexOf('_'));
		version = strFile.substring(strFile.lastIndexOf('_')+1, strFile.indexOf(".tar.gz"));
	
	} catch (MalformedURLException e) {
		
		remotePath = "/su";
		version = "undefined";
	}
	*/
}
