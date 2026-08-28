package ec.edu.uteq.appweb.biblioteca.integration;

import ec.edu.uteq.appweb.biblioteca.config.CacheConfig;
import ec.edu.uteq.appweb.biblioteca.exception.ServicioExternoException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * TODO-U4-4 implementado: consumo de Open Library con cache-aside y manejo
 * diferenciado de fallos.
 *
 * exchange() se usa en vez de retrieve().onStatus(...) porque un 404 de Open
 * Library no trae un cuerpo JSON valido: intentar convertirlo con .body(...)
 * lanzaria una excepcion de lectura en lugar de permitir devolver null.
 * exchange() da acceso directo al estado antes de decidir si conviene
 * convertir el cuerpo.
 */
@Component
public class OpenLibraryClient {

    private final RestClient restClient;

    public OpenLibraryClient(RestClient restClientExterno) {
        this.restClient = restClientExterno;
    }

    @Cacheable(value = CacheConfig.CACHE_OPENLIBRARY, key = "#isbn", unless = "#result == null")
    public OpenLibraryResponse consultarPorIsbn(String isbn) {
        try {
            return restClient.get()
                    .uri("/isbn/{isbn}.json", isbn)
                    .exchange((peticion, respuesta) -> {
                        int estado = respuesta.getStatusCode().value();
                        if (estado == 404) {
                            return null;
                        }
                        if (respuesta.getStatusCode().isError()) {
                            throw new ServicioExternoException(
                                    "Open Library respondio con estado " + estado + " para el ISBN " + isbn);
                        }
                        return respuesta.bodyTo(OpenLibraryResponse.class);
                    });
        } catch (ResourceAccessException ex) {
            throw new ServicioExternoException("Open Library no respondio a tiempo para el ISBN " + isbn, ex);
        }
    }
}
