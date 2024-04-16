package com.google.mediapipe.examples.llminference

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.launch

class ChatViewModel(
    private val inferenceModel: InferenceModel,
    private val audioRepository: AudioRepository,
    private val whisperServiceConnection: WhisperServiceConnection
) : ViewModel() {

    private val _transcription = MutableStateFlow("")
    val transcription: StateFlow<String> = _transcription.asStateFlow()

    // `GemmaUiState()` is optimized for the Gemma model.
    // Replace `GemmaUiState` with `ChatUiState()` if you're using a different model
    private val _uiState: MutableStateFlow<GemmaUiState> = MutableStateFlow(GemmaUiState())
    val uiState: StateFlow<UiState> =
        _uiState.asStateFlow()

    private val _textInputEnabled: MutableStateFlow<Boolean> =
        MutableStateFlow(true)
    val isTextInputEnabled: StateFlow<Boolean> =
        _textInputEnabled.asStateFlow()

    // New state for Audio Mode
    private val _audioModeEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val audioModeEnabled: StateFlow<Boolean> = _audioModeEnabled.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    init {
        whisperServiceConnection.bindService()
    }

    override fun onCleared() {
        super.onCleared()
        whisperServiceConnection.unbindService()
    }

    // Method to update transcription and make it editable
    fun editTranscription(newTranscription: String) {
        _transcription.value = newTranscription
    }

    fun toggleRecording() {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isRecording.value) {
                audioRepository.stopRecording()
                whisperServiceConnection.transcribeAudio(audioRepository.recordingUri.toString(), object :
                    WhisperServiceConnection.TranscriptionCallback {
                    override fun onTranscriptionReceived(transcription: String) {
                        Log.d("ChatViewModel", "Transcription result: $transcription")
                        _transcription.value = transcription
                    }

                    override fun onTranscriptionError(error: String) {
                        Log.e("ChatViewModel", "Transcription error: $error")
                    }
                })
            } else {
                audioRepository.startRecording(audioRepository.recordingFilePath)
            }
            _isRecording.value = !_isRecording.value
        }
    }

    fun playRecording() {
        Log.d("ChatViewModel", "Playing recording")
        audioRepository.playRecording()
    }

    // Toggle function for Audio Mode
    fun toggleAudioMode() {
        _audioModeEnabled.value = !_audioModeEnabled.value
        Log.d("ChatViewModel", "Toggling audio mode ${_audioModeEnabled.value}")
    }

    fun sendMessage(userMessage: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value.addMessage(userMessage, USER_PREFIX)
            var currentMessageId: String? = _uiState.value.createLoadingMessage()
            setInputEnabled(false)
            try {
                val fullPrompt = _uiState.value.fullPrompt
                inferenceModel.generateResponseAsync(fullPrompt)
                inferenceModel.partialResults
                    .collectIndexed { index, (partialResult, done) ->
                        currentMessageId?.let {
                            if (index == 0) {
                                _uiState.value.appendFirstMessage(it, partialResult)
                            } else {
                                _uiState.value.appendMessage(it, partialResult, done)
                            }
                            if (done) {
                                currentMessageId = null
                                // Re-enable text input
                                setInputEnabled(true)
                            }
                        }
                    }
            } catch (e: Exception) {
                _uiState.value.addMessage(e.localizedMessage ?: "Unknown Error", MODEL_PREFIX)
                setInputEnabled(true)
            }
        }
    }

    private fun setInputEnabled(isEnabled: Boolean) {
        _textInputEnabled.value = isEnabled
    }

    companion object {
        fun getFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val inferenceModel = InferenceModel.getInstance(context)
                val audioRepository = AudioRepository(context)
                val whisperServiceConnection = WhisperServiceConnection(context)
                return ChatViewModel(inferenceModel, audioRepository, whisperServiceConnection) as T
            }
        }
    }
}