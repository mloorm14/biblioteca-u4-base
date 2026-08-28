package ec.edu.uteq.appweb.biblioteca.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ec.edu.uteq.appweb.biblioteca.BaseIntegracionTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Prueba de integracion HTTP de ejemplo YA IMPLEMENTADA, sobre el controlador
 * de referencia. Replique exactamente este patron para LibroController.
 *
 * Actualizada en la Unidad IV: desde que TODO-U4-2 dejo la cadena de seguridad
 * en su estado final, /api/v1/** exige autenticacion, asi que las peticiones
 * necesitan el token que devuelve /api/v1/auth/login.
 */
class AutorControllerIT extends BaseIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/v1/autores responde 200 con el envoltorio ApiResponse y su meta")
    void listarAutoresDevuelveEnvoltorio() throws Exception {
        String token = obtenerToken("lector", "Lector123!");

        mockMvc.perform(get("/api/v1/autores").param("size", "5")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.meta.page").value(0))
                .andExpect(jsonPath("$.meta.size").value(5))
                .andExpect(jsonPath("$.meta.totalElements").isNumber());
    }

    @Test
    @DisplayName("GET de un autor inexistente responde 404 en formato ProblemDetail")
    void autorInexistenteDevuelveProblemDetail() throws Exception {
        String token = obtenerToken("lector", "Lector123!");

        mockMvc.perform(get("/api/v1/autores/999999")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso no encontrado"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").exists());
    }

    private String obtenerToken(String username, String password) throws Exception {
        String credenciales = """
                {"username": "%s", "password": "%s"}
                """.formatted(username, password);

        MvcResult resultado = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales))
                .andExpect(status().isOk())
                .andReturn();

        return resultado.getResponse().getHeader(HttpHeaders.AUTHORIZATION);
    }
}
