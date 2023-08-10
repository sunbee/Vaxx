package com.example.vaxz.ui.todo_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vaxz.util.Routes
import com.example.vaxz.data.Todo
import com.example.vaxz.data.TodoRepository
import com.example.vaxz.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TodoListViewModel @Inject constructor(
    private val repository: TodoRepository
): ViewModel() {
    val TAG = "VIEW_MODEL"
    val todos = repository.getTodos()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var deletedTodo: Todo? = null

    /*
    * Handle all user-originated events here.
    * Implement the event-handlers with callback for each type of event
    * registered in the sealed class TodoListEvent.
    * The callbacks use repository, which is injected as dependency in this view model,
    * for CRUD operations.
    * For events that are propagated to the UI for UI updates, use channel.
    * Send the appropriate instance from sealed class UiEvent.
    * Observe the events in the channel with a listener in the root composable.
    * Note that send puts elements in the channel and collect gathers them.
    *
    * */
    fun onEvent(event: TodoListEvent) {
        when(event) {
            is TodoListEvent.OnDeleteTodoClick -> {
                viewModelScope.launch(Dispatchers.IO) {
                    deletedTodo = event.todo
                    repository.deleteTodo(event.todo)
                    withContext(Dispatchers.Main) {
                        _uiEvent.send(UiEvent.ShowSnackBar(
                            message = "Deleted",
                            action = "Undo"
                        ))
                    }
                }
            }
            is TodoListEvent.OnDoneChange -> {
                viewModelScope.launch(Dispatchers.IO) {
                    repository.insertTodo(event.todo.copy(
                        isDone = event.isDone
                    ))
                }
            }
            is TodoListEvent.OnUndoDeleteClick -> {
                deletedTodo?.let {
                    viewModelScope.launch(Dispatchers.IO) {
                        repository.insertTodo(it)
                    }
                }
            }
            is TodoListEvent.OnTodoClick -> {
                viewModelScope.launch(Dispatchers.Main) {
                    _uiEvent.send(UiEvent.Navigate(Routes.ADD_EDIT_TODO + "?todoId=${event.todo.id}"))
                }
            }
            is TodoListEvent.OnAddTodoClick -> {
                viewModelScope.launch(Dispatchers.Main) {
                    _uiEvent.send(UiEvent.Navigate(Routes.ADD_EDIT_TODO + "?todoId=-1"))
                }
            }
        }
    }
}