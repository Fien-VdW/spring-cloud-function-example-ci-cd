# Spring Cloud Function in Azure

Example project for getting started with Spring Cloud Functions.

This project comes directly from the [azure example project](https://github.com/Azure/azure-functions-java-worker/tree/dev/samples/spring-cloud-example) and [MS learn article](https://learn.microsoft.com/en-us/azure/developer/java/spring-framework/getting-started-with-spring-cloud-function-in-azure) but is modified to be more up to date and a bit easier to follow.

This article guides you through using [Spring Cloud Functions](https://spring.io/projects/spring-cloud-function) to develop a Java function and publish it to Azure Functions. When you're done, your function code runs on the [Consumption Plan](/azure/azure-functions/functions-scale#consumption-plan) in Azure and can be triggered using an HTTP request.

## Prerequisites

To develop functions using Java, you must have the following installed:

- [Java Developer Kit](https://learn.microsoft.com/en-us/java/openjdk/install?tabs=exe%2Chomebrew%2Cubuntu)
- [Apache Maven](https://maven.apache.org/install.html), version 3.0 or higher
- [Azure CLI](https://learn.microsoft.com/en-us/cli/azure/install-azure-cli?view=azure-cli-latest)
- [Azure Functions Core Tools](https://learn.microsoft.com/en-us/azure/azure-functions/functions-run-local?tabs=windows%2Cisolated-process%2Cnode-v4%2Cpython-v2%2Chttp-trigger%2Ccontainer-apps&pivots=programming-language-java) version 4

> [!IMPORTANT]
> 1. You must set the JAVA_HOME environment variable to the install location of the JDK to complete this quickstart.
> 2. Make sure your core tools version is at least 4.0.5030

## The example project

The example project represents a classical "Hello, World" function, that runs on Azure Functions, and which is configured with Spring Cloud Function.

It receives a simple `User` JSON object, which contains a user name, and send back a `Greeting` object, which contains the welcome message to that user.

## Maven project configuration

Some properties in your Maven project need to be configured to be able to deploy to Azure according to your settings.
Modify these in your pom.xml file:

```xml
    <properties>
    ...
    <!-- customize those five properties. The functionAppName should be unique across Azure -->
    <functionResourceGroup>resource-group-name-here</functionResourceGroup>
    <functionAppServicePlanName>service-plan-name-here</functionAppServicePlanName>
    <functionAppName>function-app-name-here</functionAppName>
    <functionAppRegion>westeurope</functionAppRegion>
    <functionPricingTier>Y1</functionPricingTier>
    </properties>
```

Note: Azure Functions only supports up to Java 21, so make sure this is correctly configured in your Maven project.

## The Spring Cloud Function

In the following file you can find Spring Boot component that represents the Function we want to run:

*Hello.java*:

```java
package example.hello;

import example.hello.model.*;
import org.springframework.stereotype.Component;
import java.util.function.Function;

@Component
public class Hello implements Function<User, Greeting> {

    @Override
    public Greeting apply(User user) {
        return new Greeting("Hello, " + user.getName() + "!\n");
    }
}
```

> [!NOTE]
> The `Hello` function is quite specific:
>
> - It is a `java.util.function.Function`. It contains the business logic, and it uses a standard Java API to transform one object into another.
> - Because it has the `@Component` annotation, it's a Spring Bean, and by default its name is the same as the class, but starting with a lowercase character: `hello`. Following this naming convention is important if you want to create other functions in your application. The name must match the Azure Functions name we'll create in the next section.
> - This function implements the Spring Cloud Function interface, so you can use this function with multiple Cloud function APIs like AWS and Azure.

## The Azure Function

In the following file you can find the actual Azure Function implementation. This function delegates its execution to the Spring Cloud Function as previously mentioned:

*HelloHandler.java*:

```java
package example.hello;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import example.hello.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class HelloHandler {

    @Autowired
    private Hello hello;

    @FunctionName("hello")
    public HttpResponseMessage execute(
            @HttpTrigger(name = "request", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<User>> request,
            ExecutionContext context) {
        User user = request.getBody()
                .filter(u -> u.getName() != null)
                .orElseGet(() -> new User(request.getQueryParameters().getOrDefault("name", "world")));
        context.getLogger().info("Greeting user name: " + user.getName());
        return request
                .createResponseBuilder(HttpStatus.OK)
                .body(hello.apply(user))
                .header("Content-Type", "application/json")
                .build();
    }
}
```

This Java class is an Azure Function, with the following interesting features:

- It has `@Component` annotation, it's a Spring Bean.
- The name of the function, as defined by the `@FunctionName("hello")` annotation, is `hello`.
- It's a real Azure Function, so you can use the full Azure Functions API here.


## Run the Function locally

You can test the application locally before deploying it to Azure Function:

First you need to package your application into a Jar file:

```bash
mvn package
```

Now that the application is packaged, you can run it using the `azure-functions` Maven plugin:

```bash
mvn azure-functions:run
```

The Azure Function should now be available on your localhost, using port 7071. 
You can test the application through your browser and by appending the user to the url:
```bash
http://localhost:7071/api/hello?name=Azure
```

You can also directly send it a POST request, with a `User` object in JSON format. For example, using cURL:

```bash
curl -X POST http://localhost:7071/api/hello -d "{\"name\":\"Azure\"}"
```

The Function should answer you with a `Greeting` object, still in JSON format:

```output
{
  "message": "Hello, Azure!\n"
}
```

## Debug the Function locally

The following sections describe how to debug the function.

### Debug using Intellij IDEA

Open the project in Intellij IDEA, then create a **Remote JVM Debug** run configuration to attach. For more information, see [Tutorial: Remote debug](https://www.jetbrains.com/help/idea/tutorial-remote-debug.html).

![Create a Remote JVM Debug run configuration][create-remote-jvm-debug-run-configuration]

Run the application with the following command:

```bash
mvn azure-functions:run -DenableDebug
```

When the application starts, you'll see the following output:

```output
Worker process started and initialized.
Listening for transport dt_socket at address: 5005
```

Start project debugging in Intellij IDEA. You'll see the following output:

```output
Connected to the target VM, address: 'localhost:5005', transport: 'socket'
```

Mark the breakpoints you want to debug. After sending a request, the Intellij IDEA will enter debugging mode.


## Deploy the Function to Azure Functions

To deploy the Azure Function to the cloud, remember that the `<functionAppName>`, `<functionAppRegion>`, and `<functionResourceGroup>` properties you've defined in your *pom.xml* file will be used to configure your function.

> [!NOTE]
> The Maven plugin needs to authenticate with Azure. If you have Azure CLI installed, use `az login` before continuing.
> For more authentication options, see [Authentication](https://github.com/microsoft/azure-maven-plugins/wiki/Authentication) in the [azure-maven-plugins](https://github.com/microsoft/azure-maven-plugins) repository.

Run Maven to deploy your function automatically:

```bash
mvn azure-functions:deploy
```

Now go to the [Azure portal](https://portal.azure.com) to find the `Function App` that has been created.

Select the function:

- In the function overview, note the function's URL.
- Select the **Platform features** tab to find the **Log streaming** service, then select this service to check your running function.

Now, as you did in the previous section, you can use the browser or you can use cURL to access the running function. 
The following example demonstrates the cURL command for the deployed cloud function. Be sure to replace `your-function-name` by your real function name.


```bash
curl https://your-function-name.azurewebsites.net/api/hello -d "{\"name\":\"Azure\"}"
```

Like in the previous section, the Function should answer you with a `Greeting` object, still in JSON format:

```output
{
  "message": "Hello, Azure!\n"
}
```