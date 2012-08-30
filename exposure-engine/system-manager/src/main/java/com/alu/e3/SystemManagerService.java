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
package com.alu.e3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.Description;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;

import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;
import com.alu.e3.common.spring.SpringContextBootstrapper;
import com.alu.e3.installer.Installer;
import com.alu.e3.osgi.api.ITopology;
import com.alu.e3.topology.model.Topology;


@Path("/system-manager/")
public class SystemManagerService {
	
	private static final CategoryLogger logger = CategoryLoggerFactory.getLogger(SystemManagerService.class, Category.SYS);
	
	@Autowired
	private BundleContext bundleContext;

	private ITopology topology;
	
	@Autowired
	private SpringContextBootstrapper springContextBootstrapper;
	
	public SystemManagerService() {}
	
	public void setTopology(ITopology topology) {
		this.topology = topology;
	}

	/**
	 * <p>Ask the SystemManager to install E3 on the machines described in the topology configuration file. 
	 * <p>The path specified should contain the configuration files installer-config.xml and topology.xml.
	 *
	 * @param pathToConfigFilesDirectory the path to configuration files directory (URL encoded)
	 * @returnWrapped java.lang.String A message for the execution status
	 * @HTTP 200 in case of success
	 * @HTTP 201 in case of errors
	 * @RequestHeader No specific request header needed
	 * @ResponseHeader No specific response header needed
	 */
	@GET
	@Path("/install/{pathToConfigFilesDirectory}")
	@Description(value = "Ask the SystemManager to install E3 on the machines described in the topology configuration file. The path specified should contain the configuration files installer-config.xml and topology.xml.")
	public Response install(@PathParam("pathToConfigFilesDirectory") String pathToConfigFilesDirectory) {

		String configFilePath = System.getProperty("user.home") + File.separator + "installer-config.xml";
		String topologyFilePath = System.getProperty("user.home") + File.separator + "topology.xml";
		
		File srcConfigFile = new File(pathToConfigFilesDirectory + "/installer-config.xml");
		File srcTopologyFile = new File(pathToConfigFilesDirectory + "/topology.xml");
		
		File dstConfigFile = new File(configFilePath);
		File dstTopologyFile = new File(topologyFilePath);



		try {
			
			Topology tmpTopology = topology.getTopologyFromFile(srcTopologyFile.getAbsolutePath());
			
			logger.debug("Installing E3 on all topology targets ...");
			
			Installer installer = new Installer(srcConfigFile.getAbsolutePath(), tmpTopology, springContextBootstrapper);
			installer.deploy();
			
			logger.debug("Installation of all topology targets done.");
			
			logger.debug("Copying configFile and topologyFile to home user ...");
			// The deploy has succeed, we can override manager topology
			// Copying topology and configuration files
			Utilities.copyFile(srcConfigFile, dstConfigFile, true);
			Utilities.copyFile(srcTopologyFile, dstTopologyFile, true);
			logger.debug("Copy done.");
			
			logger.debug("No listening on the topology.");
			
			return Response.ok("Install successful").build();
			
		} catch (Exception e) {
			
			logger.error("An error occurs during the install",e);
			
			String result = "Path = " + pathToConfigFilesDirectory + "\n\n";
			result += e.getMessage();
			result += "\n\n";
			result += Utilities.getStackTrace(e);

			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(result).build();
		}
	}

    
	/**
	 * <p>Gets the current version.</p>
	 */
    @GET
    @Path("/version")
    public String getVersion() {
    	
        Enumeration<?> resEnum;
        try {
            resEnum = bundleContext.getBundle().getResources(JarFile.MANIFEST_NAME);
            while (resEnum.hasMoreElements()) {
                try {
                    URL url = (URL)resEnum.nextElement();
                    InputStream is = url.openStream();
                    if (is != null) {
                        Manifest manifest = new Manifest(is);
                        Attributes mainAttribs = manifest.getMainAttributes();
                        String version = mainAttribs.getValue("Bundle-Version");
                        if(version != null) {
                            return version;
                        }
                    }
                }
                catch (Exception e) {
                    // Silently ignore wrong manifests on classpath?
                }
            }
        } catch (IOException e1) {
            // Silently ignore wrong manifests on classpath?
        }
        return null; 
    }
}
