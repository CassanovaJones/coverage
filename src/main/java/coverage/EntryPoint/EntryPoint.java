package coverage.EntryPoint;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity;
import java.util.ArrayList;

public class EntryPoint extends ReactivePanacheMongoEntity {

  public String name;
  public String description;

  ArrayList<String> capabilities;
  ArrayList<String> clientRefs;
  ArrayList<String> products;
  

  EntryPoint updateFields(EntryPoint a) {
    this.name = a.name;
    this.description = a.description;
    this.capabilities = a.capabilities; //Make a copy of arrayList

    return this;
  }
}
