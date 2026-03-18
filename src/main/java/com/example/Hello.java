package com.example;

import com.example.model.Greeting;
import com.example.model.User;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class Hello implements Function<User, Greeting> {

    public Greeting apply(User user) {
        return new Greeting("Well hi there, " + user.getName() + "!\n");
    }
}
