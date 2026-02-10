package com.example.demo;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Map;

@SpringBootApplication
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }

    @Bean
    ApplicationRunner runner(RequestMappingHandlerMapping mapping) {
        return _ -> {
            IO.println(mapping);
        };
    }

    @Bean
    static BPP bpp() {
        return new BPP();
    }
}

@Component
class MyDynamicHandlerRegistrar implements InitializingBean {

    private final RequestMappingHandlerMapping handlerMapping;
    private final MyHandler handler = new MyHandler();

    public MyDynamicHandlerRegistrar(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Get the builder configuration to match HandlerMapping settings
        RequestMappingInfo.BuilderConfiguration config =
                handlerMapping.getBuilderConfiguration();

        // Build a RequestMappingInfo programmatically
        RequestMappingInfo info = RequestMappingInfo
                .paths("/api/dynamic/{id}")
                .methods(RequestMethod.GET)
                .produces("application/json")
                .options(config)  // ← Important! Ensures compatibility
                .build();

        // Register the handler method
        var method = MyHandler.class.getMethod("handleRequest", String.class);
        ReflectionUtils.makeAccessible(method);
        handlerMapping.registerMapping(info, handler, method);
        IO.println("✓ Registered /api/dynamic/{id} programmatically!");
    }

    static class MyHandler {

        @ResponseBody
        Map<String, String> handleRequest(@PathVariable String id) {
            return Map.of("id", id, "message", "Programmatically registered!");
        }
    }
}

class BPP implements BeanPostProcessor {

    @Override
    public @Nullable Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        return bean;
    }
}