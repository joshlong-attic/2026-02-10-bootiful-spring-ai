package com.example.vt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClient;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

// cora iberkleid

@SpringBootApplication
public class VtApplication {

    public static void main(String[] args) {
        SpringApplication.run(VtApplication.class, args);
    }

//    Executor executor = Executors.newVirtualThreadPerTaskExecutor();

}

@Controller
@ResponseBody
class HelloController {

    private final RestClient http;

    HelloController(RestClient.Builder http) {
        this.http = http.build();
    }

    @GetMapping("/delay")
    String delay() {
        var msg = Thread.currentThread() + ":";
        var response = this.http
                .get()
                .uri("http://localhost/delay/5")
                .retrieve()
                .body(String.class);
        msg += Thread.currentThread();
        IO.println(msg);
        return response;
    }
}