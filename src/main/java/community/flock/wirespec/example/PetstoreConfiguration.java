package community.flock.wirespec.example;

import community.flock.wirespec.Wirespec;
import community.flock.wirespec.generated.petstore.FindPetsByStatus;
import community.flock.wirespec.generated.petstore.GetPetById;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

@Configuration
public class PetstoreConfiguration {

    interface ResponseMapper<Res extends Wirespec.Response<?>> extends BiFunction<Wirespec.ContentMapper<byte[]>, Wirespec.Response<byte[]>, Res> { }

    @Bean
    public PetstoreClient petstoreClient(WirespecConfiguration.RequestHandler<Wirespec.Request<?>> requestHandler, Wirespec.ContentMapper<byte[]> contentMapper) {

        return new PetstoreClient() {

            public <Req extends Wirespec.Request<?>, Res extends Wirespec.Response<?>> CompletableFuture<Res> handle(Req request, ResponseMapper<Res> responseMapper) {
                return requestHandler
                        .apply(request)
                        .thenApply(response -> responseMapper.apply(contentMapper, response));
            }

            @Override
            public CompletableFuture<GetPetById.Response<?>> getPetById(GetPetById.Request<?> request){
                return handle(request, GetPetById::RESPONSE_MAPPER);
            }

            @Override
            public CompletableFuture<FindPetsByStatus.Response<?>> findPetsByStatus(FindPetsByStatus.Request<?> request) {
                return handle(request, FindPetsByStatus::RESPONSE_MAPPER);
            }
        };
    }
}
