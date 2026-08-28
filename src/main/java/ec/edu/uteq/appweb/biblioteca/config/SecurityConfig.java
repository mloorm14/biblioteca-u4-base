package ec.edu.uteq.appweb.biblioteca.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.uteq.appweb.biblioteca.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.time.OffsetDateTime;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
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
                                           JwtAuthenticationFilter jwtAuthenticationFilter,
                                           ObjectMapper mapper) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(peticiones -> peticiones
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api/docs", "/api/docs/**",
                                "/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(manejo -> manejo
                        .authenticationEntryPoint((peticion, respuesta, ex) -> escribirProblema(respuesta, mapper,
                                HttpStatus.UNAUTHORIZED, "No autenticado",
                                "Se requiere un token JWT valido para acceder a este recurso", "no-autenticado"))
                        .accessDeniedHandler((peticion, respuesta, ex) -> escribirProblema(respuesta, mapper,
                                HttpStatus.FORBIDDEN, "Acceso denegado",
                                "No tiene permisos suficientes para ejecutar esta operacion", "acceso-denegado")))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private void escribirProblema(HttpServletResponse respuesta, ObjectMapper mapper, HttpStatus estado,
                                  String titulo, String detalle, String tipo) throws IOException {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, detalle);
        problema.setTitle(titulo);
        problema.setType(URI.create(BASE_TIPO + tipo));
        problema.setProperty("timestamp", OffsetDateTime.now().toString());
        respuesta.setStatus(estado.value());
        respuesta.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        respuesta.getWriter().write(mapper.writeValueAsString(problema));
    }
}
