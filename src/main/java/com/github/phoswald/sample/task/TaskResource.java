package com.github.phoswald.sample.task;

import java.io.UncheckedIOException;
import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.jboss.logging.Logger;

@RequestScoped
@Path("/rest/tasks")
public class TaskResource {

    private final Logger logger = Logger.getLogger(getClass());

    @Inject
    TaskRepository repository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response findTasks(//
            @QueryParam("skip") Integer skip, //
            @QueryParam("limit") Integer limit) {
        try {
            var response = repository.findTasks(skip, limit);
            logger.infov("Tasks found: count={0}", Integer.valueOf(response.size()));
            return Response.ok(new GenericEntity<List<Task>>(response) { }).build();
        } catch (UncheckedIOException e) {
            logger.error("Task search failed: ", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createTask(Task request) {
        try {
            var id = repository.createTask(request);
            logger.infov("Task created: taskId={0}, title={1}", id, request.getTitle());
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            logger.warnv("Task creation failed: {0}", e.toString());
            return Response.status(Status.BAD_REQUEST).build();
        } catch (UncheckedIOException e) {
            logger.error("Task creation failed: ", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DELETE
    @Path("{taskId}")
    public Response deleteTask(@PathParam("taskId") String taskId) {
        try {
            repository.deleteTask(taskId);
            logger.infov("Task deleted: id={0}", taskId);
            return Response.ok().build();
        } catch (UncheckedIOException e) {
            logger.error("Task deletion failed: ", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
