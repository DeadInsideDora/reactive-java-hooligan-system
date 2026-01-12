package org.example.hooliguns;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@SecurityScheme(
        name = "basicAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "basic"
)
@OpenAPIDefinition(
        info = @Info(
                title = "HooliGuns of ITMO API",
                version = "1.0",
                description = "Reactive backend for incident tracking and board of shame."
        )
)
public class HooliGunsApplication {
    public static void main(String[] args) {
        SpringApplication.run(HooliGunsApplication.class, args);
    }
}
