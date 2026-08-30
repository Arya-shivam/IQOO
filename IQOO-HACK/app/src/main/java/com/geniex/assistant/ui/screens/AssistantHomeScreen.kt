package com.geniex.assistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.geniex.assistant.data.db.MeetingEntity
import com.geniex.assistant.data.db.TaskEntity
import com.geniex.assistant.model.TaskStatus
import com.geniex.assistant.ui.AssistantUiState
import java.time.LocalDate

@Composable
fun AssistantHomeScreen(
    state: AssistantUiState,
    onSubmitChatInput: (String) -> Unit,
    onUpdateTask: (TaskEntity) -> Unit,
    onClearAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    var chatInput by rememberSaveable { mutableStateOf("") }
    var showClearConfirmation by remember { mutableStateOf(false) }
    val openTasks = state.tasks.filter { it.status != TaskStatus.COMPLETED }
    val chatListState = rememberLazyListState()
    val oldestFirstMessages = state.meetings.asReversed()

    LaunchedEffect(oldestFirstMessages.size, state.loading, state.timetable.size) {
        if (oldestFirstMessages.isNotEmpty() || state.loading) {
            chatListState.animateScrollToItem((oldestFirstMessages.size * 2 + 4).coerceAtLeast(0))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ChatHeader()
        if (state.modelError != null) {
            ModelErrorCard(state.modelError)
        }
        if (state.loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = chatListState,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (oldestFirstMessages.isEmpty()) {
                item {
                    ChatBubble(
                        text = "Send me a meeting note, standup update, blocker, or deadline. I will read the chat, extract tasks, and keep your timetable updated.",
                        fromUser = false
                    )
                }
            } else {
                items(oldestFirstMessages, key = { it.id }) { meeting ->
                    ChatThread(meeting)
                }
            }

            if (state.loading) {
                item {
                    ChatBubble(
                        text = "I am analyzing the chat for tasks, priority, dependencies, deadlines, and the best time to do each item...",
                        fromUser = false
                    )
                }
            }

            item {
                TimetableCard(state.timetable)
            }

            item {
                EditableTasksCardHeader(openTasks.size)
            }

            if (openTasks.isEmpty()) {
                item {
                    EmptyTasksCard()
                }
            } else {
                items(openTasks, key = { it.id }) { task ->
                    EditableTaskCard(
                        task = task,
                        onUpdateTask = onUpdateTask
                    )
                }
            }
        }

        ChatComposer(
            input = chatInput,
            loading = state.loading,
            onInputChange = { chatInput = it },
            onSubmit = {
                onSubmitChatInput(chatInput)
                chatInput = ""
            },
            onClearAllData = { showClearConfirmation = true }
        )
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("Clear assistant data?") },
            text = { Text("This removes goals, tasks, meetings, and memories from this device. The local model stays installed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirmation = false
                        chatInput = ""
                        onClearAllData()
                    }
                ) {
                    Text("Clear data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ChatHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("GenieX", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Your PA chat. Every message can update the timetable.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ChatThread(meeting: MeetingEntity) {
    val assistantText = meeting.assistantReply.ifBlank { meeting.summary }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (meeting.transcript.isNotBlank()) {
            ChatBubble(text = meeting.transcript, fromUser = true)
        }
        if (assistantText.isNotBlank()) {
            ChatBubble(text = assistantText, fromUser = false)
        }
    }
}

@Composable
private fun ChatBubble(
    text: String,
    fromUser: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (fromUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.84f),
            color = if (fromUser) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (fromUser) 20.dp else 4.dp,
                bottomEnd = if (fromUser) 4.dp else 20.dp
            )
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                color = if (fromUser) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ChatComposer(
    input: String,
    loading: Boolean,
    onInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onClearAllData: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 3.dp,
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = onInputChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message GenieX") },
                    minLines = 1,
                    maxLines = 4,
                    enabled = !loading,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
                Button(
                    onClick = onSubmit,
                    enabled = !loading && input.isNotBlank()
                ) {
                    Text("Send")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onClearAllData,
                    enabled = !loading
                ) {
                    Text("Clear all data")
                }
            }
        }
    }
}

