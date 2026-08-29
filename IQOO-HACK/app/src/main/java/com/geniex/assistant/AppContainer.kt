package com.geniex.assistant

import android.content.Context
import com.geniex.assistant.data.db.AppDatabase
import com.geniex.assistant.data.repo.AssistantRepository
import com.geniex.assistant.domain.AssistantCoordinator
import com.geniex.assistant.domain.BriefingEngine
import com.geniex.assistant.domain.ImportanceScorer
import com.geniex.assistant.domain.PlanningEngine
import com.geniex.assistant.domain.ProactiveEngine
import com.geniex.assistant.llm.GenieXQwenLocalBridge
import com.geniex.assistant.llm.ModelConfig

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database = AppDatabase.create(appContext)
    val defaultModelDirectoryPath: String = ModelConfig.defaultInternalModelDirectory(appContext)

    val repository = AssistantRepository(database)

    private val localModelBridge = GenieXQwenLocalBridge(
        modelDirectoryProvider = {
            repository.getSetting(ModelConfig.KEY_MODEL_DIRECTORY)
        }
    )

    val coordinator = AssistantCoordinator(
        repository = repository,
        planningEngine = PlanningEngine(),
        briefingEngine = BriefingEngine(ImportanceScorer()),
        proactiveEngine = ProactiveEngine(),
        localModelBridge = localModelBridge
    )
}
