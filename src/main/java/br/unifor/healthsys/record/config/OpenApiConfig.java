package br.unifor.healthsys.record.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "HealthSys - Record Service API",
                version = "1.0.0",
                description = """
                        Serviço de prontuário eletrônico da plataforma HealthSys.

                        Responsabilidades:
                        - Criação e consulta de prontuários por paciente (`/api/records`)
                        - Registro de evoluções, prescrições e exames
                        - Registro de atendimentos e atualização de resultados de exames

                        Requer token JWT emitido pelo user-service.""",
                contact = @Contact(name = "HealthSys - UNIFOR", email = "healthsys@unifor.br"),
                license = @License(name = "Uso acadêmico/interno")
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Token JWT obtido em POST /api/auth/login (user-service). Informe apenas o token (sem o prefixo 'Bearer')."
)
public class OpenApiConfig {
}
