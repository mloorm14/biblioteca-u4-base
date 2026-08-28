package ec.edu.uteq.appweb.biblioteca.config;

import ec.edu.uteq.appweb.biblioteca.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * TODO-U4-2 implementado: cadena de seguridad definitiva.
 *
 * La autorizacion fina por rol se declara con @PreAuthorize en los controladores,
 * habilitada por @EnableMethodSecurity. Las denegaciones que ese mecanismo produce
 * llegan al GlobalExceptionHandler (AccessDeniedException -> 403 ProblemDetail).
 * Lo que ese manejador NO puede cubrir es la falta de autenticacion, porque el
 * rechazo ocurre en la cadena de filtros, antes del DispatcherServlet: por eso
 * aqui se define un AuthenticationEntryPoint y un AccessDeniedHandler propios
 * que responden en el mismo formato ProblemDetail (RFC 9457).
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String BASE_TIPO = "https://uteq.edu.ec/errores/";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(peticiones -> peticiones
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api/docs", "/api/docs/**",
                                "/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(manejo -> manejo
                        .authenticationEntryPoint((peticion, respuesta, ex) -> escribirProblema(respuesta,
                                HttpStatus.UNAUTHORIZED, "No autenticado",
                                "Se requiere un token JWT valido para acceder a este recurso", "no-autenticado"))
                        .accessDeniedHandler((peticion, respuesta, ex) -> escribirProblema(respuesta,
                                HttpStatus.FORBIDDEN, "Acceso denegado",
                                "No tiene permisos suficientes para ejecutar esta operacion", "acceso-denegado")))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Se escribe el ProblemDetail a mano, sin depender de un bean ObjectMapper/JsonMapper:
     * este rechazo ocurre en la cadena de filtros de Spring Security, antes de que el
     * mecanismo de serializacion de Spring MVC entre en juego, y el tipo de mapper JSON
     * que autoconfigura Spring Boot depende de que libreria Jackson este en el classpath.
     */
    private void escribirProblema(HttpServletResponse respuesta, HttpStatus estado,
                                  String titulo, String detalle, String tipo) throws IOException {
        String cuerpo = """
                {"type":"%s%s","title":"%s","status":%d,"detail":"%s","timestamp":"%s"}\
                """.formatted(BASE_TIPO, tipo, titulo, estado.value(), detalle, OffsetDateTime.now());
        respuesta.setStatus(estado.value());
        respuesta.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        respuesta.getWriter().write(cuerpo);
    }
}
