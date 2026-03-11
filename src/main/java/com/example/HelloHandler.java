
package com.example;

import com.example.model.Greeting;
import com.example.model.User;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class HelloHandler {

    @Autowired
    private Hello helloFunction;

    @FunctionName("hello")
    public HttpResponseMessage execute(
            @HttpTrigger(
                    name = "request",
                    methods = {HttpMethod.GET, HttpMethod.POST},
                    authLevel = AuthorizationLevel.ANONYMOUS
            )
            HttpRequestMessage<Optional<User>> request,
            ExecutionContext context) {

        User user = request.getBody()
                .filter(u -> u.getName() != null)
                .orElseGet(() ->
                        new User(request.getQueryParameters()
                                .getOrDefault("name", "world")
                        )
                );

        context.getLogger().info("Greeting user name: " + user.getName());

        Greeting result = helloFunction.apply(user);

        return request
                .createResponseBuilder(HttpStatus.OK)
                .body(result)
                .header("Content-Type", "application/json")
                .build();
    }
}

