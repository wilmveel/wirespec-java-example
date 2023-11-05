package community.flock.wirespec.example;

import community.flock.wirespec.generated.petstore.FindPetsByStatus;
import community.flock.wirespec.generated.petstore.FindPetsByStatusParameterStatus;
import community.flock.wirespec.generated.petstore.Pet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class PetstoreService {

    @Autowired
    public PetstoreClient petstoreClient;

    public List<Pet> findPetsByStatus(FindPetsByStatusParameterStatus status) throws ExecutionException, InterruptedException {
        final var req = new FindPetsByStatus.RequestVoid(Optional.of(status));
        final var res = petstoreClient.findPetsByStatus(req).get();
        return switch (res){
            case FindPetsByStatus.Response200ApplicationJson r -> r.getContent().body();
            case FindPetsByStatus.Response200ApplicationXml r -> r.getContent().body();
            case FindPetsByStatus.Response400Void ignored -> throw new IllegalStateException("Pet not found");
        };
    }

}
