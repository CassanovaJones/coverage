package coverage;

import io.smallrye.mutiny.Uni;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.bson.types.ObjectId;

@PATH("/clientref")
public class ClientReferencesAPI {
  @GET
  @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> list() {
      return ClientRef
        .listAll()
        .onItem()
        .transform(clientRefList -> Response.ok().entity(clientRefList).build())
        .onFailure()
        .recoverWithItem(
          err -> Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build()
      );
    }
  
  @GET
  @PATH("/{clientrefId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<Response> GetClientref(@PathParam("clientrefId") String id) {
    return ClientRef
      .findByIdOptional(new ObjectId(id)) 
      .onItem()
      .transform(
        ci -> {
          if (ci.ispresent()){
            return Response.ok().entity(ci.get()).build();
          } else {
            return Response.status(Status.NOT_FOUND).build();
          }
        }
      )
      .onFailure()
      .recoverWithItem(
          err -> Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build()
      )
  }

}