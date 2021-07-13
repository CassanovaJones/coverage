package coverage;

import io.smallrye.mutiny.Uni;

import java.security.DrbgParameters.Capability;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
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


@Path("/capabilities")
public class CapabilitiesAPI {
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<Response> list() {
    return Capabilities
      .listAll()
      .onItem()
      .transform(cap -> Response.ok().entity(cap).build())
      .onFailure()
      .recoverWithItem(
        err -> Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build()
      );

  }
  @GET
  @Path("/{capabilityId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<Response> getCap(@PathParam("capabilityId") String id) {
    return Capabilities
      .findByIdOptional(new ObjectId(id))
      .onItem()
      .transform(
        capOp -> {
          if (capOp.isPresent()){
            return Response.ok().entity(capOp).build();  
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
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<Response> addCap(Capabilities a, @Context UriInfo uriInfo) {
    return a
      .persist()
      .onItem()
      .transform(i -> Response.status(Status.CREATED).build())
      .onFailure()
      .recoverWithItem(
        err -> Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build()
      );
  }
  @PUT
  @Path("/{capabilityId}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<Response> updateCap(
    @PathParam("capabilityId") String id, Capabilities cap) {
    return Capabilities
      .<Capabilities>findByIdOptional(new ObjectId(id))
      .onItem()
      .transformToUni(
        co -> {
          if (co.isPresent()) {
            Capabilities cap1 = co.get();
            cap1.name = cap.name;
            cap1.description = cap.description;
            cap1.entryPoints = cap.entryPoints;

            return cap1
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
              .item(Response.status(Status.NOT_FOUND).entity(cap).build());
          }
        }
      )
      .onFailure()
      .recoverWithItem(err -> Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build());
  }
  @DELETE
  public Uni<Response> deleteAllCapabilities() {
    return Capabilities
      .deleteAll()
      .onItem()
      .transform(count -> Response.ok().entity(count).build())
      .onFailure()
      .recoverWithItem(Response.status(Status.INTERNAL_SERVER_ERROR).build());
  }
  @DELETE
  @Path("/{capabilityId}")
  public Uni<Response> deleteCapabilityById(@PathParam("capabilityId") String id) {
    return Capabilities
      .deleteById(new Object(id))
      .onItem()
      .transform(
        succeeded -> {
          if (succeeded) {
            return Response.ok().build();
          } else {
            return Response.status(Status.NOT_FOUND).build();
          }
        }
      )
      .onFailure()
      .recoverWithItem(Response.status(Status.INTERNAL_SERVER_ERROR).build());
  }
  @PATCH
  @Path("{/capbilityId}/hasEntryPoint/{entryPointId}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<Response> addEntryPoint(@PathParam("capabilityId") String capId, @PathParam("entryPointId") String epId) {
    return Capabilities
      .<Capabilities>findByIdOptional(new ObjectId(capId))
      .onItem()
      .transformToUni(                                            //Prevents two Unis from being returned
        capOpt -> {
          if (capOpt.isPresent()) {
                   capOpt.get().entryPoints.add(epId);
            return capOpt.get().update();
          } else { 
              throw new NotFoundException();
          }
            
        }
      )
      .onItem()
      .transform(v -> {return Response.ok().build();})
      .onFailure(f -> {
        if (f.getClass()== NotFoundException.class){
          return Response.status(Status.NOT_FOUND) != null; 
        } else {
          return false;
        }
      })
      .recoverWithItem( err -> Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build());
  };
}
