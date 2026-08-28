package ec.edu.uteq.appweb.biblioteca.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * TODO-U4-2 implementado: autentica cada peticion a partir del JWT.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String CABECERA_AUTORIZACION = "Authorization";
    private static final String PREFIJO_BEARER = "Bearer ";
    private static final String COOKIE_TOKEN = "access_token";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest peticion,
                                    HttpServletResponse respuesta,
                                    FilterChain cadena) throws ServletException, IOException {
        String token = extraerToken(peticion);
        if (StringUtils.hasText(token)) {
            if (jwtService.esValido(token)) {
                String rol = jwtService.extraerRol(token);
                var autenticacion = new UsernamePasswordAuthenticationToken(
                        jwtService.extraerUsername(token),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            } else {
                SecurityContextHolder.clearContext();
            }
        }
        cadena.doFilter(peticion, respuesta);
    }

    @Nullable
    private String extraerToken(HttpServletRequest peticion) {
        String cabecera = peticion.getHeader(CABECERA_AUTORIZACION);
        if (StringUtils.hasText(cabecera) && cabecera.startsWith(PREFIJO_BEARER)) {
            return cabecera.substring(PREFIJO_BEARER.length());
        }
        Cookie[] cookies = peticion.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE_TOKEN.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
