package community.flock.wirespec.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import community.flock.wirespec.Wirespec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class WirespecConfiguration {

    public interface RequestHandler<Req extends Wirespec.Request<?>> extends Function<Req, CompletableFuture<Wirespec.Response<byte[]>>> {
    }

    ;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Wirespec.ContentMapper<byte[]> contentMapper(ObjectMapper objectMapper) {

        final var wirespecObjectMapper = objectMapper.copy()
                .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
                .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);

        return new Wirespec.ContentMapper<>() {
            @Override
            public <T> Wirespec.Content<T> read(Wirespec.Content<byte[]> content, Type valueType) {
                var type = wirespecObjectMapper.constructType(valueType);
                try {
                    T obj = wirespecObjectMapper.readValue(content.body(), type);
                    return new Wirespec.Content<T>(content.type(), obj);
                } catch (IOException e) {
                    throw new RuntimeException("Cannot read content", e);
                }
            }

            @Override
            public <T> Wirespec.Content<byte[]> write(Wirespec.Content<T> content) {
                try {
                    var bytes = wirespecObjectMapper.writeValueAsBytes(content.body());
                    return new Wirespec.Content<>(content.type(), bytes);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Cannot write content", e);
                }
            }
        };
    }

    @Bean
    public <Req extends Wirespec.Request<?>> RequestHandler<Req> requestHandler(ObjectMapper objectMapper, RestTemplate restTemplate, Wirespec.ContentMapper<byte[]> contentMapper) {
        return request -> {
            final var query = request.getQuery().entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            it -> it.getValue().stream().map(v -> {
                                try {
                                    return objectMapper.writeValueAsString(v).replaceAll("^\"|\"$", "");
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                            }).collect(Collectors.toList()))
                    );

            final var uri = UriComponentsBuilder
                    .fromUriString("https://petstore3.swagger.io/api/v3" + request.getPath())
                    .queryParams(new LinkedMultiValueMap<>(query))
                    .build()
                    .toUri();

            final var result = restTemplate.execute(
                    uri,
                    HttpMethod.valueOf(request.getMethod().name()),
                    req -> {
                        if (request.getContent() != null) {
                            final var content = contentMapper.write(request.getContent());
                            req.getBody().write(content.body());
                        }
                    },
                    res -> {
                        final var statusCode = res.getStatusCode().value();
                        final var contentType = res.getHeaders().getContentType().toString();
                        final var content = new Wirespec.Content<>(contentType, res.getBody().readAllBytes());

                        return new Wirespec.Response<byte[]>() {
                            @Override
                            public int getStatus() {
                                return statusCode;
                            }

                            @Override
                            public Map<String, List<Object>> getHeaders() {
                                return null;
                            }

                            @Override
                            public Wirespec.Content<byte[]> getContent() {
                                return content;
                            }
                        };
                    }
            );

            return CompletableFuture.completedFuture(result);
        };
    }

}
