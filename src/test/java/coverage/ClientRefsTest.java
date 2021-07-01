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
public class ClientRefsTest {

  final String clientRefsName = "Test Capabilities";
  final String newName = "New ClientRefs Name";

  @Inject
  Logger log;

  String exampleClientRefsJson() {
    return new JSONObject()
      .put("name", clientRefsName)
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
    given().when().get("/clientRefs").then().statusCode(200);
  }

  @Test
  public void testClientRefsAddAndDeleteAll() {
    String c = exampleClientRefsJson();

    given().when().delete("/clientRefs").then().statusCode(200);

    String addedClientRefsURI = given()
      .contentType(ContentType.JSON)
      .body(c)
      .when()
      .post("/clientRefs")
      .then()
      .statusCode(201)
      .extract()
      .response()
      .asString();

    given()
      .when()
      .get(addedClientRefsURI)
      .then()
      .statusCode(200)
      .body("name", equalTo(clientRefsName));

    given().when().delete("/clientRefs").then().statusCode(200);
  }

  @Test
  public void testUpdateClientRefs() {
    String a = exampleClientRefsJson();

    given().when().delete("/clientRefs").then().statusCode(200);

    String addedClientRefsURI = given()
      .contentType(ContentType.JSON)
      .body(a)
      .when()
      .post("/clientRefs")
      .then()
      .statusCode(201)
      .extract()
      .response()
      .asString();

    LinkedHashMap<String, String> a_map = given()
      .when()
      .get(addedClientRefsURI)
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
      .put("/clientRefs/{id}")
      .then()
      .statusCode(200);

    given()
      .when()
      .get(addedClientRefsURI)
      .then()
      .statusCode(200)
      .body("name", equalTo(newName));

    given().when().delete("/clientRefs").then().statusCode(200);
  }

  @Test
  public void getClientRefs() {
    String a = exampleClientRefsJson();

    given().when().delete("/clientRefs").then().statusCode(200);

    given()
      .contentType(ContentType.JSON)
      .body(a)
      .when()
      .post("/clientRefs")
      .then()
      .statusCode(201);

    String id = given()
      .when()
      .get("/clientRefs")
      .then()
      .statusCode(200)
      .extract()
      .path("[0].id");

    given()
      .pathParam("id", id)
      .when()
      .get("/clientRefs/{id}")
      .then()
      .statusCode(200);

    given().when().delete("/clientRefs").then().statusCode(200);
  }

  @Test
  public void failOnBadClientRefsId() {
    given().when().delete("/clientRefs").then().statusCode(200);

    given()
      .pathParam("id", "60d1434f7fe4d40a3c74d8c7")
      .when()
      .get("/clientRefs/{id}")
      .then()
      .statusCode(404);
  }

  @Test
  public void testDeleteClientRefs() {
    String a = exampleClientRefsJson();

    given().when().delete("/clientRefs").then().statusCode(200);

    given()
      .contentType(ContentType.JSON)
      .body(a)
      .when()
      .post("/clientRefs")
      .then()
      .statusCode(201);

    String id = given()
      .when()
      .get("/clientRefs")
      .then()
      .statusCode(200)
      .extract()
      .path("[0].id");

    given()
      .pathParam("id", id)
      .when()
      .delete("/clientRefs/{id}")
      .then()
      .statusCode(200);

    given().when().delete("/clientRefs").then().statusCode(200);
  }
}