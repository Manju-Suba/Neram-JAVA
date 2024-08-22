package pm.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilderFactory;

import java.util.Collections;

@Configuration
public class RestTemplateConfig {


    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());
        restTemplate.setUriTemplateHandler(uriBuilderFactory());
        restTemplate.setInterceptors(Collections.singletonList(authorizationInterceptor()));
        return restTemplate;
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        return new SimpleClientHttpRequestFactory();
    }

    @Bean
    public UriBuilderFactory uriBuilderFactory() {
        return new DefaultUriBuilderFactory();
    }

    @Bean
    public ClientHttpRequestInterceptor authorizationInterceptor() {
        return (request, body, execution) -> {
            request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer deyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIxIiwianRpIjoiODRiMGQ1MDFkMjFjMGFhNzRjOTM4NTg3ZTc0MmQ3MGNlYjk2NWRjMzNmNTkwN2EyNGQ2ZTFlYjAzOWQxMGM1OWRjZmJmZTY4ZGVjYmYwNjEiLCJpYXQiOjE3MDM1NzcxMDguMTA0MDE0LCJuYmYiOjE3MDM1NzcxMDguMTA0MDE2LCJleHAiOjE3MzUxOTk1MDguMDkxNzI1LCJzdWIiOiI2MCIsInNjb3BlcyI6W119.ednhE_5lmC9JGPDNDth5eFsbsPK5N_vAWdgJFDpj66QpmtN8zoeEuTIzeUNB1uRr3_btGRY1zeSyis8TjnnmLBuPnfPE-54NvbAh3p73XdfEpZjVWUflQCmoFzkROiFeXMQxVBbe-ghei9oW41j9_iewYK4jI4IFOIdSK3oPQZ4g1bPST8oeblfFhrs0FiuHv89kHkr_lQp9URAWPr3vEXuafz5Mwya900BuSbVxRbCSN6MhvZ3pkObMBHm9GydiDdBkJF3OXdlF4439c4dc4-cR4V8AyZGUqFkW6AoCRyiU_XVwV9RA2ktLldzbIrcTHum6K5COYf2jkbm0BgJrmRYheA3ouSVSOPlIu4jgqZV_0nA8PMx1G8AWe9uUrCa5fj4zLBXwY4MqMn2GUYtTJSWB-fQ66klNsQx8h7BTwMwcp7AK7DNtrwcwnKFJQbGw1bA5CgumMu4VlqUdPU1w7vY3Gd3zPfMOMnR7F3flJ5tC_T0anDmSWt54JDBZrCDxrbRPQppJsN4-OZCdamFkDIJhS8yNOWpL8k1a47P0_piL_kuUGZMD116uLHG9Cs631fl0dQ9iLmhth1ES2TtDpoRrIwdBcNF29AKCTEUC9yD0vOUfTIG1cfpIbgMctrhdmdHr2rQRfLEwuw75rkj6R9xVVn3zGEpBGtCajztfsXQ");
            return execution.execute(request, body);
        };
    }
}

