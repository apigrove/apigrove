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
package com.alu.e3.prov.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;

import com.alu.e3.common.camel.ExchangeConstantKeys;
import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;
import com.alu.e3.common.tools.BundleTools;
import com.alu.e3.prov.restapi.ExchangeData;
import com.alu.e3.prov.restapi.model.Api;
import com.alu.e3.prov.restapi.model.ResourceItem;
import com.alu.e3.prov.restapi.model.SchemaValidationEnum;
import com.alu.e3.prov.restapi.model.Validation;
import com.alu.e3.prov.restapi.model.Validation.Schema;

import freemarker.cache.URLTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class ApiJarBuilder implements IApiJarBuilder {
	private static final CategoryLogger LOG = CategoryLoggerFactory.getLogger(ApiJarBuilder.class, Category.PROV);

	private static final String MANIFEST_TEMPLATE_PATH = "ftl/META-INF/MANIFEST.MF.ftl";
	private static final String ROUTE_TEMPLATE_PATH = "ftl/META-INF/spring/route-context.xml.ftl";
	private static final String RESOURCES_FOLDER_PATH_IN_JAR = "resources";

	private Configuration fmConfiguration = null;

	protected int bufferSize = 128 * 1024;
	private Resource soap11;
	private Resource soap12;
	private Resource xml;
	
	// for Jar generation in disk 
	private boolean generateJarInFile = false;	
	private String workingDir;
	private String archiveDir;
	
	public ApiJarBuilder(Resource soap11, Resource soap12, Resource xml) {
		this();
		this.soap11 = soap11;
		this.soap12 = soap12;
		this.xml = xml;
	}

	public ApiJarBuilder() {
		fmConfiguration = new Configuration();
		// fmConfiguration.setCacheStorage(new NoCacheStorage());
		fmConfiguration.setLocalizedLookup(false);
		fmConfiguration.setTemplateLoader(new URLTemplateLoader() {
			@Override
			protected URL getURL(String name) {
				return ApiJarBuilder.class.getClassLoader().getResource(name);
			}
		});
	}

	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alu.e3.prov.restapi.IApiJarBuilder#build(com.alu.e3.prov.restapi.
	 * model.Api, java.util.Map)
	 */
	@Override
	public byte[] build(Api api, ExchangeData exchange) {
		final Map<Object, Object> variablesMap = new HashMap<Object, Object>();
		variablesMap.put("exchange", exchange);

		byte[] jarBytes = null;
		ByteArrayOutputStream baos = null;
		JarOutputStream jos = null;
		try {
			baos = new ByteArrayOutputStream();
			jos = new JarOutputStream(baos);

			List<JarEntryData> entries = new ArrayList<JarEntryData>();
			doGenXML(entries, api, variablesMap);
			doGenManifest(entries, api, variablesMap);
			doGenResources(entries, api, variablesMap);

			for (JarEntryData anEntry : entries) {
				jos.putNextEntry(anEntry.jarEntry);
				jos.write(anEntry.bytes);

			}
			// the close is necessary before getting bytes
			jos.close();
			jarBytes = baos.toByteArray();

			if (this.generateJarInFile) {
				// generate Jar in Disk for debug only
				doGenJar(jarBytes, api, variablesMap);
			}

		} catch (Exception e) {
			if(LOG.isErrorEnabled()) {
				LOG.error("Error building the jar for apiID:" + api.getId(), e);
			}
		} finally {
			if (jos != null)
				try {
					jos.close();
				} catch (IOException e) {
					LOG.error("Error closing stream", e);
				}

			if (baos != null)
				try {
					baos.close();
				} catch (IOException e) {
					LOG.error("Error closing stream", e);
				}
		}

		return jarBytes;
	}

	protected void doGenManifest(List<JarEntryData> entries, Api api, Map<Object, Object> variablesMap) throws IOException, TemplateException {
		Template template = fmConfiguration.getTemplate(MANIFEST_TEMPLATE_PATH);
		StringWriter buffer = new StringWriter();
		template.process(variablesMap, buffer);
		buffer.flush();

		if (LOG.isDebugEnabled())
			LOG.debug(buffer.getBuffer().toString());

		addJarEntry(entries, buffer.getBuffer().toString().getBytes("UTF-8"), "META-INF/MANIFEST.MF");

	}

	protected void doGenXML(List<JarEntryData> entries, Api api, Map<Object, Object> variablesMap) throws IOException, TemplateException {
		Template template = fmConfiguration.getTemplate(ROUTE_TEMPLATE_PATH);
		StringWriter buffer = new StringWriter();
		template.process(variablesMap, buffer);
		buffer.flush();

		if (LOG.isDebugEnabled())
			LOG.debug(buffer.getBuffer().toString());

		addJarEntry(entries, buffer.getBuffer().toString().getBytes("UTF-8"), "META-INF/spring/route-context.xml");
	}

	protected void doGenResources(List<JarEntryData> entries, Api api, Map<Object, Object> variablesMap) throws UnsupportedEncodingException, IOException {
		if (api.getValidation() != null) {
			Validation val = api.getValidation();

			Schema schema = val.getSchema();

			if (schema != null) {

				// loop on schema resources and store them on files
				StringBuffer buf = new StringBuffer();
				List<ResourceItem> resources = schema.getResourcesList();
				int resourcesCount = resources.size();
				for (int i = 0; i < resourcesCount; i++) {
					ResourceItem item = resources.get(i);

					if (item != null) {
						boolean isMain = item.isIsMain();
						String fileName = item.getName();
						if (isMain) {
							fileName = "main." + fileName;
						}

						// log file name
						buf.append(fileName);
						if ((i + 1) < resourcesCount) {
							buf.append(";");
						}

						addJarEntry(entries, item.getGrammar().getBytes("UTF-8"), RESOURCES_FOLDER_PATH_IN_JAR + File.separator + fileName);
					}
				}

				// if the validation is WSDL, we need to add the schemas to
				// validate the SOAP envelop
				if (schema.getType() == SchemaValidationEnum.WSDL) {
					addSOAPResources(entries, buf, RESOURCES_FOLDER_PATH_IN_JAR);
				}

				// write resources.list file
				addJarEntry(entries, buf.toString().getBytes("UTF-8"), RESOURCES_FOLDER_PATH_IN_JAR + File.separator + "resources.list");
			}

		}
	}

	protected void doGenJar(byte[] jarBytes, Api api, Map<Object, Object> variablesMap) throws Exception {
		ExchangeData data = (ExchangeData) variablesMap.get("exchange");

		String jarName = data.getProperties().get(ExchangeConstantKeys.E3_API_ID_ENCODED.toString()) + "-" + data.getProperties().get(ExchangeConstantKeys.E3_PROVISION_ID.toString()) + ".jar";

		File file = new File(workingDir + File.separator + archiveDir + File.separator + jarName);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}

		BundleTools.byteArray2File(jarBytes, file);
	}

	protected void addJarEntry(List<JarEntryData> entries, byte[] bytes, String entryName) throws IOException {

		JarEntry anEntry = new JarEntry(entryName);
		JarEntryData data = new JarEntryData();
		data.bytes = bytes;
		data.jarEntry = anEntry;

		entries.add(data);

	}

	private void addSOAPResources(List<JarEntryData> entries, StringBuffer buf, String dirPath) throws IOException {

		// load xml schema
		InputStream xmlIn = this.xml.getInputStream();
		String xmlName = this.xml.getFilename();
		addJarEntry(entries, IOUtils.toByteArray(xmlIn), dirPath + File.separator + xmlName);
		// log filename
		buf.append(";");
		buf.append(xmlName);
		buf.append(";");

		// load soap 1.1 schema
		InputStream soap11In = this.soap11.getInputStream();
		String soap11Name = this.soap11.getFilename();
		addJarEntry(entries, IOUtils.toByteArray(soap11In), dirPath + File.separator + soap11Name);

		// log filename
		buf.append(soap11Name);
		buf.append(";");

		// load soap 1.2 schema
		InputStream soap12In = this.soap12.getInputStream();
		String soap12Name = this.soap12.getFilename();
		addJarEntry(entries, IOUtils.toByteArray(soap12In), dirPath + File.separator + soap12Name);

		// log filename
		buf.append(soap12Name);

	}

	private class JarEntryData {
		JarEntry jarEntry;
		byte[] bytes;
	}

	public String getWorkingDir() {
		return workingDir;
	}

	public void setWorkingDir(String workingDir) {
		this.workingDir = workingDir;
	}

	public String getArchiveDir() {
		return archiveDir;
	}

	public void setArchiveDir(String archiveDir) {
		this.archiveDir = archiveDir;
	}

	public boolean isGenerateJarInFile() {
		return generateJarInFile;
	}

	public void setGenerateJarInFile(boolean generateJarInFile) {
		this.generateJarInFile = generateJarInFile;
	}

}
