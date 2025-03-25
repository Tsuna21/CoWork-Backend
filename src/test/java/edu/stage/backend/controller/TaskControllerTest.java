package edu.stage.backend.controller;

import edu.stage.backend.service.TaskService;
import edu.stage.backend.model.Task;
import edu.stage.backend.model.User;
import edu.stage.backend.model.Priority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private Task task1;
    private Task task2;

    private User mockUser;

    

    @BeforeEach
    void setUp() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setPassword("password123");
        task1 = new Task(1L, "Task 1", "Description 1", Priority.HIGH, mockUser);
        task2 = new Task(2L, "Task 2", "Description 2", Priority.LOW, mockUser);
    }

    @Test
    void testGetAllTasks_ReturnsTaskList() {
        // Arrange
        List<Task> taskList = Arrays.asList(task1, task2);
        when(taskService.getAllTasks()).thenReturn(taskList);

        // Act
        ResponseEntity<List<Task>> response = taskController.getAllTasks();

        // Assert
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()).contains(task1, task2);
        verify(taskService, times(1)).getAllTasks();
    }

    @Test
    void testGetTaskById_ReturnsTask_WhenTaskExists() {
        // Arrange
        when(taskService.getTaskById(1L)).thenReturn(Optional.of(task1));

        // Act
        ResponseEntity<Task> response = taskController.getTaskById(1L);

        // Assert
        assertThat(response.getBody()).isEqualTo(task1);
        verify(taskService, times(1)).getTaskById(1L);
    }

    @Test
    void testGetTaskById_ReturnsNotFound_WhenTaskDoesNotExist() {
        // Arrange
        when(taskService.getTaskById(3L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Task> response = taskController.getTaskById(3L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());  // ✅ Correction ici
        verify(taskService, times(1)).getTaskById(3L);
    }

    @Test
    void testCreateTask_ReturnsCreatedTask() {
        // Arrange
        Task newTask = new Task(null, "New Task", "New Description", Priority.MEDIUM, mockUser);
        Task savedTask = new Task(1L, "New Task", "New Description", Priority.MEDIUM, mockUser);
        when(taskService.createTask(any(Task.class))).thenReturn(savedTask);
        
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "password", List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        // Act
        ResponseEntity<Task> response = taskController.createTask(newTask);

        // Assert
        assertThat(response.getBody()).isEqualTo(savedTask);
        verify(taskService, times(1)).createTask(any(Task.class));
    }

    @Test
    void testDeleteTask_ReturnsNoContent_WhenTaskExists() {
        // Arrange
        when(taskService.deleteTask(1L)).thenReturn(true);

        // Act
        ResponseEntity<Void> response = taskController.deleteTask(1L);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());  // ✅ Correction ici
        verify(taskService, times(1)).deleteTask(1L);
    }

    @Test
    void testDeleteTask_ReturnsNotFound_WhenTaskDoesNotExist() {
        // Arrange
        when(taskService.deleteTask(3L)).thenReturn(false);

        // Act
        ResponseEntity<Void> response = taskController.deleteTask(3L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());  // ✅ Correction ici
        verify(taskService, times(1)).deleteTask(3L);
    }
}
