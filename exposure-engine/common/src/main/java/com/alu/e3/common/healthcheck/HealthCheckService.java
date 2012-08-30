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
package com.alu.e3.common.healthcheck;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.InvalidParameterException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alu.e3.common.E3Constant;
import com.alu.e3.common.osgi.api.IHealthCheckFactory;
import com.alu.e3.data.IHealthCheckService;

/**
 * Health Check class 
 */
public class HealthCheckService implements IHealthCheckService, Runnable {

	/* final members. */
	private static final String HTTP_RESPONSE_OK = "HTTP/1.0 200 OK\r\nContent-Type: text/plain\r\nConnection: Close\r\n\r\nHealth check ok.";

	/* members. */
	private int listeningPort;
	private ServerSocket serverSocket;
	
	private AtomicBoolean alive;

	private HealthCheckService() {
		alive = new AtomicBoolean(true);
	}
	
	/**
	 * Constructor
	 */
	public HealthCheckService(String instanceType) {
		this();
		
		if (IHealthCheckFactory.GATEWAY_INTERNAL_TYPE.equals(instanceType))
			this.listeningPort = E3Constant.GATEWAY_INTERNAL_DEFAULT_HEALTH_CHECK_PORT;
		else if (IHealthCheckFactory.GATEWAY_TYPE.equals(instanceType))
			this.listeningPort = E3Constant.GATEWAY_DEFAULT_HEALTH_CHECK_PORT;
		else if (IHealthCheckFactory.SPEAKER_TYPE.equals(instanceType))
			this.listeningPort = E3Constant.SPEAKER_DEFAULT_HEALTH_CHECK_PORT;
		else if (IHealthCheckFactory.MANAGER_TYPE.equals(instanceType))
			this.listeningPort = E3Constant.MANAGER_DEFAULT_HEALTH_CHECK_PORT;
		else throw new InvalidParameterException("Invalid instance type.");
	}
	
	/**
	 * Start healthcheck service
	 */
	@Override
	public void start()
	{
		try {
			this.stop();
			serverSocket = new ServerSocket(listeningPort);	
			alive.set(true);
			new Thread(this).start();
	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Halt healthcheck service
	 */
	@Override
	public void stop()
	{
		alive.set(false);
		
		if (serverSocket == null)
			return;
		
		try {
			serverSocket.close(); // if the service is running, it may make accept() fail.
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			serverSocket = null;
		}
	}
	
	/**
	 * Server main loop.
	 */
	@Override
	public void run() {
		while (alive.get()) {
			
			/* service not started. */
			if (serverSocket == null || serverSocket.isClosed())
				return;
			
			try {
				Socket socket = serverSocket.accept();
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				
				//writeUTF() will produce a leading byte-order character which messes up some clients
				out.writeChars(HTTP_RESPONSE_OK);
				out.close();
				socket.close();

			} catch (SocketException e) {
				System.out.println("Socket closed.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * Check if the health service is alive on the specified host.
	 */
	@Override
	public boolean check(String host) {
			
		boolean bCheck = false;
		
		try {
			Socket clientSocket = new Socket(host, listeningPort);
			bCheck = clientSocket.isConnected();
			clientSocket.close();
			
		} catch (Exception e) {
		}
		
		return bCheck;
	}
}
