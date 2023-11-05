package community.flock.wirespec.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import community.flock.wirespec.Wirespec;
import community.flock.wirespec.generated.petstore.FindPetsByStatus;
import community.flock.wirespec.generated.petstore.GetPetById;
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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;


@Configuration
public class PetstoreConfiguration {




    @Bean
    public PetstoreClient petstoreClient(Function<Wirespec.Request<?>, CompletableFuture<Wirespec.Response<byte[]>>> requestHandler, RestTemplate restTemplate, Wirespec.ContentMapper<byte[]> contentMapper) {

        return new PetstoreClient() {

            public <Req extends Wirespec.Request<?>, Res extends Wirespec.Response<?>> CompletableFuture<Res> handle(Req request, BiFunction<Wirespec.ContentMapper<byte[]>, Wirespec.Response<byte[]>, Res> responseMapper){
                return requestHandler
                        .apply(request)
                        .thenApply(response -> responseMapper.apply(contentMapper, response));
            }

            @Override
            public CompletableFuture<GetPetById.Response<?>> getPetById(GetPetById.Request<?> request) {
                return handle(request, GetPetById::RESPONSE_MAPPER);
            }

            @Override
            public CompletableFuture<FindPetsByStatus.Response<?>> findPetsByStatus(FindPetsByStatus.Request<?> request) {
                return handle(request, FindPetsByStatus::RESPONSE_MAPPER);
            }
        };
    }
}
