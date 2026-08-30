package com.opengranola.android.data

import androidx.room.withTransaction
import java.util.Calendar

class DemoDataSeeder(private val database: OpenGranolaDatabase) {
    suspend fun load(now: Long = System.currentTimeMillis()) = database.withTransaction {
        clearInsideTransaction()
        val bundle = buildBundle(now)
        check(bundle.nodes.map { it.id }.toSet().size == bundle.nodes.size) { "Duplicate demo graph node IDs" }
        check(bundle.edges.all { it.fromId in bundle.nodeIds && it.toId in bundle.nodeIds }) { "Demo graph contains a dangling edge" }

        database.assistantDao().apply {
            saveMemories(bundle.memories)
            saveGoals(bundle.goals)
            savePlans(bundle.plans)
            saveTasks(bundle.tasks)
            saveActions(bundle.actions)
            saveEvents(bundle.events)
            saveCommitments(bundle.commitments)
            saveGraphNodes(bundle.nodes)
            saveEdges(bundle.edges)
        }
        bundle.meetings.forEach { database.meetingDao().save(it) }
        bundle.notifications.forEach { database.notificationDao().save(it) }
    }

    suspend fun clear() = database.withTransaction { clearInsideTransaction() }

    private suspend fun clearInsideTransaction() {
        database.assistantDao().deleteDemoData()
        database.meetingDao().deleteDemo()
        database.notificationDao().deleteDemo()
    }

