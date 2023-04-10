package akhid.development.controller;

import akhid.development.service.MinioService;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.simpleframework.xml.Path;

import javax.inject.Inject;
import javax.validation.ValidationException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MinioController {

    @Inject
    MinioService minioService;

    @POST
    @Path("/bucket/files/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response uploadFile(@MultipartForm MultipartFormDataInput fileInput) throws Exception {

        Map<String, Object> result = new HashMap<>();
        try {
            result.put("statusCode", 200);
            result.put("data", minioService.submit(fileInput));
            return Response.ok(result).build();
        } catch (ValidationException ex) {
            result.put("message", ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(result).build();
        } catch (NotFoundException nfe) {
            result.put("message", nfe.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(result).build();
        }  catch (Exception ex) {
            ex.printStackTrace();
            result.put("statusCode", 500);
            result.put("message", "INTERNAL_SERVER_ERROR");
            return Response.serverError()
                    .entity(result)
                    .build();
        }
    }

    @GET
    @Path("/bucket/files")
    public Response getAll() {

        Map<String, Object> result = new HashMap<>();
        try {
            result.put("statusCode", 200);
            result.put("data", minioService.getAllFiles());
            return Response.ok(result).build();
        } catch (ValidationException ex) {
            result.put("message", ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(result).build();
        } catch (NotFoundException nfe) {
            result.put("message", nfe.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(result).build();
        }  catch (Exception ex) {
            ex.printStackTrace();
            result.put("statusCode", 500);
            result.put("message", "INTERNAL_SERVER_ERROR");
            return Response.serverError()
                    .entity(result)
                    .build();
        }
    }

}
