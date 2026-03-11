package com.example;

import com.example.model.Greeting;
import com.example.model.User;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

public class HelloTest {

    @Test
    public void test() {
        Greeting result = new Hello().apply(new User("foo"));
        assertThat(result.getMessage()).isEqualTo("Hello, foo!\n");
    }

//    @Test
//    public void start() {
//        FunctionInvoker<User, Greeting> handler = new FunctionInvoker<>(
//                Hello.class);
//        Greeting result = handler.handleRequest(new User("foo"), new ExecutionContext() {
//            @Override
//            public Logger getLogger() {
//                return Logger.getLogger(HelloTest.class.getName());
//            }
//
//            @Override
//            public String getInvocationId() {
//                return "id1";
//            }
//
//            @Override
//            public String getFunctionName() {
//                return "hello";
//            }
//        });
//        handler.close();
//        assertThat(result.getMessage()).isEqualTo("Hello, foo!\n");
//    }
}
