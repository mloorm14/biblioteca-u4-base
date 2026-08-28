package ec.edu.uteq.appweb.biblioteca.web;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
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
 * TODO-U4-5: pruebas de integracion del catalogo.
 */
class LibroControllerIT extends BaseIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/v1/libros responde 200 con el envoltorio completo y metadatos de paginacion correctos")
    void listarLibrosDevuelveEnvoltorio() throws Exception {
        String token = obtenerToken("lector", "Lector123!");

        mockMvc.perform(get("/api/v1/libros")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(5)))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.meta.page").value(0))
                .andExpect(jsonPath("$.meta.size").value(20));
    }

    @Test
    @DisplayName("GET /api/v1/libros/999999 responde 404 con ProblemDetail (title, status, detail)")
    void buscarLibroInexistenteDevuelveProblemDetail() throws Exception {
        String token = obtenerToken("lector", "Lector123!");

        mockMvc.perform(get("/api/v1/libros/999999")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    @DisplayName("POST /api/v1/libros con titulo vacio responde 400 con el arreglo errors poblado")
    void crearLibroConTituloVacioDevuelveValidacion() throws Exception {
        String token = obtenerToken("admin", "Admin123!");
        String cuerpo = """
                {
                    "isbn": "9780134494166",
                    "titulo": "",
                    "anioPublicacion": 2020,
                    "ejemplaresTotales": 3,
                    "autorId": 1,
                    "editorialId": 1,
                    "categoriaId": 1
                }
                """;

        mockMvc.perform(post("/api/v1/libros")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", hasSize(greaterThan(0))));
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
