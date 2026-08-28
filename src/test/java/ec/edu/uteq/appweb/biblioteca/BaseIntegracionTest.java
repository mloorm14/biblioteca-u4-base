package ec.edu.uteq.appweb.biblioteca;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Base de las pruebas de integracion HTTP. YA IMPLEMENTADA: heredela y escriba
 * solo sus casos de prueba.
 *
 * Levanta un PostgreSQL 18 real en un contenedor efimero y deja que Flyway
 * aplique V1, V2 y V3 sobre el, de modo que cada ejecucion parte del mismo
 * estado conocido. @ServiceConnection sustituye a la vieja @DynamicPropertySource:
 * Spring Boot descubre solo la URL, el usuario y la clave del contenedor.
 *
 * Nota de version: en Testcontainers 2.x la clase vive en
 * org.testcontainers.postgresql.PostgreSQLContainer. La ruta antigua
 * org.testcontainers.containers.PostgreSQLContainer quedo obsoleta.
 *
 * El contenedor se arranca a mano en un inicializador estatico, sin @Testcontainers
 * ni @Container: esas anotaciones detienen el contenedor en el afterAll de CADA
 * clase de prueba, pero el ApplicationContext de Spring queda cacheado entre clases
 * (misma configuracion) con el DataSource apuntando al puerto original. La siguiente
 * clase reinicia el contenedor en un puerto nuevo y el contexto cacheado queda
 * huerfano, apuntando a un puerto muerto. Sin esas anotaciones, el contenedor se
 * inicia una sola vez por JVM y Testcontainers lo limpia via Ryuk al terminar.
 *
 * Requisito: Docker debe estar corriendo en la maquina.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("integracion")
public abstract class BaseIntegracionTest {

    @ServiceConnection
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:18-alpine")
                    .withDatabaseName("biblioteca_test")
                    .withUsername("test")
                    .withPassword("test");

    static {
        POSTGRES.start();
    }
}
