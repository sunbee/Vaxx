package com.example.vaxz.ui.add_edit_todo

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vaxz.data.Todo
import com.example.vaxz.data.TodoRepository
import com.example.vaxz.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddEditTodoViewModel @Inject constructor(
    private val repository: TodoRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    val TAG = "ADD_TODO"

    var title = mutableStateOf<String>("")
        private set

    var description = mutableStateOf<String?>("")
        private set

    var todo = mutableStateOf<Todo?>(null)
        private set

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _uiEvent2 = MutableSharedFlow<UiEvent>(replay = 0)
    val uiEvent2 = _uiEvent2.asSharedFlow()

    init {
        val todoId = savedStateHandle.get<Int>("todoId")!!
        if (todoId != -1) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.getTodoById(todoId)?.let { todoDB ->
                    title.value = todoDB.title
                    description.value = todoDB.description ?: ""
                    todo.value = todoDB
                }  // end LET
            }  // end COROUTINE
        }  // end IF
    }  // end INIT

    fun onEvent(event: AddEditTodoEvent) {
        when(event) {
            is AddEditTodoEvent.OnTitleChange -> {
                Log.d(TAG, "You triggered OnTitleChange.")
                title.value = event.title
            }
            is AddEditTodoEvent.OnDescriptionChange -> {
                Log.d(TAG, "You triggered OnDescriptionChange.")
                description.value = event.description
            }
            is AddEditTodoEvent.OnSaveTodoClick -> {
                Log.d(TAG, "You triggered OnSaveTodoClick.")
                viewModelScope.launch(Dispatchers.IO) {
                    if (title.value.isBlank()) {
                        withContext(Dispatchers.Main) {
                            /*
                            _uiEvent.send(UiEvent.ShowSnackBar(
                                message = "Found no title!"
                            ))*/
                            Log.d(TAG, "Emitted ShowSnackBar.")
                            _uiEvent2.emit(UiEvent.ShowSnackBar(
                                message = "Found no title",
                                action = null
                            ))
                        }
                        return@launch
                    }
                    repository.insertTodo(Todo(
                        title = title.value,
                        description = description.value ?: "",
                        isDone = todo?.value?.isDone ?: false,
                        id = todo?.value?.id
                    ))
                    withContext(Dispatchers.Main) {
                        //_uiEvent.send(UiEvent.PopBackStack)
                        Log.d(TAG, "Emitted PopBackStack.")
                        _uiEvent2.emit(UiEvent.PopBackStack)
                    }
                }  // end COROUTINE
            }  // end IS
        }  // end WHEN
    }  // end FUN
}