package vives.bancovives.rest.divisas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.Map;

/**
 * Controlador REST para la conversión de divisas.
 */
@RestController
public class DivisasConverterController {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Convierte una cantidad de una divisa a otra.
     *
     * @param amount La cantidad de dinero a convertir.
     * @param from   La divisa de origen.
     * @param to     La divisa de destino.
     * @return Un mapa con la información de la conversión.
     */
    @Operation(description = "Convierte una cantidad de una divisa a otra")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Conversión exitosa",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Solicitud inválida"
            )
    })
    @GetMapping("${api.version}/convert")
    public Map<String, Object> convertDivisas(
            @RequestParam double amount,
            @RequestParam String from,
            @RequestParam String to) {

        String url = String.format("https://api.frankfurter.dev/v1/latest?amount=%f&from=%s&to=%s", amount, from, to);
        return restTemplate.getForObject(url, Map.class);
    }
}