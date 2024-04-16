package com.google.mediapipe.examples.llminference

import android.graphics.drawable.Icon
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
internal fun ChatRoute(
    chatViewModel: ChatViewModel = viewModel(
        factory = ChatViewModel.getFactory(LocalContext.current.applicationContext)
    )
) {
    val uiState by chatViewModel.uiState.collectAsStateWithLifecycle()
    val textInputEnabled by chatViewModel.isTextInputEnabled.collectAsStateWithLifecycle()
    val audioModeEnabled by chatViewModel.audioModeEnabled.collectAsStateWithLifecycle()
    val isRecording by chatViewModel.isRecording.collectAsStateWithLifecycle()
    val transcription by chatViewModel.transcription.collectAsStateWithLifecycle()

    ChatScreen(
        uiState,
        textInputEnabled,
        audioModeEnabled,
        isRecording,
        onSendMessage = { chatViewModel.sendMessage(it) },
        onToggleAudioMode = { chatViewModel.toggleAudioMode() },
        onToggleRecording = { chatViewModel.toggleRecording() },
        onOptionSelected = { /* Handle option selection */ },
        onPlayRecording = { chatViewModel.playRecording() },
        transcription = transcription,
        onEditTranscription = { newTranscription -> chatViewModel.editTranscription(newTranscription) }

    )
}


@Composable
fun ChatScreen(
    uiState: UiState,
    textInputEnabled: Boolean = true,
    audioModeEnabled: Boolean = false,
    isRecording: Boolean = false,
    onSendMessage: (String) -> Unit,
    onToggleAudioMode: () -> Unit,
    onToggleRecording: () -> Unit,
    onOptionSelected: (String) -> Unit,
    onPlayRecording: () -> Unit,
    transcription: String,
    onEditTranscription: (String) -> Unit
) {
    var userMessage by rememberSaveable { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Audio mode toggle at the top center
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Audio Mode")
            Switch(
                checked = audioModeEnabled,
                onCheckedChange = { onToggleAudioMode() },
                colors = SwitchDefaults.colors()
            )
        }

        // Message display area
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            reverseLayout = true
        ) {
            items(uiState.messages) { chat ->
                ChatItem(chat)
            }
        }

        // Bottom center controls for recording and options
        if (audioModeEnabled) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                if (isRecording) {
                    Button(onClick = { onToggleRecording() }) {
                        Text("Stop")
                    }
                } else {
                    Button(onClick = { onToggleRecording() }) {
                        Text("Record")
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = {
                                Log.d("ChatScreen", "Edit button clicked")
                                onOptionSelected("Edit")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit")
                        }
                        IconButton(
                            onClick = {
                                onOptionSelected("Listen")
                                Log.d("ChatScreen", "Listen button clicked")
                                onPlayRecording()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Listen")
                        }
                        IconButton(
                            onClick = {
                                Log.d("ChatScreen", "Send button clicked")
                                onOptionSelected("Send")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                        }
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextField(
                                value = transcription,
                                onValueChange = { onEditTranscription(it) },
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                            )
                            IconButton(
                                onClick = {
                                    if (transcription.isNotBlank()) {
                                        onSendMessage(transcription)
                                        onEditTranscription("")
                                    }
                                },
                                modifier = Modifier.align(Alignment.CenterVertically),
                                enabled = textInputEnabled
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                            }
                        }



                        //Icon(Icons.Filled.Edit, contentDescription = "Edit")
                        //Icon(Icons.Filled.PlayArrow, contentDescription = "Listen")
                        //Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")


                        //Button(onClick = { onOptionSelected("Edit") }) { Text("Edit") }
                        //Button(onClick = { onOptionSelected("Listen") }) { Text("Listen") }
                        //Button(onClick = { onOptionSelected("Abrakadabra") }) { Text("Abrakadabra") }
                    }
                }
            }
        } else {
            // Text input and send button only visible when not in audio mode
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = userMessage,
                    onValueChange = { userMessage = it },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    label = { Text("Type a message") },
                    modifier = Modifier.weight(1f),
                    enabled = textInputEnabled
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (userMessage.isNotBlank()) {
                            onSendMessage(userMessage)
                            userMessage = ""
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterVertically),
                    enabled = textInputEnabled
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        }
    }
}

@Composable
fun ChatItem(
    chatMessage: ChatMessage
) {
    val backgroundColor = if (chatMessage.isFromUser) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    val bubbleShape = if (chatMessage.isFromUser) {
        RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp)
    } else {
        RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
    }

    val horizontalAlignment = if (chatMessage.isFromUser) {
        Alignment.End
    } else {
        Alignment.Start
    }

    Column(
        horizontalAlignment = horizontalAlignment,
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth()
    ) {
        val author = if (chatMessage.isFromUser) {
            stringResource(R.string.user_label)
        } else {
            stringResource(R.string.model_label)
        }
        Text(
            text = author,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row {
            BoxWithConstraints {
                Card(
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    shape = bubbleShape,
                    modifier = Modifier.widthIn(0.dp, maxWidth * 0.9f)
                ) {
                    if (chatMessage.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        Text(
                            text = chatMessage.message,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}