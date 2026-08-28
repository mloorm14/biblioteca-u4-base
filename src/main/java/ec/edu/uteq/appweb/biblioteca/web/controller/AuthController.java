package ec.edu.uteq.appweb.biblioteca.web.controller;

import ec.edu.uteq.appweb.biblioteca.domain.Usuario;
import ec.edu.uteq.appweb.biblioteca.repository.UsuarioRepository;
import ec.edu.uteq.appweb.biblioteca.security.JwtService;
import ec.edu.uteq.appweb.biblioteca.web.dto.ApiResponse;
import ec.edu.uteq.appweb.biblioteca.web.dto.LoginRequest;
import ec.edu.uteq.appweb.biblioteca.web.dto.LoginResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO-U4-2 implementado: autenticacion.
 *
 * Un login fallido responde 401 con ProblemDetail directamente desde aqui: no
 * existe una excepcion propia de credenciales invalidas en GlobalExceptionHandler
 * (que es codigo cerrado de la Unidad III) y no corresponde inventarle una.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final URI TIPO_CREDENCIALES_INVALIDAS =
            URI.create("https://uteq.edu.ec/errores/credenciales-invalidas");

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest solicitud) {
        Optional<Usuario> usuario = usuarios.findByUsernameAndActivoTrue(solicitud.username());
        if (usuario.isEmpty() || !passwordEncoder.matches(solicitud.password(), usuario.get().getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(credencialesInvalidas());
        }

        Usuario encontrado = usuario.get();
        String token = jwtService.generar(encontrado);
        LoginResponse cuerpo = new LoginResponse(encontrado.getUsername(), encontrado.getRol().name(), "Bearer",
                jwtService.expiracionEnSegundos());
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(ApiResponse.ok(cuerpo, "Autenticacion exitosa"));
    }

    private ProblemDetail credencialesInvalidas() {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
                "Usuario o contrasena invalidos");
        problema.setTitle("Credenciales invalidas");
        problema.setType(TIPO_CREDENCIALES_INVALIDAS);
        return problema;
    }
}
