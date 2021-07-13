package coverage;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.LinkedHashMap;
import javax.inject.Inject;
import org.jboss.logging.Logger;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class CapabilitiesTest {

  final String capabilityName = "Test Capabilities";
  final String newName = "New Capability Name";

  @Inject
  Logger log;

  String exampleCapabilityJson() {
    return new JSONObject()
      .put("name", capabilityName)
      .put("description", "Built on Kubernetes Platform")
      .put("Entry Point", "example..")
      .toString();
  }

  String convertToJsonString(LinkedHashMap<String, String> c) {
    return new JSONObject()
      .put("name", c.get("name"))
      .put("description", c.get("description"))
      .put("entryPoints", c.get("entryPoints"))
      .toString();
  }

  @Test
  public void testCapabilitiesEndpoint() {
    given().when().get("/capabilities").then().statusCode(200);
  }

  @Test
  public void testCapabilityAddAndDeleteAll() {
    String c = exampleCapabilityJson();

    given().when().delete("/capabilities").then().statusCode(200);

    String addedCapabilityURI = given()
      .contentType(ContentType.JSON)
      .body(c)
      .when()
      .post("/capabilities")
      .then()
      .statusCode(201)
      .extract()
      .response() //is URL
      .asString();

    given()
      .when()
      .get(addedCapabilityURI)
      .then()
      .statusCode(200)
      .body("name", equalTo(capabilityName));

    given().when().delete("/capabilities").then().statusCode(200);
  }

  @Test
  public void testUpdateCapability() {
    String a = exampleCapabilityJson();

    given().when().delete("/capabilities").then().statusCode(200);

    String addedCapabilityURI = given()
      .contentType(ContentType.JSON)
      .body(a)
      .when()
      .post("/capabilities")
      .then()
      .statusCode(201)
      .extract()
      .response()
      .asString();

    LinkedHashMap<String, String> a_map = given()
      .when()
      .get(addedCapabilityURI)
      .then()
      .statusCode(200)
      .extract()
      .path("$");

    String id = a_map.get("id");
    a_map.put("name", newName);
    String a1 = new JSONObject(a_map).toString();

    given()
      .pathParam("id", id)
      .contentType(ContentType.JSON)
      .body(a1)
      .when()
      .put("/capabilities/{id}")
      .then()
      .statusCode(200);

    given()
      .when()
      .get(addedCapabilityURI)
      .then()
      .statusCode(200)
      .body("name", equalTo(newName));

    given().when().delete("/capabilities").then().statusCode(200);
  }

  @Test
  public void getCapability() {
    String a = exampleCapabilityJson();

    given().when().delete("/capabilities").then().statusCode(200);

    given()
      .contentType(ContentType.JSON)
      .body(a)
      .when()
      .post("/capabilities")
      .then()
      .statusCode(201);

    String id = given()
      .when()
      .get("/capabilities")
      .then()
      .statusCode(200)
      .extract()
      .path("[0].id");

    given()
      .pathParam("id", id)
      .when()
      .get("/capabilities/{id}")
      .then()
      .statusCode(200);

    given().when().delete("/capabilities").then().statusCode(200);
  }

  @Test
  public void failOnBadCapabilityId() {
    given().when().delete("/capabilities").then().statusCode(200);

    given()
      .pathParam("id", "60d1434f7fe4d40a3c74d8c7")
      .when()
      .get("/capabilities/{id}")
      .then()
      .statusCode(404);
  }

  @Test
  public void testDeleteCapability() {
    String a = exampleCapabilityJson();

    given().when().delete("/capabilities").then().statusCode(200);

    given()
      .contentType(ContentType.JSON)
      .body(a)
      .when()
      .post("/capabilities")
      .then()
      .statusCode(201);

    String id = given()
      .when()
      .get("/capabilities")
      .then()
      .statusCode(200)
      .extract()
      .path("[0].id");

    given()
      .pathParam("id", id)
      .when()
      .delete("/capabilities/{id}")
      .then()
      .statusCode(200);

    given().when().delete("/capabilities").then().statusCode(200);
  }
}