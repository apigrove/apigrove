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
package com.alu.e3.tdr.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimerTask;

import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;

import com.alu.e3.common.E3Constant;
import com.alu.e3.tdr.service.ITdrQueueService;

public class TdrXMLWriter extends TimerTask {

	private static final Logger logger = LoggerFactory.getLogger(TdrXMLWriter.class);

	private static int INSTANCE_INDEX = 0;

	private static final Map<String, TdrStreamWriter> tdrTypeNameToTdrStreamWriter = new HashMap<String, TdrStreamWriter>();

	private ITdrQueueService tdrQueueService;

	private boolean alive = true;

	private int bulkWriteSize = 500;

	private File outputDirectory;

	private TdrStreamWriter tdrStreamWriter;

	private int instanceIndex;

	public TdrXMLWriter() {}

	public void init() {
		outputDirectory.mkdirs();
	}


	public void setTdrQueueService(ITdrQueueService tdrQueueService) {
		this.tdrQueueService = tdrQueueService;
	}

	@ManagedAttribute
	public int getBulkWriteSize() {
		return bulkWriteSize;
	}

	public void setBulkWriteSize(int bulkWriteSize) {
		this.bulkWriteSize = bulkWriteSize;
	}

	public void setOutputDirectory(String outputDirectory) {
		setOutputDirectory(new File(outputDirectory));
	}

	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	@ManagedOperation
	public void kill() {
		alive = false;
	}


	@Override
	public void run() {
		instanceIndex = INSTANCE_INDEX++;
		logger.debug("TdrXMLWriter instance:"+instanceIndex+" Starting ...");
		processTdrQueue();
	}

	private void processTdrQueue() {
		while (alive) {
			try {
				Map<String, List<Map<String, Object>>> tdrData = tdrQueueService.getOrWait();
				writeTdrData(tdrData);
			} catch (InterruptedException e) {
				// ignore this
			}
		}
		tdrStreamWriter.stop();
	}

	private void writeTdrData(Map<String, List<Map<String, Object>>> tdrData) {
		for (Entry<String, List<Map<String, Object>>> entry : tdrData.entrySet()) {
			try {
				logger.debug("Writing TDR: " + entry.getKey());
				getTdrStreamWriter(entry.getKey()).writeTdrs(entry.getValue());
			} catch (TransformerException e) {
				logger.error(e.getMessage(), e);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	private TdrStreamWriter getTdrStreamWriter(String tdrTypeName) {
		synchronized (tdrTypeNameToTdrStreamWriter) {
			TdrStreamWriter tdrStreamWriter = tdrTypeNameToTdrStreamWriter.get(tdrTypeName);
			if (tdrStreamWriter == null) {
				tdrStreamWriter = newTdrStreamWriter(tdrTypeName);
				tdrTypeNameToTdrStreamWriter.put(tdrTypeName, tdrStreamWriter);
			}
			return tdrStreamWriter;
		}
	}

	private static TdrStreamWriter newTdrStreamWriter(String subdirectoryName) {
		File dir = new File(E3Constant.TDR_BASE_PATH, subdirectoryName);
		dir.mkdirs();
		logger.debug("Creating new TDR writer: " + dir.getPath());
		return new TdrStreamWriter(dir);
	}

	/**
	 * General Setters
	 */
	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	public void setTdrStreamWriter(TdrStreamWriter tdrStreamWriter) {
		this.tdrStreamWriter = tdrStreamWriter;
	}

	public void setInstanceIndex(int instanceIndex) {
		this.instanceIndex = instanceIndex;
	}
}
