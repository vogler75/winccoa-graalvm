# WinCC OA GraalVM Node.js Runtime Integration Library

Starting with WinCC OA Version 3.20, developers can leverage Node.js and JavaScript to implement business logic within the WinCC OA environment. This functionality is enabled by integrating the Node.js runtime with native connectivity to WinCC OA features, offering a powerful and flexible platform for creating custom automation solutions.

This open-source Java library serves as a wrapper for WinCC OA's Node.js functions, providing a seamless way to write business logic in Java while maintaining direct access to WinCC OA's capabilities through Node.js.  

By utilizing the GraalVM Node.js runtime, this integration enables the creation of solutions for WinCC OA that leverage the power of Java and its extensive library ecosystem, alongside the flexibility of JavaScript.

**Please be aware that the GraalVM Node.js Runtime is not officially supported by WinCC Open Architecture.**

# Setup Instructions

1. **Download and Extract GraalVM Node.js Runtime**  
   Download the GraalVM Node.js Runtime from [this link](https://github.com/oracle/graaljs/releases/download/graal-24.0.1/graalnodejs-community-jvm-24.0.1-windows-amd64.zip) and unzip it to a directory. In this example, we extracted it to `C:\Tools\graalnodejs-community-jvm-24.0.1-windows-amd64`. Make sure to download the correct file, which is named **graalnodejs** and also has "**jvm**" in the name (Windows version). After extraction, set the `PATH` variable to point to the bin directory of the extracted files:
   ```bash
   set PATH="C:\Tools\graalnodejs-community-24.0.1-windows-amd64\bin";%PATH%
   ```

2. **Create a WinCC OA Project**  
   Create a new WinCC OA project. In this example, we will name the project `Test320`.

3. **Copy the Files from this GIT Repository**  
   Copy the `winccoa-graalvm` directory into the `javascript` directory within your project directory (C:\WinCC_OA_Proj\Test320\javascript\winccoa-graalvm).


4. **Install Node.js Dependencies**  
   Navigate to the Node.js directory and install the required dependencies using npm:
   ```bash
   cd C:\WinCC_OA_Proj\Test320\javascript\winccoa-graalvm\nodejs  
   npm install
   ```

5. **Build the Java Project**  
   Open a terminal (with the `PATH` variable set to the GraalVM Node.js Runtime) and navigate to the Java directory. Then, build the project using Gradle (make sure Gradle is installed):
   ```bash
   cd C:\WinCC_OA_Proj\Test320\javascript\winccoa-graalvm\java  
   gradlew build
   ```

6. **Start the MQTT Server**  
   You can now start the MQTT Server example by running `startMqtt.bat`. If everything is set up correctly, the manager should start and connect to WinCC OA. The server will now host a simple MQTT server interface on port 1883. You can use any MQTT client to subscribe to WinCC OA datapoints, such as `ExampleDP_Arg1.`

   **Note:** The provided MQTT server is a basic implementation and not a fully compliant MQTT server.

# Snowflake Instructions   

```
CREATE OR REPLACE SCHEMA scada;
CREATE TABLE IF NOT EXISTS scada.scada (
  system character varying(1000) NOT NULL,
  datapoint character varying(1000) NOT NULL,
  sourcetime timestamp with time zone NOT NULL,
  servertime timestamp with time zone NOT NULL,
  numericvalue numeric,
  stringvalue text,
  status character varying(30),
  CONSTRAINT scada_pk PRIMARY KEY (system, datapoint, sourcetime)
  );
```

Generate key: https://docs.snowflake.com/en/user-guide/key-pair-auth

> openssl genrsa 2048 | openssl pkcs8 -topk8 -v2 des3 -inform PEM -out snowflake.p8 -nocrypt 

> openssl rsa -in snowflake.p8 -pubout -out snowflake.pub

> ALTER USER xxxxxx SET RSA_PUBLIC_KEY='MIIBIjANBgkqh...'; -- Replace this with your public key from the snowflake.pub file (without -----BEGIN PRIVATE KEY----- and without -----END PRIVATE KEY-----)

Copy snowflake-template.json to snowflake.json and set your connection settings and your scada query.

> cd C:\WinCC_OA_Proj\Test320\javascript\winccoa-graalvm\java

> startSnow.bat