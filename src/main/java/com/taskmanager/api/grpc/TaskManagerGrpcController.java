package com.taskmanager.api.grpc;

import com.google.protobuf.Empty;
import com.taskmanager.api.grpc.generated.*;
import com.taskmanager.application.domain.assistent.exception.PromptIsEmptyException;
import com.taskmanager.application.domain.shared.DomainException;
import com.taskmanager.application.domain.task.exception.TaskNotFoundException;
import com.taskmanager.application.port.inbound.assistent.AssistantQueryHandler;
import com.taskmanager.application.port.inbound.assistent.query.AskQuery;
import com.taskmanager.application.port.inbound.task.TaskCommandHandler;
import com.taskmanager.application.port.inbound.task.TaskQueryHandler;
import com.taskmanager.application.port.inbound.task.command.CompleteTaskCommand;
import com.taskmanager.application.port.inbound.task.command.CreateTaskCommand;
import com.taskmanager.application.port.inbound.task.command.UpdateTaskCommand;
import com.taskmanager.application.port.inbound.task.data.TaskData;
import com.taskmanager.application.port.inbound.task.query.FindTaskByIdQuery;
import com.taskmanager.application.port.inbound.task.query.GetAvailableTasksQuery;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class TaskManagerGrpcController extends TaskManagerControllerGrpc.TaskManagerControllerImplBase {

    private final TaskQueryHandler taskQueryHandler;
    private final TaskCommandHandler taskCommandHandler;
    private final AssistantQueryHandler assistantQueryHandler;

    @Override
    public void getTasks(Empty request, StreamObserver<TaskList> responseObserver) {
        try {
            List<Task> tasks = taskQueryHandler.handle(new GetAvailableTasksQuery())
                    .stream()
                    .map(this::convertToGrpcTask)
                    .toList();
            TaskList taskList = TaskList.newBuilder()
                    .addAllTasks(tasks)
                    .build();
            responseObserver.onNext(taskList);
            responseObserver.onCompleted();
        } catch (RuntimeException e) {
            log.error("Cannot get tasks", e);
            responseObserver.onError(Status.INTERNAL.withDescription("Something went wrong").asException());
        }
    }

    @Override
    public void createTask(CreateTaskRequest request, StreamObserver<Task> responseObserver) {
        try {
            CreateTaskCommand command = new CreateTaskCommand(request.getTitle(), request.getDescription());
            TaskData taskData = taskCommandHandler.handle(command);
            responseObserver.onNext(convertToGrpcTask(taskData));
            responseObserver.onCompleted();
        } catch (DomainException e) {
            log.debug("Cannot create task", e);
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asException());
        } catch (RuntimeException e) {
            log.error("Cannot create task", e);
            responseObserver.onError(Status.INTERNAL.withDescription("Something went wrong").asException());
        }
    }

    @Override
    public void findTaskById(FindTaskByIdRequest request, StreamObserver<Task> responseObserver) {
        String taskId = request.getId();
        try {
            FindTaskByIdQuery query = new FindTaskByIdQuery(taskId);
            TaskData taskData = taskQueryHandler.handle(query);
            responseObserver.onNext(convertToGrpcTask(taskData));
            responseObserver.onCompleted();
        } catch (TaskNotFoundException e) {
            log.debug("Cannot find task by id [{}]", taskId, e);
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asException());
        } catch (DomainException e) {
            log.debug("Cannot find task by id [{}]", taskId, e);
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asException());
        } catch (RuntimeException e) {
            log.error("Cannot find task by id [{}]", taskId, e);
            responseObserver.onError(Status.INTERNAL.withDescription("Something went wrong").asException());
        }
    }

    @Override
    public void updateTask(UpdateTaskRequest request, StreamObserver<Task> responseObserver) {
        try {
            UpdateTaskCommand command = new UpdateTaskCommand(request.getId(), request.getTitle(), request.getDescription());
            TaskData taskData = taskCommandHandler.handle(command);
            responseObserver.onNext(convertToGrpcTask(taskData));
            responseObserver.onCompleted();
        } catch (TaskNotFoundException e) {
            log.debug("Cannot update task with id [{}]", request.getId(), e);
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asException());
        } catch (DomainException e) {
            log.debug("Cannot update task with id [{}]", request.getId(), e);
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asException());
        } catch (RuntimeException e) {
            log.error("Cannot update task with id [{}]", request.getId(), e);
            responseObserver.onError(Status.INTERNAL.withDescription("Something went wrong").asException());
        }
    }

    @Override
    public void completeTask(CompleteTaskRequest request, StreamObserver<Task> responseObserver) {
        String taskId = request.getId();
        try {
            CompleteTaskCommand command = new CompleteTaskCommand(taskId);
            TaskData taskData = taskCommandHandler.handle(command);
            responseObserver.onNext(convertToGrpcTask(taskData));
            responseObserver.onCompleted();
        } catch (TaskNotFoundException e) {
            log.debug("Cannot complete task with id [{}]", taskId, e);
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asException());
        } catch (DomainException e) {
            log.debug("Cannot complete task with id [{}]", taskId, e);
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asException());
        } catch (RuntimeException e) {
            log.error("Cannot complete task with id [{}]", taskId, e);
            responseObserver.onError(Status.INTERNAL.withDescription("Something went wrong").asException());
        }
    }

    @Override
    public void chat(ChatRequest request, StreamObserver<ChatResponse> responseObserver) {
        try {
            AskQuery query = new AskQuery(request.getPrompt());
            String response = assistantQueryHandler.handle(query);
            responseObserver.onNext(ChatResponse.newBuilder().setResponse(response).build());
            responseObserver.onCompleted();
        } catch (PromptIsEmptyException e) {
            log.debug("Cannot process chat request", e);
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asException());
        } catch (RuntimeException e) {
            log.error("Cannot process chat request", e);
            responseObserver.onError(Status.INTERNAL.withDescription("Something went wrong").asException());
        }
    }

    private Task convertToGrpcTask(TaskData taskData) {
        return Task.newBuilder()
                .setTitle(taskData.title())
                .setId(taskData.id())
                .setDescription(taskData.description())
                .setCompleted(taskData.completed())
                .build();
    }
}