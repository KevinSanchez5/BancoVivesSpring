package vives.bancovives.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración de la aplicación.
 */
@Configuration
public class DivisasConfig {

    /**
     * Define un bean para RestTemplate.
     *
     * @return una nueva instancia de RestTemplate.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}