    private fun buildBundle(now: Long): DemoBundle {
        val nodes = mutableListOf<GraphNodeEntity>()
        val edges = mutableListOf<GraphEdgeEntity>()
        fun node(id: String, type: String, title: String, details: String, tags: String, status: String = "active", updatedAt: Long = now) =
            GraphNodeEntity(id, type, title, details, tags, status, id, updatedAt, updatedAt).also(nodes::add)
        fun edge(from: String, to: String, type: String, evidence: String, confidence: Float = 1f) =
            GraphEdgeEntity("demo:edge:${edges.size}", nodes.first { it.id == from }.type, from, nodes.first { it.id == to }.type, to, type, confidence, evidence, now).also(edges::add)

        node("demo:person", "person", "Aarav Mehta — developer showcase", "Early-career developer balancing an interior-systems internship, three freelance clients, AI engineering, DSA, courses, events and a long daily commute.", "developer,intern,freelancer,learner")

        val goalSpecs = listOf(
            Triple("demo:goal:internship", "Ship reliable parametric interior builders", "Deliver Java-based kitchen, cabinet, ceiling and panelling generators with validated dimensions, cutlists and editable 3D previews."),
            Triple("demo:goal:freelance", "Deliver freelance client releases", "Ship production React, NestJS and Node.js work without letting client deadlines collide with internship responsibilities."),
            Triple("demo:goal:ai", "Become production-ready in AI engineering", "Learn LLM APIs, embeddings, RAG, evaluation, agents and local inference by building small measurable projects."),
            Triple("demo:goal:dsa", "Build interview-level DSA fluency", "Complete a consistent problem-solving cycle across arrays, trees, graphs, dynamic programming and revision."),
            Triple("demo:goal:hackathons", "Compete in relevant hackathons", "Register selectively, form a team, submit functioning prototypes and avoid speculative event overload."),
            Triple("demo:goal:courses", "Finish selected technical courses", "Complete the AI engineering and advanced Java modules already started before registering for more."),
            Triple("demo:goal:portfolio", "Publish a credible developer portfolio", "Turn internship systems, freelance outcomes and AI prototypes into sanitized case studies."),
            Triple("demo:goal:energy", "Protect energy around the commute", "Use the 10–11 AM and 7–9 PM commute windows intentionally while preserving sleep and focused work blocks.")
        )
        val goals = goalSpecs.mapIndexed { index, (id, title, description) ->
            node(id, "goal", title, description, when (index) { 0 -> "java,3d,internship"; 1 -> "react,nestjs,node,freelance"; else -> "learning,personal" })
            GoalEntity(id, title, description, "active", day(now, -45 + index, 9), day(now, -index, 18))
        }
        goals.forEach { edge("demo:person", it.id, "pursues", "Active commitment in the showcase profile") }

        val projects = listOf(
            node("demo:project:interior", "project", "Parametric Interior Builder Platform", "Java rules engine and 3D editor integration for kitchens, cabinets, ceiling panels, wall panelling, cutlists and editable geometry.", "java,threejs,parametric,3d,internship"),
            node("demo:client:northstar", "client", "Northstar Clinics", "Freelance operations dashboard: React frontend, NestJS API, PostgreSQL reporting and role-based access.", "react,nestjs,postgres,freelance"),
            node("demo:client:studioline", "client", "StudioLine Interiors", "Lead-capture and project showcase site with React, Node.js webhooks and image-heavy performance work.", "react,node,webhooks,freelance"),
            node("demo:client:campusloop", "client", "CampusLoop", "Small Node.js event-registration backend requiring idempotent webhooks and transactional email delivery.", "node,event,webhook,freelance"),
            node("demo:project:ai-lab", "project", "Personal AI Engineering Lab", "A compact RAG assistant with ingestion, retrieval evaluation, prompt tests and OpenRouter integration.", "ai,rag,llm,evaluation"),
            node("demo:project:dsa", "project", "DSA Practice System", "Topic queue, timed problems, error log and spaced revision for interviews.", "dsa,algorithms,interview")
        )
        edge("demo:project:interior", "demo:goal:internship", "contributes_to", "Primary internship deliverable")
        listOf("demo:client:northstar", "demo:client:studioline", "demo:client:campusloop").forEach { edge(it, "demo:goal:freelance", "contributes_to", "Active freelance account") }
        edge("demo:project:ai-lab", "demo:goal:ai", "contributes_to", "Practice project for AI engineering")
        edge("demo:project:dsa", "demo:goal:dsa", "contributes_to", "Tracks interview preparation")

        val skillSpecs = listOf(
            "java" to "Java 17 and rules-engine design", "react" to "React and TypeScript", "nestjs" to "NestJS APIs",
            "node" to "Node.js services", "threejs" to "Three.js geometry integration", "sql" to "PostgreSQL and SQL",
            "testing" to "JUnit and integration testing", "rag" to "RAG and embeddings", "evals" to "LLM evaluation",
            "dsa" to "Data structures and algorithms"
        )
        skillSpecs.forEach { (id, title) -> node("demo:skill:$id", "skill", title, "Actively used or being developed in the showcase history.", id) }
        listOf("java", "threejs", "testing").forEach { edge("demo:skill:$it", "demo:project:interior", "used_by", "Required by the parametric builder") }
        listOf("react", "nestjs", "node", "sql").forEach { skill ->
            listOf("demo:client:northstar", "demo:client:studioline", "demo:client:campusloop").forEach { client -> edge("demo:skill:$skill", client, "used_by", "Freelance implementation skill", .85f) }
        }
        listOf("rag", "evals").forEach { edge("demo:skill:$it", "demo:project:ai-lab", "used_by", "AI engineering learning target") }
        edge("demo:skill:dsa", "demo:project:dsa", "used_by", "Daily problem-solving practice")

        data class TaskSpec(val key: String, val title: String, val details: String, val deps: List<String> = emptyList(), val priority: Int = 2)
        data class PlanSpec(val key: String, val goal: String, val title: String, val objective: String, val tasks: List<TaskSpec>)
        val planSpecs = listOf(
            PlanSpec("parametric", "demo:goal:internship", "Release parametric kitchen builder v1", "Ship validated Java cabinet rules, cutlists and editable 3D output for the internship review.", listOf(
                TaskSpec("constraints", "Consolidate cabinet constraints", "Document base, wall and tall-unit dimensions, fillers, plinth, worktop and corner rules.", priority = 1),
                TaskSpec("dimensions", "Normalize Java dimension rules", "Move duplicated cabinet measurements into validated rule objects with millimetre units.", listOf("constraints"), 1),
                TaskSpec("metadata", "Map wall and opening metadata", "Resolve wall orientation, doors, windows and usable spans before placement.", listOf("constraints"), 1),
                TaskSpec("catalogue", "Obtain approved hardware catalogue", "External prerequisite: hinge clearances and drawer hardware dimensions are awaiting the design team.", priority = 1),
                TaskSpec("generator", "Implement cabinet sequence generator", "Generate deterministic cabinet runs while respecting openings, fillers and corner units.", listOf("dimensions", "metadata", "catalogue"), 1),
                TaskSpec("cutlist", "Generate manufacturing cutlist", "Derive panels, edge bands and quantities from generated cabinet geometry.", listOf("generator"), 2),
                TaskSpec("preview", "Integrate editable 3D preview", "Expose generated layout to the Three.js editor while preserving cabinet identity and metadata.", listOf("generator"), 2),
                TaskSpec("tests", "Add regression fixtures", "Cover narrow walls, L-shaped kitchens, openings, fillers and cutlist totals.", listOf("cutlist", "preview"), 2)
            )),
            PlanSpec("northstar", "demo:goal:freelance", "Northstar dashboard production release", "Complete the highest-risk freelance client release before the agreed review.", listOf(
                TaskSpec("contract", "Freeze reporting API contract", "Confirm filters, pagination and role visibility with the client.", priority = 1),
                TaskSpec("access", "Receive staging credentials", "External prerequisite: client DevOps must provision the staging database and deployment token.", priority = 1),
                TaskSpec("api", "Implement NestJS reporting endpoints", "Add DTO validation, authorization guards and PostgreSQL queries.", listOf("contract"), 1),
                TaskSpec("ui", "Connect React analytics screens", "Wire TanStack Query, empty states, filters and loading/error handling.", listOf("contract", "api"), 1),
                TaskSpec("qa", "Run role and export QA", "Verify admin, manager and viewer access plus CSV export edge cases.", listOf("ui", "access"), 2),
                TaskSpec("release", "Deploy and prepare handover", "Deploy staging, record walkthrough and prepare release notes.", listOf("qa"), 2)
            )),
            PlanSpec("ai", "demo:goal:ai", "Build evaluated personal RAG prototype", "Turn AI engineering study into a small project with measurable retrieval quality.", listOf(
                TaskSpec("dataset", "Create evaluation question set", "Write 30 questions with expected source documents and abstention cases.", priority = 1),
                TaskSpec("ingest", "Implement document ingestion", "Chunk markdown notes and preserve source metadata.", priority = 1),
                TaskSpec("retrieve", "Add embedding retrieval", "Return scored chunks with source IDs and configurable top-k.", listOf("ingest"), 1),
                TaskSpec("answer", "Add OpenRouter answer generation", "Answer only from retrieved evidence and cite source IDs.", listOf("retrieve"), 2),
                TaskSpec("evaluate", "Measure retrieval and answer quality", "Run the question set and record hit-rate, faithfulness and failure examples.", listOf("dataset", "answer"), 1),
                TaskSpec("writeup", "Publish learning write-up", "Explain architecture, metrics, mistakes and next experiment.", listOf("evaluate"), 3)
            )),
            PlanSpec("hackathon", "demo:goal:hackathons", "Submit ContextGraph buildathon entry", "Produce a focused graph-memory prototype without compromising client delivery.", listOf(
                TaskSpec("register", "Complete team registration", "Confirm team members, track and submission rules.", priority = 1),
                TaskSpec("scope", "Freeze one-day MVP scope", "Use local curation, graph retrieval and one frontier coaching workflow.", listOf("register"), 1),
                TaskSpec("prototype", "Build demo workflow", "Seed realistic context and demonstrate blocker-aware coaching.", listOf("scope"), 1),
                TaskSpec("video", "Record three-minute demo", "Show ingestion, graph, plan dependencies and a context-aware answer.", listOf("prototype"), 2),
                TaskSpec("submit", "Submit repository and video", "Check judging requirements and submit before the deadline.", listOf("video"), 1)
            )),
            PlanSpec("dsa", "demo:goal:dsa", "Four-week graphs and DP revision", "Build repeatable interview confidence through timed practice and error review.", listOf(
                TaskSpec("graphs", "Review graph traversal templates", "Re-implement BFS, DFS, topological sort and union-find from memory.", priority = 1),
                TaskSpec("graphset", "Solve twelve graph problems", "Mix shortest paths, components, cycle detection and dependency ordering.", listOf("graphs"), 1),
                TaskSpec("dp", "Build DP pattern sheet", "Capture state, transition and complexity for common one- and two-dimensional patterns.", listOf("graphs"), 2),
                TaskSpec("dpset", "Solve twelve DP problems", "Practice subsequence, knapsack, grid and interval patterns.", listOf("dp"), 2),
                TaskSpec("mock", "Complete timed mixed mock", "Solve two medium problems in 75 minutes and review mistakes.", listOf("graphset", "dpset"), 1)
            ))
        )
        val plans = mutableListOf<PlanEntity>()
        val tasks = mutableListOf<PlanTaskEntity>()
        planSpecs.forEachIndexed { planIndex, spec ->
            val planId = "demo:plan:${spec.key}"
            node(planId, "plan", spec.title, spec.objective, "plan,${spec.key}")
            plans += PlanEntity(planId, spec.title, spec.objective, "active", day(now, -12 + planIndex, 9), now)
            edge(planId, spec.goal, "implements", "This plan operationalizes the goal")
            spec.tasks.forEachIndexed { position, task ->
                val taskId = "demo:task:${spec.key}:${task.key}"
                val missing = task.key in setOf("catalogue", "access")
                node(taskId, if (missing) "prerequisite" else "task", task.title, task.details, "task,${spec.key}", if (missing) "missing" else if (position == 0) "done" else "todo")
                tasks += PlanTaskEntity(taskId, planId, task.title, task.details, if (missing) "blocked" else if (position == 0) "done" else "todo", task.priority, position)
                edge(planId, taskId, "contains", "Task belongs to ${spec.title}")
            }
            spec.tasks.forEach { task ->
                task.deps.forEach { dependency ->
                    edge("demo:task:${spec.key}:${task.key}", "demo:task:${spec.key}:$dependency", "requires", "Explicit prerequisite in the demo plan")
                }
            }
        }

        val factSpecs = listOf(
            "units" to "The interior rules engine uses millimetres end-to-end; conversion is allowed only at UI boundaries.",
            "base-cabinet" to "Current base cabinet defaults are 720 mm carcass height, 560 mm depth and a 100 mm plinth, with project-level overrides.",
            "identity" to "Every generated cabinet must keep a stable ID so edits survive 3D scene regeneration.",
            "cutlist" to "Cutlist output must include panel dimensions, quantity, material, grain direction and exposed-edge metadata.",
            "openings" to "Doors and windows are hard placement constraints; wall moulding and panelling builders consume the same opening metadata.",
            "intern-review" to "Internship review is next Friday; the reviewer expects Java tests plus a live editable 3D demonstration.",
            "northstar-stack" to "Northstar uses React 18, TypeScript, TanStack Query, NestJS, PostgreSQL and JWT role guards.",
            "northstar-deadline" to "Northstar staging review is Wednesday at 6 PM; credentials are still owned by client DevOps.",
            "studioline" to "StudioLine prioritizes Core Web Vitals, responsive project galleries and reliable lead webhooks over CMS flexibility.",
            "campusloop" to "CampusLoop registration webhooks must be idempotent because the payment provider retries deliveries.",
            "freelance-hours" to "Freelance work is capped at 12 focused hours per week to protect internship output and sleep.",
            "ai-course" to "The active AI engineering course covers embeddings, RAG, evaluation, agents and production observability.",
            "rag-rule" to "The personal RAG prototype must cite source IDs and abstain when retrieved evidence is insufficient.",
            "eval-target" to "Initial RAG target: at least 80% source hit-rate across 30 hand-written evaluation questions.",
            "dsa-log" to "Every failed DSA problem is logged with the missed pattern, incorrect assumption and next review date.",
            "dsa-window" to "DSA practice fits best from 8:15–8:55 AM before the first commute.",
            "commute" to "The daily outbound commute is 10–11 AM; return travel usually starts at 7 PM and ends before 9 PM.",
            "commute-use" to "Commute time is suitable for course video and review, but not coding or high-concentration problem solving.",
            "youtube" to "YouTube subscriptions are mostly Java, React, AI engineering, system design and DSA channels.",
            "video-rule" to "A learning video counts as progress only when it produces notes, code or a scheduled experiment.",
            "events" to "Event registration is limited to opportunities aligned with AI, developer tooling or the portfolio goal.",
            "sleep" to "Target sleep is 11:45 PM–7:30 AM; late freelance sessions correlate with skipped morning DSA.",
            "portfolio" to "Client names and private code must be anonymized before freelance work becomes a public case study.",
            "preference" to "The developer prefers short evidence-based coaching with one concrete next action, not generic motivation."
        )
        val memories = factSpecs.mapIndexed { index, (key, text) ->
            val id = "demo:fact:$key"
            node(id, "fact", text.substringBefore(';').take(90), text, "fact,${key.replace('-', ',')}", updatedAt = day(now, -(index % 12), 20))
            MemoryEntity(id, text, "demo", .95f - (index % 5) * .04f, key.replace('-', ','), day(now, -30 + index, 12), day(now, -(index % 12), 20))
        }
        listOf("units", "base-cabinet", "identity", "cutlist", "openings", "intern-review").forEach { edge("demo:fact:$it", "demo:project:interior", "about", "Technical internship knowledge") }
        listOf("northstar-stack", "northstar-deadline").forEach { edge("demo:fact:$it", "demo:client:northstar", "about", "Client delivery context") }
        edge("demo:fact:studioline", "demo:client:studioline", "about", "Client requirement")
        edge("demo:fact:campusloop", "demo:client:campusloop", "about", "Client requirement")
        listOf("ai-course", "rag-rule", "eval-target").forEach { edge("demo:fact:$it", "demo:project:ai-lab", "about", "AI learning context") }
        listOf("dsa-log", "dsa-window").forEach { edge("demo:fact:$it", "demo:project:dsa", "about", "DSA routine context") }

        data class ActionSpec(val goal: String, val source: String, val title: String, val summary: String, val tags: String, val skill: String)
        val actionSpecs = listOf(
            ActionSpec("demo:goal:internship", "github", "Aligned base-cabinet dimension constants", "Updated Java dimension rules and cutlist fixtures after finding a carcass-height mismatch.", "java,cabinet,cutlist", "java"),
            ActionSpec("demo:goal:internship", "github", "Added wall-opening collision fixture", "Covered a window overlapping a generated cabinet run and documented expected filler behavior.", "java,geometry,test", "testing"),
            ActionSpec("demo:goal:internship", "meeting", "Reviewed panelling builder metadata", "Mapped wall IDs, opening ranges and material metadata shared by panelling and ceiling builders.", "3d,metadata,internship", "threejs"),
            ActionSpec("demo:goal:freelance", "github", "Implemented Northstar report filters", "Added validated NestJS DTOs and PostgreSQL query conditions for date, clinic and owner filters.", "nestjs,postgres,client", "nestjs"),
            ActionSpec("demo:goal:freelance", "github", "Connected React analytics cards", "Added TanStack Query states, typed API responses and empty-state copy for the Northstar dashboard.", "react,typescript,client", "react"),
            ActionSpec("demo:goal:freelance", "email", "Sent StudioLine performance review", "Shared mobile image findings, proposed responsive image sizes and requested approval for lazy loading.", "react,performance,client", "react"),
            ActionSpec("demo:goal:freelance", "github", "Made CampusLoop webhook idempotent", "Stored provider event IDs before processing and returned success for known retries.", "node,webhook,client", "node"),
            ActionSpec("demo:goal:ai", "code", "Built markdown chunking experiment", "Compared heading-aware chunks against fixed windows and preserved document source IDs.", "rag,chunking,ai", "rag"),
            ActionSpec("demo:goal:ai", "notes", "Defined RAG evaluation questions", "Added expected sources and abstention cases for Java, client and learning documents.", "rag,evaluation,ai", "evals"),
            ActionSpec("demo:goal:ai", "youtube", "Practiced embedding retrieval", "Watched an embeddings lesson, wrote notes and reproduced cosine-similarity ranking locally.", "youtube,embeddings,ai", "rag"),
            ActionSpec("demo:goal:dsa", "practice", "Solved graph traversal set", "Completed BFS islands and course-schedule problems; logged a missed cycle-detection invariant.", "dsa,graphs,bfs", "dsa"),
            ActionSpec("demo:goal:dsa", "practice", "Revised dynamic programming states", "Re-solved house robber and grid paths without notes and recorded transition mistakes.", "dsa,dp,revision", "dsa"),
            ActionSpec("demo:goal:hackathons", "browser", "Registered for ContextGraph Buildathon", "Completed registration, selected developer-tools track and saved submission requirements.", "hackathon,event,registration", "ai"),
            ActionSpec("demo:goal:courses", "course", "Completed RAG evaluation module", "Finished quizzes and converted the examples into a checklist for the personal AI lab.", "course,rag,evaluation", "evals"),
            ActionSpec("demo:goal:portfolio", "notes", "Drafted parametric builder case study", "Outlined problem, constraints, Java architecture, 3D integration and measurable testing outcomes.", "portfolio,java,3d", "java")
        )
        val actions = (0 until 42).map { index ->
            val spec = actionSpecs[index % actionSpecs.size]
            val occurred = day(now, -(index / 3), listOf(8, 14, 21)[index % 3], 15 + (index * 7 % 35))
            val id = "demo:action:$index"
            node(id, "action", spec.title, "${spec.summary} Day ${14 - index / 3} of the showcase timeline.", spec.tags, "linked", occurred)
            edge(id, spec.goal, "contributes_to", spec.summary, .92f)
            val skillId = "demo:skill:${spec.skill}"
            if (nodes.any { it.id == skillId }) edge(id, skillId, "demonstrates", "Action used this skill", .85f)
            ActionEntity(id, spec.source, id, spec.source, spec.title, spec.summary, spec.tags, .7f + (index % 3) * .1f, "linked", occurred, occurred)
        }

        node("demo:routine:commute", "routine", "Twice-daily commute", "Outbound 10–11 AM and return 7–9 PM; audio/video review is possible but focused coding is not.", "commute,time,energy")
        edge("demo:routine:commute", "demo:goal:energy", "contributes_to", "Routine must be planned to protect energy", .8f)
        val events = mutableListOf<ContextEventEntity>()
        repeat(14) { offset ->
            listOf(10 to "Outbound commute", 19 to "Return commute").forEach { (hour, title) ->
                val id = "demo:event:commute:$offset:$hour"
                val timestamp = day(now, -offset, hour, if (hour == 19) 10 + offset % 4 * 10 else 5)
                val duration = if (hour == 19) "80–110 minutes, completed before 9 PM" else "50–60 minutes"
                node(id, "event", title, "$duration. Used for AI/Java video review, flashcards or rest; never counted as coding time.", "commute,routine", "complete", timestamp)
                edge(id, "demo:routine:commute", "instance_of", "Observed commute block")
                events += ContextEventEntity(id, "demo", "commute", title, duration, timestamp, .6f)
            }
        }
        val upcoming = listOf(
            Triple("buildathon", "ContextGraph Buildathon kickoff", "Team check-in, scope freeze and repository setup; registration confirmed."),
            Triple("java-course", "Advanced Java concurrency workshop", "Registered course session covering executors, structured concurrency and profiling."),
            Triple("ai-meetup", "Production RAG meetup", "Event registration confirmed; prepare two questions about evaluation and observability."),
            Triple("northstar-review", "Northstar staging review", "Client review depends on staging credentials, role QA and CSV export."),
            Triple("intern-demo", "Internship parametric builder review", "Demonstrate Java rules, cutlist output, editable 3D preview and regression fixtures."),
            Triple("dsa-contest", "Weekend DSA contest", "Optional timed checkpoint; skip if Northstar release remains blocked."),
            Triple("course-deadline", "AI engineering course milestone", "Finish evaluation module and submit RAG lab notes."),
            Triple("portfolio-review", "Portfolio case-study review", "Sanitize client details and review architecture diagrams.")
        )
        upcoming.forEachIndexed { index, (key, title, detail) ->
            val id = "demo:event:$key"
            val timestamp = day(now, 2 + index * 2, if (index % 2 == 0) 18 else 11, 0)
            node(id, "event", title, detail, "calendar,event,registered", "upcoming", timestamp)
            events += ContextEventEntity(id, "calendar", "calendar", title, detail, timestamp, .85f)
            val goal = when { key.contains("intern") -> "demo:goal:internship"; key.contains("northstar") -> "demo:goal:freelance"; key.contains("dsa") -> "demo:goal:dsa"; key.contains("course") || key.contains("ai-") -> "demo:goal:courses"; key.contains("portfolio") -> "demo:goal:portfolio"; else -> "demo:goal:hackathons" }
            edge(id, goal, "scheduled_for", "Upcoming registered event or deadline")
        }
        repeat(7) { offset ->
            events += ContextEventEntity(
                "demo:event:usage:$offset", "device", "usage", "Phone usage ${offset + 1} day(s) ago",
                "Total ${185 + offset * 8} minutes. YouTube ${42 + offset}m, Chrome ${31 + offset * 2}m, Slack 24m, GitHub 18m, LeetCode ${20 + offset}m, Maps 16m.",
                day(now, -offset, 23), .55f
            )
        }

        val patternSpecs = listOf(
            Triple("commute-fragmentation", "Commute fragments the evening", "Return travel ends between 8:20 and 8:55 PM, leaving one reliable low-energy block rather than a full coding session."),
            Triple("internship-momentum", "Internship work has the strongest momentum", "Java and parametric-builder actions appear on 10 of the last 14 days, with tests following implementation within 48 hours."),
            Triple("client-risk", "Northstar carries external deadline risk", "Frontend and API work are progressing, but staging QA still depends on credentials controlled by client DevOps."),
            Triple("dsa-sleep", "Late work predicts skipped DSA", "Morning DSA is consistent after nights ending before 11:45 PM and usually absent after late freelance sessions."),
            Triple("learning-transfer", "AI study transfers into code", "RAG videos and course modules produced notes, chunking code and evaluation questions instead of passive watch time."),
            Triple("event-load", "Event registrations are nearing capacity", "Three upcoming technical events overlap with a client review and internship demo; another registration would create a conflict."),
            Triple("context-switching", "Freelance context switching is concentrated", "Three clients create repeated React/NestJS/Node switches; batching Northstar work reduces restart cost."),
            Triple("youtube-signal", "YouTube is useful only with an artifact", "Coding and AI videos correlate with progress when followed by notes or code; unstructured watching clusters during tired commute periods.")
        )
        patternSpecs.forEach { (key, title, detail) -> node("demo:pattern:$key", "pattern", title, detail, "pattern,evidence", "observed") }
        edge("demo:pattern:commute-fragmentation", "demo:routine:commute", "inferred_from", "Fourteen days of commute events")
        edge("demo:pattern:commute-fragmentation", "demo:goal:energy", "affects", "Reduces evening deep-work capacity")
        edge("demo:pattern:internship-momentum", "demo:goal:internship", "describes", "Linked GitHub and meeting actions")
        edge("demo:pattern:client-risk", "demo:task:northstar:access", "inferred_from", "Staging credentials remain missing")
        edge("demo:pattern:client-risk", "demo:goal:freelance", "affects", "Blocks release QA")
        edge("demo:pattern:dsa-sleep", "demo:goal:dsa", "affects", "Morning practice depends on prior-night stopping time")
        edge("demo:pattern:dsa-sleep", "demo:fact:sleep", "inferred_from", "Recorded sleep preference and practice history")
        edge("demo:pattern:learning-transfer", "demo:goal:ai", "describes", "Course and YouTube actions generated artifacts")
        edge("demo:pattern:event-load", "demo:goal:hackathons", "conflicts_with", "Current registrations overlap delivery deadlines")
        edge("demo:pattern:event-load", "demo:goal:freelance", "affects", "Northstar review shares the same week")
        edge("demo:pattern:context-switching", "demo:goal:freelance", "affects", "Three active client stacks")
        edge("demo:pattern:youtube-signal", "demo:goal:ai", "describes", "Artifact-producing videos count as progress")

        val meetings = listOf(
            MeetingEntity("demo:meeting:intern-review", "Internship builder architecture review", day(now, -2, 15), "Discussed Java cabinet constraints, wall openings, cutlist fields, stable 3D IDs and missing hardware clearances.", "The rules engine should remain deterministic and use millimetres. Stable cabinet IDs are required for editor updates. Hardware clearances are blocked on the design catalogue."),
            MeetingEntity("demo:meeting:northstar", "Northstar client status call", day(now, -1, 18), "Reviewed reporting filters, role access, staging credentials, CSV exports and Wednesday review scope.", "API and React analytics are progressing. Client DevOps owns staging credentials. Role-based QA and export verification must finish before handover."),
            MeetingEntity("demo:meeting:hackathon", "Buildathon team scope session", day(now, -4, 21), "Agreed to demonstrate local curation, graph memory, dependency-aware plans and frontier coaching.", "Keep the submission to one end-to-end workflow. Seed realistic context, show missing prerequisites, and record a three-minute demo."),
            MeetingEntity("demo:meeting:mentor", "AI engineering mentor check-in", day(now, -6, 17), "Reviewed RAG learning plan, evaluation dataset, retrieval metrics and avoiding framework-first development.", "Create 30 questions before tuning retrieval. Measure source hit-rate and failure examples. Add agents only after retrieval quality is visible.")
        )
        meetings.forEach { meeting ->
            node(meeting.id, "meeting", meeting.title, meeting.notes, "meeting,summary", "complete", meeting.startedAt)
            val goal = when { "Northstar" in meeting.title -> "demo:goal:freelance"; "Buildathon" in meeting.title -> "demo:goal:hackathons"; "AI" in meeting.title -> "demo:goal:ai"; else -> "demo:goal:internship" }
            edge(meeting.id, goal, "informs", "Saved meeting summary")
        }
        val commitments = listOf(
            CommitmentEntity("demo:commitment:hardware", "demo:meeting:intern-review", meetings[0].title, "Request approved hardware clearances", "Aarav", "Before Thursday", "I will ask the design team for the approved hinge and drawer catalogue.", .96f, "open", now, now),
            CommitmentEntity("demo:commitment:fixtures", "demo:meeting:intern-review", meetings[0].title, "Add two opening-collision fixtures", "Aarav", "Before internship review", "Add window and door collision fixtures before the demo.", .94f, "done", now, now),
            CommitmentEntity("demo:commitment:credentials", "demo:meeting:northstar", meetings[1].title, "Provide staging credentials", "Client DevOps", "Monday", "DevOps will share database and deployment access by Monday.", .97f, "open", now, now),
            CommitmentEntity("demo:commitment:qa", "demo:meeting:northstar", meetings[1].title, "Complete role and CSV QA", "Aarav", "Wednesday 3 PM", "I will verify all three roles and CSV export before the review.", .97f, "open", now, now),
            CommitmentEntity("demo:commitment:scope", "demo:meeting:hackathon", meetings[2].title, "Freeze buildathon scope", "Aarav", "Tonight", "I will freeze the graph-memory workflow tonight.", .95f, "done", now, now),
            CommitmentEntity("demo:commitment:video", "demo:meeting:hackathon", meetings[2].title, "Record three-minute product demo", "Team", "Submission day", "We will record a three-minute end-to-end demo.", .93f, "open", now, now),
            CommitmentEntity("demo:commitment:dataset", "demo:meeting:mentor", meetings[3].title, "Write 30 RAG evaluation questions", "Aarav", "This weekend", "Create thirty questions before changing retrieval settings.", .98f, "open", now, now),
            CommitmentEntity("demo:commitment:metrics", "demo:meeting:mentor", meetings[3].title, "Record retrieval failure examples", "Aarav", "After first evaluation run", "Record hit-rate and specific failure examples.", .96f, "open", now, now)
        )
        commitments.forEach { commitment ->
            node(commitment.id, "commitment", commitment.title, "Owner: ${commitment.owner}; due: ${commitment.dueText}; evidence: ${commitment.evidence}", "commitment,meeting", commitment.status)
            edge(commitment.id, commitment.meetingId, "extracted_from", commitment.evidence, commitment.confidence)
        }

        val notificationSpecs = listOf(
            Triple("GitHub", "PR review requested: cabinet rule alignment", "Two comments on cutlist grain direction and base cabinet dimensions."),
            Triple("Slack", "Design team mentioned you", "Hardware catalogue still awaiting final approval; use provisional values only in tests."),
            Triple("Gmail", "Northstar staging review confirmed", "Wednesday 6:00 PM. Please include role matrix and CSV export demo."),
            Triple("GitHub", "CI passed on parametric-builder", "42 Java tests passed including wall-opening collision fixtures."),
            Triple("Calendar", "Return commute in 30 minutes", "Leave by 7:10 PM to arrive before 8:45 PM."),
            Triple("LeetCode", "Graph study plan reminder", "Course Schedule II and Network Delay Time are queued for review."),
            Triple("YouTube", "New video: Evaluating RAG systems", "Saved to AI Engineering playlist for the morning commute."),
            Triple("Devfolio", "ContextGraph Buildathon registration complete", "Team profile accepted; submission closes in nine days."),
            Triple("Coursera", "AI engineering module due", "Complete retrieval evaluation quiz before Sunday."),
            Triple("Gmail", "StudioLine image optimization approved", "Proceed with responsive variants and lazy loading."),
            Triple("Sentry", "Northstar staging API error", "Report export returned 403 for manager role in the latest test."),
            Triple("GitHub", "CampusLoop webhook PR merged", "Idempotency guard and retry tests merged to main."),
            Triple("Meetup", "Production RAG meetup confirmed", "Your seat is reserved; add questions before the event."),
            Triple("Notion", "Internship review checklist updated", "Java tests, cutlist sample and editable 3D preview are required."),
            Triple("Maps", "Evening commute: 86 minutes", "Moderate traffic; expected arrival 8:36 PM."),
            Triple("GitHub", "Portfolio case-study draft", "Anonymization checklist has three unresolved client references."),
            Triple("Discord", "Buildathon team check-in", "Scope call at 9:15 PM after commute."),
            Triple("Calendar", "DSA focus block", "Graphs revision tomorrow at 8:15 AM.")
        )
        val notifications = notificationSpecs.mapIndexed { index, (app, title, body) ->
            NotificationEntity("demo:notification:$index", "demo.${app.lowercase()}", app, title, body, now - index * 47L * 60 * 1000)
        }

        return DemoBundle(memories, goals, plans, tasks, actions, events, commitments, meetings, notifications, nodes, edges)
    }

    private fun day(now: Long, offset: Int, hour: Int, minute: Int = 0): Long = Calendar.getInstance().apply {
        timeInMillis = now
        add(Calendar.DAY_OF_YEAR, offset)
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private data class DemoBundle(
        val memories: List<MemoryEntity>, val goals: List<GoalEntity>, val plans: List<PlanEntity>,
        val tasks: List<PlanTaskEntity>, val actions: List<ActionEntity>, val events: List<ContextEventEntity>,
        val commitments: List<CommitmentEntity>, val meetings: List<MeetingEntity>, val notifications: List<NotificationEntity>,
        val nodes: List<GraphNodeEntity>, val edges: List<GraphEdgeEntity>
    ) { val nodeIds: Set<String> get() = nodes.mapTo(mutableSetOf()) { it.id } }
}
