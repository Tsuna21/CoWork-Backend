package edu.stage.backend.service;

import edu.stage.backend.model.Task;
import edu.stage.backend.model.User;
import edu.stage.backend.model.Priority;
import edu.stage.backend.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

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
    void testFindById_ReturnsTask_WhenTaskExists() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task1));

        // Act
        Optional<Task> foundTask = taskService.getTaskById(1L);

        // Assert
        assertThat(foundTask).isPresent();
        assertThat(foundTask.get().getTitle()).isEqualTo("Task 1");
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    void testFindById_ReturnsEmpty_WhenTaskDoesNotExist() {
        // Arrange
        when(taskRepository.findById(3L)).thenReturn(Optional.empty());

        // Act
        Optional<Task> foundTask = taskService.getTaskById(3L);

        // Assert
        assertThat(foundTask).isEmpty();
        verify(taskRepository, times(1)).findById(3L);
    }

    @Test
    void testFindAll_ReturnsListOfTasks() {
        // Arrange
        when(taskRepository.findAll()).thenReturn(List.of(task1, task2));

        // Act
        List<Task> tasks = taskService.getAllTasks();

        // Assert
        assertThat(tasks).hasSize(2);
        assertThat(tasks).contains(task1, task2);
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    void testSaveTask_ReturnsSavedTask() {
        // Arrange
        Task newTask = new Task(null, "New Task", "New Description", Priority.MEDIUM, mockUser);
        when(taskRepository.save(any(Task.class))).thenReturn(newTask);

        // Act
        Task savedTask = taskService.createTask(newTask);

        // Assert
        assertThat(savedTask.getTitle()).isEqualTo("New Task");
        verify(taskRepository, times(1)).save(any(Task.class));
    }

   @Test
    void testDeleteTask_DeletesSuccessfully() {
        // Arrange
        Long taskId = 1L;
        when(taskRepository.existsById(taskId)).thenReturn(true); // ✅ Simule que la tâche existe
        doNothing().when(taskRepository).deleteById(taskId); // ✅ Simule la suppression

        // Act
        boolean result = taskService.deleteTask(taskId);

        // Assert
        assertTrue(result); // ✅ Vérifie que la suppression a bien retourné true
        verify(taskRepository, times(1)).existsById(taskId); // ✅ Vérifie que existsById a été appelé
        verify(taskRepository, times(1)).deleteById(taskId); // ✅ Vérifie que deleteById a bien été appelé
    }

    @Test
    void testCreateTask_SavesTaskSuccessfully() {
        // Arrange
        Task taskToSave = new Task();
        Task savedTask = new Task(1L, "Nouvelle tâche", "Description de la tâche", Priority.MEDIUM, mockUser);

        when(taskRepository.save(taskToSave)).thenReturn(savedTask);

        // Act
        Task result = taskService.createTask(new Task());

        // Assert
        assertNotNull(result);
        assertEquals(savedTask.getId(), result.getId());
        assertEquals(savedTask.getTitle(), result.getTitle());
        assertEquals(savedTask.getDescription(), result.getDescription());
        assertEquals(savedTask.getPriority(), result.getPriority());

        // Vérifier que `save()` a bien été appelé une fois
        verify(taskRepository, times(1)).save(taskToSave);
    }

    @Test
    void testUpdateTask_UpdatesSuccessfully() {
        // Arrange
        Long taskId = 1L;
        Task existingTask = new Task(taskId, "Ancien Titre", "Ancienne Description", Priority.LOW, mockUser);
        Task updatedTaskDetails = new Task(taskId, "Nouveau Titre", "Nouvelle Description", Priority.HIGH, mockUser);
        Task updatedTask = new Task(taskId, "Nouveau Titre", "Nouvelle Description", Priority.HIGH, mockUser);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

        // Act
        Optional<Task> result = taskService.updateTask(taskId, updatedTaskDetails);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Nouveau Titre");
        assertThat(result.get().getDescription()).isEqualTo("Nouvelle Description");
        assertThat(result.get().getPriority()).isEqualTo(Priority.HIGH);

        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).save(existingTask);
    }

}
