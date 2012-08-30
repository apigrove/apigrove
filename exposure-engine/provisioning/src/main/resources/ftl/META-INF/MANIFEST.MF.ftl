Manifest-Version: 1.0
Bundle-Version: 1.0
Build-Jdk: 1.6.0_25
Built-By: E3-ExposureEngine
Tool: E3-ExposureEngine
Bundle-Name: API ${exchange.properties.E3_API_ID} ${exchange.properties.E3_API_ID_ENCODED}
Bundle-ManifestVersion: 2
Created-By: E3-ExposureEngine
Bundle-SymbolicName: API-${exchange.properties.E3_API_ID}
Import-Package: org.apache.camel,org.apache.camel.component.jetty,com.
 alu.e3.rate.model,com.alu.e3.common.osgi.api,com.alu.e3.gateway.loadb
 alancer
DynamicImport-Package: *
