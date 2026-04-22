package com.github.phoswald.sample.task;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.databind.ObjectMapper;

@Dependent
class TaskRepository {

    private final ObjectMapper mapper = new ObjectMapper();

    @Inject
    @ConfigProperty(name = "app.task.directory")
    String taskDirectory;

    List<Task> findTasks(Integer skip, Integer limit) {
        try {
            return Files.list(Paths.get(taskDirectory)) //
                    .filter(path -> path.getFileName().toString().endsWith(".json")) //
                    .sorted(Comparator.comparing(Path::toString)) //
                    .skip(skip == null ? 0 : skip.intValue()) //
                    .limit(limit == null ? 100 : limit.intValue()) //
                    .map(this::loadTask) //
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Task loadTask(Path path) {
        try (Reader reader = Files.newBufferedReader(path)) {
            return mapper.readValue(reader, Task.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    String createTask(Task task) {
        var now = Instant.now();
        task.setNewTaskId();
        task.setUserId("guest");
        task.setTimestamp(now.atOffset(ZoneOffset.UTC).toString());
        task.validate();
        storeTask(task);
        return task.getTaskId();
    }

    private void storeTask(Task task) {
        var path = Paths.get(taskDirectory, task.getTaskId() + ".json");
        try (Writer writer = Files.newBufferedWriter(path)) {
            mapper.writeValue(writer, task);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    void deleteTask(String taskId) {
        try {
            var path = Paths.get(taskDirectory, taskId + ".json");
            var pathNew = Paths.get(taskDirectory, taskId + ".json.old");
            Files.move(path, pathNew);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
