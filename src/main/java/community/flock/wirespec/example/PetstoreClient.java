package community.flock.wirespec.example;


import community.flock.wirespec.generated.petstore.FindPetsByStatus;
import community.flock.wirespec.generated.petstore.GetPetById;

public interface PetstoreClient extends FindPetsByStatus, GetPetById {
}