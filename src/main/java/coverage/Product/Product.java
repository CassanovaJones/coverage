package coverage.Product;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity;
import java.util.ArrayList;

public class Product extends ReactivePanacheMongoEntity {

  public String name;
  public String description;
  
  ArrayList<String> entryPoints;
  ArrayList<String> clientRef;
  ArrayList<String> capabilities;
  
  Product updateFields(Product a) {
    this.name = a.name;
    this.description = a.description;

    return this;
  }
}