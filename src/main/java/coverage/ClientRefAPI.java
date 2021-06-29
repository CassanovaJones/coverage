package coverage;

import io.smallrye.mutiny.Uni;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.bson.types.ObjectId;

@Path("/clientref")
public class ClientRefAPI {
  @GET
  @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> list() {
      return ClientRefAPI
        .listAll()
        .onItem()
        .transform(clientRefList -> Response.ok().entity(clientRefList).build())
        .onFailure()
        .recoverWithItem(
          err -> Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build()
      );
    }
  
  @GET
  @Path("/{clientRefId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<Response> getClientRef(@PathParam("clientRefId") String id) {
    return ClientRefAPI
      .findByIdOptional(new ObjectId(id)) 
      .onItem()
      .transform(
        ci -> {
          if (ci.isPresent()){
            return Response.ok().entity(ci.get()).build();
          } else {
            return Response.status(Status.NOT_FOUND).build();
          }
        }
      )
      .onFailure()
      .recoverWithItem(
          err -> Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build()
      );
  }
  @POST
  @Path("/{clientRefId}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<Response> addClientRef(ClientRefAPI a) {
    return a
      .persist()
      .onItem()
      .transform(i -> Response.status(Status.CREATED).build()
      .onFailure()
      .recoverWithItem(
        err -> Response.status(Status.INTERNAL_ERROR).entity(err).build()
        );
  }
  @PUT
  @Path("/{clientRefId}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<Response> updateClientRef(
    @PathParam("clientRefId") String id, ClientRefAPI client) {
      return ClientRefAPI
        .<ClientRefAPI>findByOptionalId(new ObjectId(id))
        .onItem()
        .transformToUni(
          cr -> {
            if (cr.isPresent()) {
              ClientRef client1 = cr.get();
              client1.name = client.name;
              client1.product = client.product;
              client1.results = client.results;

              return client1
                .update()
                .onItem()
                .transform(i -> Response.ok().build())
                .onFailure()
                .recoverWithItem(
                  err -> Response
                    .status(Status.INTERNAL_SERVER_ERROR)
                    .entity(err).build()
                );
            } else {
              return Uni
                .createFrom()
                .item(Response.status(Status.NOT_FOUND).entity(client).build());
            }
          }
        )
        .onFailure()
        .recoverWithItem(
          err -> Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(err).build());
    }
  @DELETE
  public Uni<Response> deleteAllClientRefs(){
    return ClientRefAPI
      .deleteAll()
      .onItem()
      .transform(count -> Response.ok.entity(count).build())
      .onFailure()
      .recoverWithItem(Response.status(Status.INTERNAL_SERVER_ERROR).build());
  }
  @DELETE
  @Path("/{clientRefId}")
  public Uni<Response> deleteClientRefbyId(@PathParam("clientRefId") String id) {
    return ClientRefAPI
      .deleteById(new ObjectId(id))
      .onItem()
      .transform(
        success -> {
          if (success) {
            return Response.ok().build();
          } else {
            return Response.status(Status.NOT_FOUND).build();
          }
        }
      )
      .onFailure()
      .recoverWithItem(Response.status(Status.INTERNAL_SERVER_ERROR));
  }
  @PATCH
  @Path("{/clientRefId}/isAssociatedWith/{entryPointId}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<Response> addEntryPoint(@PathParam("clientRefId") String crId, @PathParam("entryPointId") String epId) {
    return ClientRefAPI
      .findById(new ObjectId(crId))
      .onItem()
      .transform(client.entryPoints.append(epId).build());
        client.update();
        return 
          Response.ok().entity(client).build()
      .onFailure()
      .recoverWithItem( err -> Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build());
  };

}