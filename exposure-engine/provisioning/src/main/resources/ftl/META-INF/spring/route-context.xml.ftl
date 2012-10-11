<?xml version="1.0" encoding="UTF-8"?>

<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:camel="http://camel.apache.org/schema/spring"
       xmlns:osgi="http://www.springframework.org/schema/osgi"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="
       http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/osgi http://www.springframework.org/schema/osgi/spring-osgi.xsd
       http://camel.apache.org/schema/spring http://camel.apache.org/schema/spring/camel-spring.xsd
       http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-2.5.xsd">
	
	<context:annotation-config />

	<!-- Imports in this camel context, OSGi references of CamelComponent factories -->
	<osgi:reference id="ipWhiteList" interface="org.apache.camel.Component" bean-name="ipWhiteList" />
	<osgi:reference id="auth" interface="org.apache.camel.Component" bean-name="auth" />
	<osgi:reference id="rateLimitProcessor" interface="org.apache.camel.Processor" bean-name="rateLimitProcessor" />
	<osgi:reference id="validator" interface="org.apache.camel.Component" bean-name="validator" />
	<bean id="preSouthboundTDRProcessor" class="com.alu.e3.gateway.common.camel.processor.PreSouthboundTDRProcessor" />
	<bean id="postSouthboundTDRProcessor" class="com.alu.e3.gateway.common.camel.processor.PostSouthboundTDRProcessor" />
	<#if exchange.properties.E3_REQUEST_PAYLOAD.allowedHttpMethodsAsString??>
		<bean id="httpMethodProcessor" class="com.alu.e3.gateway.common.camel.processor.HttpMethodProcessor" >
			<property name="allowedHttpMethods" value="${exchange.properties.E3_REQUEST_PAYLOAD.allowedHttpMethodsAsString}" />
		</bean>
	</#if>
	<bean id="tdrRequestProcessor" class="com.alu.e3.gateway.common.camel.processor.TDRRequestProcessor" />
	<bean id="tdrResponseProcessor" class="com.alu.e3.gateway.common.camel.processor.TDRResponseProcessor" />
	<bean id="tdrStaticProcessor" class="com.alu.e3.gateway.common.camel.processor.TDRStaticProcessor" />
	<bean id="propertyExtractionProcessor" class="com.alu.e3.gateway.common.camel.processor.PropertyExtractionProcessor" />
	<bean id="headerTransRequestProcessor" class="com.alu.e3.gateway.common.camel.processor.HeaderTransRequestProcessor" />
	<bean id="headerTransResponseProcessor" class="com.alu.e3.gateway.common.camel.processor.HeaderTransResponseProcessor" />
	
	<osgi:reference id="logCorrelationIDGenerator" interface="org.apache.camel.Processor" bean-name="logCorrelationIDGenerator" />
	<osgi:reference id="subscriberIDGenerator" interface="org.apache.camel.Processor" bean-name="subscriberIDGenerator" />
	<osgi:reference id="logCorrelationIDExtractor" interface="org.apache.camel.Processor" bean-name="logCorrelationIDExtractor" />
	<osgi:reference id="subscriberIDExtractor" interface="org.apache.camel.Processor" bean-name="subscriberIDExtractor" />
	<osgi:reference id="notifyUrlHeaderProcessor" interface="org.apache.camel.Processor" bean-name="notifyUrlHeaderProcessor" />
	<osgi:reference id="notifyUrlWSAProcessor" interface="org.apache.camel.Processor" bean-name="notifyUrlWSAProcessor" />
	<osgi:reference id="AuthPostProcessor" interface="org.apache.camel.Processor" bean-name="AuthPostProcessor" />
	
	<osgi:reference id="targetHostManager" interface="com.alu.e3.gateway.loadbalancer.ITargetHostManager" bean-name="targetHostManager" />
	<osgi:reference id="topologyClient" interface="com.alu.e3.common.osgi.api.ITopologyClient" bean-name="topologyClient" />
	
	<!-- The following httpLoadBalancers are now listening to sslJetty component reload behavior. -->
	<!-- Keep that in mind : httpLoadbalancer is holding itself the http4 producer ! -->
	<#list exchange.properties.E3_REQUEST_PAYLOAD.contexts as context>
	<bean id="httpLoadBalancerProcessor_${context.id}" class="com.alu.e3.gateway.loadbalancer.HttpLoadBalancerProcessor">
		<property name="topologyClient" ref="topologyClient" />
		<property name="targetHostManager" ref="targetHostManager" />
		<#if context.loadBalancing??>
			<#if context.loadBalancing.failOver??>
			<property name="failedOver">
				<value>true</value>
			</property>
				<#if context.loadBalancing.failOver.onResponseCode??>
			<property name="failedOverErrorCode">
				<value>${context.loadBalancing.failOver.onResponseCode}</value>
			</property>
				</#if>
			</#if>
		</#if>
		<property name="apiId">
			<value>${exchange.properties.E3_API_ID}</value>
		</property>
		<property name="contextId">
			<value>${context.id}</value>
		</property>
	</bean>
	</#list>
	
	<#if exchange.properties.E3_REQUEST_PAYLOAD.tdr??>
        <#if "${exchange.properties.E3_REQUEST_PAYLOAD.tdrEnabled.enabled}" == "true">
			 <osgi:reference id="tdrRule" interface="org.apache.camel.Component" bean-name="tdrRule" />
        </#if>
    </#if>		
    <osgi:reference id="jetty" interface="org.apache.camel.Component" bean-name="jetty" />
	<osgi:reference id="gatewayErrorProcessor" interface="org.apache.camel.Processor" bean-name="gatewayErrorProcessor" />
	
	<#if exchange.properties.E3_REQUEST_PAYLOAD.https.enabled>
    	<bean id="sslJettyWatcher" class="com.alu.e3.gateway.SslJettyComponentWatcher" init-method="init" destroy-method="destroy" />
    	
    	<osgi:reference id="ssljetty" interface="org.apache.camel.Component" bean-name="ssljetty">
    		<osgi:listener ref="sslJettyWatcher" />
    		<#list exchange.properties.E3_REQUEST_PAYLOAD.contexts as context>
    		<osgi:listener ref="httpLoadBalancerProcessor_${context.id}" />
			</#list>
    	</osgi:reference>

   		<#if exchange.properties.E3_REQUEST_PAYLOAD.https.tlsMode.value() == "2Way">
    		<osgi:reference id="clientCertificateValidator" interface="org.apache.camel.Processor" bean-name="clientCertificateValidator" />
   		</#if>
	</#if>
			
  <camelContext xmlns="http://camel.apache.org/schema/spring">
  	                       
	<jmxAgent id="agent" disabled="true"/>
	
	<#assign tdrEnabled=false />
	<#if exchange.properties.E3_REQUEST_PAYLOAD.tdrEnabled??>
        <#if "${exchange.properties.E3_REQUEST_PAYLOAD.tdrEnabled.enabled}" == "true">  
			<#assign tdrEnabled=true />
		</#if>
	</#if>
  
  	<onException>
		<exception>java.lang.Exception</exception>
		<handled><constant>true</constant></handled>
      	<#if tdrEnabled>
      		  <to uri="tdrRequestProcessor" />
      		  <to uri="tdrResponseProcessor" />
      		  <to uri="tdrStaticProcessor" />
 	  		  <to uri="tdrRule:EMIT" />
 	  	</#if>
		<to uri="gatewayErrorProcessor"/>
	</onException>
	
    <route id="API_${exchange.properties.E3_API_ID_ENCODED}">
    <#assign endpoint=exchange.properties.E3_REQUEST_PAYLOAD.endpoint?replace('^/*', '', 'fr') />
    <#if exchange.properties.E3_REQUEST_PAYLOAD.https.enabled>
      	<from uri="ssljetty:https://0.0.0.0:25101/${endpoint}?matchOnUriPrefix=true&amp;enableMultipartFilter=false"/>
      	
      	<#if exchange.properties.E3_REQUEST_PAYLOAD.https.tlsMode.value() == "2Way">
      	<to uri="clientCertificateValidator"/>
      	</#if>
    <#else>
    	<from uri="jetty:http://0.0.0.0:25100/${endpoint}?matchOnUriPrefix=true&amp;enableMultipartFilter=false"/>
   	</#if>
	              
	  <#if tdrEnabled>
	  		<setProperty propertyName="E3_TDR_ENABLED"><simple>true</simple></setProperty>
	  		<to uri="tdrRule:COMMON?txTDRName=${exchange.properties.E3_REQUEST_PAYLOAD.tdrOnUse!""}" />    
            <to uri="tdrRule:STATIC?propName=API&amp;staticValue=${exchange.properties.E3_API_ID}" />
          	<to uri="tdrRule:STATIC?propName=APIType&amp;staticValue=${exchange.properties.E3_REQUEST_PAYLOAD.type}&amp;tdrTypeName=${exchange.properties.E3_REQUEST_PAYLOAD.tdrOnUse!""}" />
      <#else>
      		<setProperty propertyName="E3_TDR_ENABLED"><simple>false</simple></setProperty>          
      </#if>
      
      
      <#if exchange.properties.E3_REQUEST_PAYLOAD.ipWhiteList??>
	      <#assign ipWhiteListParams = "apiId="+exchange.properties.E3_API_ID>
	      <to uri="ipWhiteList:check?${ipWhiteListParams}" />
      </#if>

      <#assign authParams = "apiId="+exchange.properties.E3_API_ID>
      <#if exchange.properties.E3_REQUEST_PAYLOAD.authentication??>
        <#if exchange.properties.E3_REQUEST_PAYLOAD.authentication.useAuthKey>
          	<#assign authParams = authParams + "&amp;authKey=true">
	        <#if exchange.properties.E3_REQUEST_PAYLOAD.authentication.authKey??>
	        	<#assign authParams = authParams + "&amp;keyName=" + exchange.properties.E3_REQUEST_PAYLOAD.authentication.authKey.keyName>
	        	<#if exchange.properties.E3_REQUEST_PAYLOAD.authentication.authKey.headerName??>
	        		<#assign authParams = authParams + "&amp;headerName=" + exchange.properties.E3_REQUEST_PAYLOAD.authentication.authKey.headerName>
	        	</#if>
	        </#if>
        </#if>
        <#if exchange.properties.E3_REQUEST_PAYLOAD.authentication.useBasicAuth>
          	<#assign authParams = authParams + "&amp;basic=true">
        </#if>
        <#if exchange.properties.E3_REQUEST_PAYLOAD.authentication.useIpWhiteListAuth>
        	<#assign authParams = authParams + "&amp;ipList=true"> 
        </#if>
        <#if exchange.properties.E3_REQUEST_PAYLOAD.authentication.useNoAuth>
        	<#assign authParams = authParams + "&amp;noAuth=true"> 
        </#if>
        <#if exchange.properties.E3_REQUEST_PAYLOAD.authentication.useOAuth>
        	<#assign authParams = authParams + "&amp;oAuth=true"> 
        </#if>
        <#if exchange.properties.E3_REQUEST_PAYLOAD.authentication.useWsse>
        	<#assign authParams = authParams + "&amp;wsse=true"> 
        </#if>
      </#if>
      <to uri="auth:check?${authParams}" />
 	  
	  <#if exchange.properties.E3_REQUEST_PAYLOAD.subscriptionStep??>
 	  	<#if "${exchange.properties.E3_REQUEST_PAYLOAD.subscriptionStep}" == "NOTIFICATION">
 	  		<to uri="subscriberIDExtractor" />
 	  	<#else>
 	  		<to uri="subscriberIDGenerator" />
 	  	</#if>
 	  </#if>
	  
 	  <to uri="rateLimitProcessor" />
 	  
	  <#if exchange.properties.E3_REQUEST_PAYLOAD.allowedHttpMethodsAsString??>
 	    <to uri="httpMethodProcessor" />
 	  </#if>
 	  
 	  <#if "${exchange.properties.E3_REQUEST_PAYLOAD.type}" == "COMPOSITE">
 	  	<#if exchange.properties.E3_REQUEST_PAYLOAD.subscriptionStep??>
 	  		<#if "${exchange.properties.E3_REQUEST_PAYLOAD.subscriptionStep}" == "NOTIFICATION">
 	  			<to uri="logCorrelationIDExtractor" />
  			<#else>
 	  			<to uri="logCorrelationIDGenerator" />
 	  		</#if>
 	  	<#else>
 	  		<to uri="logCorrelationIDGenerator" />
 	  	</#if>
 	  </#if>
 	  
 	  <#assign validParams = "apiId="+exchange.properties.E3_API_ID>
      <#if exchange.properties.E3_REQUEST_PAYLOAD.validation??>
      	<#if exchange.properties.E3_REQUEST_PAYLOAD.validation.xml??>
      		<#assign validParams = validParams + "&amp;xmlValid=true">
      	</#if>
      	<#if exchange.properties.E3_REQUEST_PAYLOAD.validation.soap??>
      		<#assign validParams = validParams + "&amp;soapValid=true">
      		<#if exchange.properties.E3_REQUEST_PAYLOAD.validation.soap.version??>
      			<#assign validParams = validParams + "&amp;soapVersion=" + exchange.properties.E3_REQUEST_PAYLOAD.validation.soap.version>
      		</#if>
      	</#if>
      	<#if exchange.properties.E3_REQUEST_PAYLOAD.validation.schema??>
      		<#assign validParams = validParams + "&amp;baseURI=${endpoint}" + "&amp;schemaValid=${exchange.properties.E3_REQUEST_PAYLOAD.validation.schema.type}">
      	</#if>
	    <to uri="validator:check?${validParams}" />
      </#if>
 	  
 	  <to uri="propertyExtractionProcessor" />
 	  
 	  <#if tdrEnabled>
 	  	<to uri="tdrRequestProcessor" />
 	  </#if>
 	  <#if exchange.properties.E3_REQUEST_PAYLOAD.headerTransEnabled>
 	  	<to uri="headerTransRequestProcessor" />
 	  </#if>
 	  
 	  <#assign idCondition = "{property.E3_AUTH_IDENTITY_APICONTEXT}"> 
 	  <choice>
 	  	 <#list exchange.properties.E3_REQUEST_PAYLOAD.contexts as context>
      	<when>
      		<simple>$${idCondition} == '${context.id}'</simple>
      		<#if tdrEnabled>
				<to uri="tdrRule:STATIC?propName=Environment&amp;staticValue=${context.id}&amp;tdrTypeName=billing" />
      		</#if>          
			<to uri="direct:internal_${context.id}_EnvRoutes" />
      	</when>
      	<#if context.defaultContext>
      		<#assign defaultContext = context> 
      	</#if>	
      	</#list>
      	<#if defaultContext??>
      	<otherwise>
      	   <#if tdrEnabled>  
      	    		<to uri="tdrRule:STATIC?propName=Environment&amp;staticValue=${defaultContext.id}&amp;tdrTypeName=billing" />
      	    </#if>          
      		<to uri="direct:internal_${defaultContext.id}_EnvRoutes" />
      	</otherwise>
      	</#if>
      </choice>
      <setProperty propertyName="E3_GOT_SB_RESPONSE">
        <simple>true</simple>
      </setProperty>
      <#if tdrEnabled>
      	<to uri="tdrResponseProcessor" />
      </#if>
      <#if exchange.properties.E3_REQUEST_PAYLOAD.headerTransEnabled >
      	<to uri="headerTransResponseProcessor" />
      </#if>
 	  <#if tdrEnabled>
 	       <to uri="tdrStaticProcessor" />
 	  	   <to uri="tdrRule:EMIT" />
 	  </#if>
      
    </route>
      
      	 <#list exchange.properties.E3_REQUEST_PAYLOAD.contexts as context>
      	 	<route id="${context.id}_EnvRoutes">
      	 		<from uri="direct:internal_${context.id}_EnvRoutes" />
      	 		
      	 		<#if context.targetHosts?size &gt; 0>
      	 		<to uri="direct:internal_Env_${context.id}"/>
      			</#if>
      			<#if exchange.properties.E3_REQUEST_PAYLOAD.subscriptionStep??>
 	  				<#if "${exchange.properties.E3_REQUEST_PAYLOAD.subscriptionStep}" == "NOTIFICATION">
      			<to uri="direct:internal_Notification" />
      				</#if>
      			</#if>
      	 	</route>
      	 </#list>
      
      	<#list exchange.properties.E3_REQUEST_PAYLOAD.contexts as context>
        		<route id="internal_Env_${context.id}">
            		<from uri="direct:internal_Env_${context.id}"/>

            		<#if tdrEnabled>
            				<to uri="preSouthboundTDRProcessor" />
            		</#if>

            		<to uri="httpLoadBalancerProcessor_${context.id}" />
            		<#if tdrEnabled>  
            				<to uri="postSouthboundTDRProcessor" />
            		</#if>
            		
				    <#if exchange.properties.E3_REQUEST_PAYLOAD.authentication??>
           				<to uri="AuthPostProcessor" />
            		</#if>
            		
        		</route>
      	</#list>
      	
      	<#if exchange.properties.E3_REQUEST_PAYLOAD.subscriptionStep??>
 	  		<#if "${exchange.properties.E3_REQUEST_PAYLOAD.subscriptionStep}" == "NOTIFICATION">
 	  			
				<route id="internal_Notification">
            		<from uri="direct:internal_Notification"/>
            		
            		<#if "${exchange.properties.E3_REQUEST_PAYLOAD.notificationFormat}" == "HEADER">
            		<!-- NotificationFormat: HEADER -->
		            <to uri="notifyUrlHeaderProcessor" />
		            
		            <#elseif "${exchange.properties.E3_REQUEST_PAYLOAD.notificationFormat}" == "WS_ADDRESSING">
		            <!-- NotificationFormat: WS_ADDRESSING -->
		            <to uri="notifyUrlWSAProcessor" />

		            </#if>
		            
		            <#if tdrEnabled>  
		            		<to uri="tdrRule:DYNAMIC?propName=notifyUrl&amp;headerName=CamelHttpUri" />
							<to uri="preSouthboundTDRProcessor" />
					</#if>
            		<to uri="http4:host"/>

					<#if tdrEnabled>            				
						<to uri="postSouthboundTDRProcessor" />
            		</#if>
        		</route>
 	  		</#if>
 	  	</#if>

  </camelContext>
</beans>