@Composable
private fun ModelErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TimetableCard(timetable: List<String>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Priority timetable", style = MaterialTheme.typography.titleMedium)
            if (timetable.isEmpty()) {
                Text(
                    "Send a chat and I will arrange the work by importance, urgency, blockers, and the best part of the day to do it.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                timetable.forEachIndexed { index, item ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = "${index + 1}. $item",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditableTasksCardHeader(taskCount: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Edit tasks", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            if (taskCount == 0) {
                "No open tasks yet."
            } else {
                "Correct the assistant when needed. Changes immediately update the timetable."
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyTasksCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Send a natural message above, and I will create editable tasks here.",
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EditableTaskCard(
    task: TaskEntity,
    onUpdateTask: (TaskEntity) -> Unit
) {
    var title by rememberSaveable(task.id, task.updatedAtEpochMs) { mutableStateOf(task.title) }
    var details by rememberSaveable(task.id, task.updatedAtEpochMs) { mutableStateOf(task.details) }
    var priority by rememberSaveable(task.id, task.updatedAtEpochMs) { mutableStateOf(task.priority.toString()) }
    var deadline by rememberSaveable(task.id, task.updatedAtEpochMs) {
        mutableStateOf(task.deadlineEpochDay?.let { LocalDate.ofEpochDay(it).toString() }.orEmpty())
    }
    var blockedReason by rememberSaveable(task.id, task.updatedAtEpochMs) { mutableStateOf(task.blockedReason.orEmpty()) }
    var statusName by rememberSaveable(task.id, task.updatedAtEpochMs) { mutableStateOf(task.status.name) }

    val status = runCatching { TaskStatus.valueOf(statusName) }.getOrDefault(TaskStatus.PENDING)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Task", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Title") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
            OutlinedTextField(
                value = details,
                onValueChange = { details = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Context / why it matters") },
                minLines = 2,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = priority,
                    onValueChange = { priority = it.filter(Char::isDigit).take(2) },
                    modifier = Modifier.weight(1f),
                    label = { Text("Priority 1-10") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it.take(10) },
                    modifier = Modifier.weight(2f),
                    label = { Text("Deadline YYYY-MM-DD") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
            }
            OutlinedTextField(
                value = blockedReason,
                onValueChange = { blockedReason = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Dependency or blocker") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusButton("Pending", status == TaskStatus.PENDING) { statusName = TaskStatus.PENDING.name }
                StatusButton("Doing", status == TaskStatus.IN_PROGRESS) { statusName = TaskStatus.IN_PROGRESS.name }
                StatusButton("Blocked", status == TaskStatus.BLOCKED) { statusName = TaskStatus.BLOCKED.name }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        onUpdateTask(
                            task.copy(
                                title = title.trim().ifBlank { task.title },
                                details = details.trim(),
                                priority = priority.toIntOrNull()?.coerceIn(1, 10) ?: task.priority,
                                deadlineEpochDay = parseDeadlineEpoch(deadline, task.deadlineEpochDay),
                                blockedReason = blockedReason.trim().ifBlank { null },
                                status = status
                            )
                        )
                    }
                ) {
                    Text("Save changes")
                }
                OutlinedButton(
                    onClick = { onUpdateTask(task.copy(status = TaskStatus.COMPLETED)) }
                ) {
                    Text("Done")
                }
            }
        }
    }
}

@Composable
private fun StatusButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    if (selected) {
        Button(onClick = onClick) {
            Text(label)
        }
    } else {
        OutlinedButton(onClick = onClick) {
            Text(label)
        }
    }
}

private fun parseDeadlineEpoch(raw: String, currentValue: Long?): Long? {
    val trimmed = raw.trim()
    if (trimmed.isBlank()) return null
    return runCatching { LocalDate.parse(trimmed).toEpochDay() }.getOrDefault(currentValue)
}
