package com.example.aot;

import org.jspecify.annotations.Nullable;
import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor;
import org.springframework.beans.factory.aot.BeanFactoryInitializationCode;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.Serializable;
import java.util.UUID;

@SpringBootApplication
public class AotApplication {

    public static void main(String[] args) {
        SpringApplication.run(AotApplication.class, args);
    }

    @Bean
    static MyBeanFactoryPostProcessor myBeanFactoryPostProcessor() {
        return new MyBeanFactoryPostProcessor();
    }

    @Bean
    static MyBeanFactoryInitializationAotProcesor myBeanFactoryInitializationAotProcesor() {
        return new MyBeanFactoryInitializationAotProcesor();
    }
}

class MyBeanFactoryInitializationAotProcesor implements BeanFactoryInitializationAotProcessor {

    @Override
    public @Nullable BeanFactoryInitializationAotContribution processAheadOfTime(ConfigurableListableBeanFactory beanFactory) {

        return (generationContext, code ) -> {

            code.getMethods().add("hi" ,p -> p.addStatement("System.out.println(\"hi\")"));

            var runtimeHints = generationContext.getRuntimeHints();
            for (var beanDefinitionName : beanFactory.getBeanDefinitionNames()) {
                var clzz = beanFactory.getType(beanDefinitionName);
                if (Serializable.class.isAssignableFrom(clzz)) {
                    runtimeHints.serialization().registerType(TypeReference.of(clzz.getName()));
                    IO.println("serialization registerType " + clzz.getName());
                }
            }
        };
    }
}
@Component
class ShoppingCart implements Serializable {
}

class MyBeanFactoryPostProcessor implements org.springframework.beans.factory.config.BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        IO.println("postProcessBeanFactory");
        for (var beanDefinitionName : beanFactory.getBeanDefinitionNames()) {
            var bd = beanFactory.getBeanDefinition(beanDefinitionName);
            var clzz = beanFactory.getType(beanDefinitionName);
            IO.println(beanDefinitionName + " " + bd.getFactoryBeanName() + ":" + bd.getScope() + ":" + clzz.getName());

        }
    }
}

// 0. ingest (xml, component scanning, BeanRegistrar, java config,...)
// 1. BeanDefinition
// 2. beans

//@RegisterReflectionForBinding (Foo.class)
@Component
@ImportRuntimeHints(MyApplicationRunner.Hints.class)
class MyApplicationRunner implements ApplicationRunner {

    static class Hints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
            hints.reflection().registerType(Foo.class, MemberCategory.values());

        }
    }


    private final Foo foo;

    MyApplicationRunner(Foo foo) {
        this.foo = foo;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        // 1. reflection
        // 2 serializatino
        // 3 jni
        // 4 resources
        // 5 proxies

        var jsonMapper = new JsonMapper();
        IO.println(jsonMapper.writeValueAsString(this.foo));

    }
}
/*
@Controller
@ResponseBody
class FooController {

    @GetMapping("/foo")
    Foo foo() {
        return new Foo();
    }
}*/

@Component
class Bar {
    final Foo foo;

    Bar(Foo foo) {
        this.foo = foo;
    }


}

@Component
class Foo {
    final String name;

    Foo() {
        this.name = UUID.randomUUID().toString();
    }

    public String getName() {
        return name;
    }
}