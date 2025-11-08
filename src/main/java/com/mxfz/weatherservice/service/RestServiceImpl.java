package com.mxfz.weatherservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@Slf4j
public class RestServiceImpl implements RestService {

    private final RestClient restClient;

    public RestServiceImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public <T> T callApi(URI endpoint,
                         HttpMethod method,
                         HttpHeaders requestHeaders,
                         Map<String, ?> requestParams,
                         Object requestBody,
                         ParameterizedTypeReference<T> responseType) {

        log.info("Hitting API: {}", endpoint);

        UriBuilder builder = UriComponentsBuilder.fromUri(endpoint);
        if (!Objects.isNull(requestParams) && !requestParams.isEmpty()) {
            for (Map.Entry<String, ?> entry : requestParams.entrySet()) {
                builder.queryParam(entry.getKey(), entry.getValue());
            }
        }

        Function<UriBuilder, URI> uri = uriBuilder -> builder.build();
        Consumer<HttpHeaders> httpHeaders = headers -> headers.addAll(Objects.isNull(requestHeaders) ? new HttpHeaders() : requestHeaders);

        return restClient
                .method(method)
                .uri(uri)
                .body(Objects.isNull(requestBody) ? "" : requestBody)
                .headers(httpHeaders)
                .retrieve()
                .body(responseType);
    }
}