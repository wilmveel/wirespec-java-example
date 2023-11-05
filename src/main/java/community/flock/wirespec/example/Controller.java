package community.flock.wirespec.example;

import community.flock.wirespec.generated.petstore.FindPetsByStatusParameterStatus;
import community.flock.wirespec.generated.petstore.Pet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("pet")
public class Controller {

    @Autowired
    private PetstoreService petstoreService;

    @GetMapping
    public ResponseEntity<List<Pet>> getPet() throws ExecutionException, InterruptedException {
        final var body = petstoreService.findPetsByStatus(FindPetsByStatusParameterStatus.sold);
        return ResponseEntity.ok(body);
    }
}
