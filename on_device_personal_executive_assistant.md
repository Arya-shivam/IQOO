# On-Device Personal Executive Assistant

## 1. Vision

Build a **private, proactive, on-device personal assistant** that
behaves more like a personal executive assistant than a chatbot.

The assistant continuously builds an understanding of the user's:

-   Goals
-   Tasks
-   Projects
-   Deadlines
-   Meetings
-   Commitments
-   Learning objectives
-   Decisions
-   Habits
-   Priorities
-   Past activities

It then uses this information to determine:

> **What is important, what needs attention, what can wait, and what the
> user should do next.**

### Example

The user says:

> "I registered for a hackathon on September 20th and I want to prepare
> for it."

The assistant should understand that this is a **goal with a deadline**,
break it into required activities, create a plan, monitor progress, and
continuously adjust the plan.

It might say:

> "You have 22 days before the hackathon. Today I'd recommend setting up
> the local LLM pipeline and spending one hour understanding llama.cpp.
> Tomorrow we can work on the memory system."

If the user misses a task:

> "You didn't complete yesterday's DSA session. I've moved Binary Search
> to today and shortened today's revision session. We still have enough
> time, but I'd avoid skipping another day."

------------------------------------------------------------------------

## 2. Core Principle

The LLM should **not be the memory**.

The LLM is the reasoning and communication layer.

The application owns the user's persistent state.

``` text
                  USER
                   │
                   ▼
          ┌─────────────────┐
          │ Personal Memory │
          │   & World Model │
          └────────┬────────┘
                   │
          Relevant Context
                   │
                   ▼
          ┌─────────────────┐
          │   Local LLM     │
          │   Qwen GGUF     │
          └────────┬────────┘
                   │
             Reasoning
                   │
                   ▼
          ┌─────────────────┐
          │ Decision Engine │
          └────────┬────────┘
                   │
                   ▼
              🤖 PA
```

This prevents the assistant from "forgetting" when the LLM context
window ends.

------------------------------------------------------------------------

## 3. Product Characteristics

The assistant should feel:

### Persistent

It remembers important information across days, weeks and months.

### Context-aware

It understands relationships between tasks, goals, deadlines and
projects.

### Proactive

It doesn't always wait for the user to ask.

### Goal-oriented

It understands why a task exists.

### Adaptive

Plans change when circumstances change.

### Personal

Recommendations become increasingly specific to the user's behaviour and
preferences.

### Private

Personal information, memories and AI inference remain on the device.

------------------------------------------------------------------------

## 4. Example User Journey

### 4.1 Creating a goal

User:

> "I want to learn DSA."

Assistant:

> "What's the goal? Interview preparation, competitive programming, or
> general knowledge?"

User:

> "Interview preparation. I have interviews starting in October."

The system creates:

``` text
Goal
├── Name: DSA Interview Preparation
├── Deadline: October
├── Type: Learning
└── Status: Active
```

------------------------------------------------------------------------

## 5. Goal Planning

The assistant should convert large goals into smaller objectives.

Example:

``` text
DSA Interview Preparation
│
├── Arrays
├── Strings
├── Hashing
├── Two Pointers
├── Sliding Window
├── Binary Search
├── Linked Lists
├── Stacks & Queues
├── Trees
├── Graphs
├── Dynamic Programming
└── Revision / Mock Interviews
```

The planning engine considers:

-   Deadline
-   Current knowledge
-   Available hours
-   Difficulty
-   Dependencies
-   Importance
-   Progress
-   Previous performance

------------------------------------------------------------------------

## 6. Dynamic Daily Planning

The assistant should not simply create a static calendar.

It should continuously recalculate the plan.

Example:

``` text
Goal:
DSA preparation

Deadline:
September 30

Available time:
2 hours/day

Current progress:
Arrays        ✓
Strings       ✓
Hashing       ✓
Binary Search 40%
Trees         0%
Graphs        0%
```

The assistant determines what should happen today.

### Morning

> "Today I'd focus on finishing Binary Search and solving five problems.
> Trees can wait until tomorrow."

### If the user finishes early

> "You finished today's target early. We can start Trees now, or use the
> remaining time for Binary Search problems. I'd recommend the latter
> because your accuracy is still low."

### If the user falls behind

> "You're two sessions behind. I've adjusted the schedule. We'll reduce
> revision time slightly rather than pushing everything forward."

------------------------------------------------------------------------

## 7. Understanding Importance

Importance should not be a simple user-entered value.

The system should calculate an importance score using multiple signals.

``` text
Importance =
    Goal relevance
  + Deadline urgency
  + Business impact
  + Dependencies
  + Consequence of delay
  + User-defined priority
  + Commitment level
  + Historical importance
```

Example:

``` text
Task A:
Prepare investor presentation

Deadline: Tomorrow
Impact: Very High
Dependencies: Client meeting

Task B:
Fix small UI alignment

Deadline: None
Impact: Low
```

The PA should conclude:

> "The presentation should come first."

------------------------------------------------------------------------

## 8. Understanding Dependencies

Tasks should be represented as relationships.

``` text
Get API credentials
        │
        ▼
Configure authentication
        │
        ▼
Test API
        │
        ▼
Client demo
```

If the first task is blocked:

> "The API integration is currently blocked because we're waiting for
> credentials from Raj. I'd follow up with him today."

This creates much more intelligent behaviour than a simple task list.

------------------------------------------------------------------------

## 9. Meeting Intelligence

One of the major features is **local meeting recording**.

The user starts:

> 🎙️ Start Meeting

The application records the meeting locally.

After the meeting:

``` text
Audio
 ↓
On-device Speech-to-Text
 ↓
Transcript
 ↓
Local LLM
 ↓
Structured extraction
```

The assistant extracts:

-   Decisions
-   Action items
-   Deadlines
-   Commitments
-   People
-   Responsibilities
-   Follow-ups
-   Risks
-   Dependencies
-   Important information

------------------------------------------------------------------------

## 10. Meeting Example

Meeting conversation:

> "Raj will send the API credentials by Tuesday. Once we receive them,
> we'll start integration. The client demo is Friday."

The assistant creates:

``` text
Task:
Raj → Send API credentials

Deadline:
Tuesday

Dependency:
API integration depends on credentials

Event:
Client demo

Deadline:
Friday
```

The assistant can then proactively act on this information.

Wednesday:

> "Raj's credentials haven't been received yet. They're blocking the
> integration, and the client demo is Friday. I'd recommend following up
> now."

------------------------------------------------------------------------

## 11. Structured Memory

Use multiple types of memory.

### 11.1 Long-Term Memory

Stable information.

Examples:

``` text
Goals
Preferences
Important people
Projects
Skills
Recurring habits
Long-term commitments
```

### 11.2 Episodic Memory

Things that happened.

Examples:

``` text
Meeting on August 29
Finished API integration
Spoke with client
Promised proposal
Started learning DSA
```

### 11.3 Structured State

Critical operational information.

``` text
Task
- id
- title
- status
- priority
- deadline
- project
- owner
- dependencies
- created_at
- updated_at
```

This should live in a database rather than inside LLM memory.

### 11.4 Semantic Memory

Embeddings can be generated for:

-   Notes
-   Meeting summaries
-   Conversations
-   Documents
-   Important events

This allows semantic retrieval.

Example:

User:

> "What did we discuss about the payment system?"

The system searches semantic memory and retrieves relevant past
information.

------------------------------------------------------------------------

## 12. Memory Consolidation

Memory should not become an infinite garbage dump.

Periodically:

``` text
Recent events
     ↓
Memory extraction
     ↓
Duplicate detection
     ↓
Importance evaluation
     ↓
Contradiction detection
     ↓
Long-term memory update
```

Example:

``` text
"I might learn Rust"
```

should not have the same importance as:

``` text
"I need to learn Rust for my project starting September 15."
```

------------------------------------------------------------------------

## 13. Memory Importance

Each memory can have an importance score.

``` text
10 → Critical / permanent
8  → Very important
6  → Useful long-term
4  → Temporary
2  → Low-value
0  → Discard
```

Critical information should remain indefinitely unless explicitly
deleted.

------------------------------------------------------------------------

## 14. Contradiction Handling

The assistant should detect conflicting information.

Example:

Old:

``` text
Hackathon:
September 20
```

New:

``` text
Hackathon:
September 27
```

The system should not blindly keep both.

It should identify the conflict and update the current state.

If uncertain:

> "I previously had the hackathon deadline as September 20th. You
> mentioned September 27th today. Should I update it?"

------------------------------------------------------------------------

## 15. Learning Recommendations

The assistant should understand not only **what the user wants to
learn**, but **why and by when**.

Example:

``` text
Goal:
Participate in hackathon

Deadline:
September 20

Current skills:
Java
Spring Boot

Missing skills:
Local LLM inference
Android
llama.cpp
Prompt/agent design
```

The assistant can create:

``` text
Day 1:
Understand llama.cpp

Day 2:
Run Qwen locally

Day 3:
Integrate Android JNI

Day 4:
Build memory system

Day 5:
Build agent tools

Day 6:
Build voice interaction
...
```

The recommendation should be based on the user's actual goals rather
than generic learning advice.

------------------------------------------------------------------------

## 16. Personal Skill Model

The application can maintain a lightweight skill graph.

``` text
User
│
├── Java          ██████████
├── Spring Boot   ████████
├── Android       ███
├── DSA           █████
├── AI            ████
└── llama.cpp     ██
```

The scores shouldn't be treated as absolute truth.

They are estimates based on:

-   User activity
-   Completed tasks
-   Problems encountered
-   Learning sessions
-   Self-assessment
-   Project history

------------------------------------------------------------------------

## 17. Proactive Intelligence

The PA should continuously evaluate events.

``` text
Event occurs
     ↓
Does it matter?
     ↓
Does it affect a goal?
     ↓
Does it require action?
     ↓
Is action required now?
     ↓
Should the user be interrupted?
```

This is critical.

The assistant should **not constantly annoy the user**.

------------------------------------------------------------------------

## 18. Interruptibility

The PA needs an interruption policy.

### High urgency

> "Your client meeting starts in 15 minutes."

Speak immediately.

### Medium urgency

> "You haven't followed up with Raj yet."

Show notification / mention during next interaction.

### Low urgency

> "You may want to learn Docker networking."

Save for daily briefing.

This makes the assistant feel more human.

------------------------------------------------------------------------

## 19. Daily Briefing

The assistant can proactively provide a morning briefing.

Example:

> **Good morning.**
>
> You have three important things today.
>
> First, finish the API integration because the client demo depends on
> it.
>
> Second, follow up with Raj about the credentials.
>
> You also planned to study DSA. I'd keep that to one hour today because
> the project deadline is more important.
>
> You have a free two-hour window from 3--5 PM. I'd use it for the
> integration work.

------------------------------------------------------------------------

## 20. End-of-Day Reflection

At the end of the day:

> "You completed 4 of 5 planned tasks today. The API integration is now
> complete. The only missed task was DSA practice. I've moved it to
> tomorrow."

This updates:

-   Progress
-   Plans
-   Memory
-   Skill estimates

------------------------------------------------------------------------

## 21. Voice-First Interaction

The assistant should feel conversational.

``` text
User:
"What should I do now?"

PA:
"You have an hour before your meeting.
The most important unfinished task is the API integration.
I'd spend the hour finishing the authentication tests."
```

The interaction should support:

-   Speech-to-text
-   Natural language
-   Text-to-speech
-   Interruptions
-   Follow-up questions

------------------------------------------------------------------------

## 22. On-Device Architecture

Everything should run locally.

``` text
                 Android Phone
┌─────────────────────────────────────┐
│                                     │
│          Android Application        │
│                                     │
│  ┌─────────────┐ ┌───────────────┐ │
│  │ Voice / UI  │ │  Notifications │ │
│  └──────┬──────┘ └───────┬───────┘ │
│         │                 │         │
│         └────────┬────────┘         │
│                  ↓                  │
│           Personal Assistant        │
│                  │                  │
│       ┌──────────┼──────────┐       │
│       ↓          ↓          ↓       │
│   Memory      Planner     Agent     │
│   Engine      Engine      Engine    │
│       │          │          │       │
│       └──────────┼──────────┘       │
│                  ↓                  │
│             Local Qwen              │
│                  ↓                  │
│             llama.cpp               │
│                  ↓                  │
│          CPU / GPU / NPU            │
│                                     │
│             SQLite                  │
│          Vector Storage             │
│                                     │
└─────────────────────────────────────┘
```

------------------------------------------------------------------------

## 23. Recommended Technology

### Android

-   Kotlin
-   Jetpack Compose
-   Android Room / SQLite
-   WorkManager
-   Android notifications
-   Android audio APIs

### Local LLM

Initial option:

``` text
Qwen GGUF
     ↓
llama.cpp
```

For compatible Snapdragon devices, investigate:

``` text
llama.cpp
    ↓
GGML Hexagon backend
    ↓
Snapdragon Hexagon HTP / NPU
```

This avoids requiring Ollama or Termux in the final product.

------------------------------------------------------------------------

## 24. Local Speech Pipeline

``` text
Microphone
    ↓
Speech-to-Text
    ↓
Transcript
    ↓
Memory Extraction
    ↓
SQLite / Vector Memory
```

Speech recognition should also be local wherever practical.

The same applies to text-to-speech.

------------------------------------------------------------------------

## 25. Agent Architecture

The LLM should have controlled tools.

Example:

``` text
read_task()
create_task()
update_task()
complete_task()

read_project()
create_project()

search_memory()
save_memory()

get_calendar()
get_upcoming_events()

create_plan()
update_plan()
```

The model should **request actions through tools** rather than directly
manipulating the database.

------------------------------------------------------------------------

## 26. Agent Loop

Example:

``` text
User:
"I registered for a hackathon on September 20.
I want to build an AI assistant."

        ↓

Extract information

        ↓

Create goal

        ↓

Determine deadline

        ↓

Inspect current skills

        ↓

Identify required capabilities

        ↓

Generate milestones

        ↓

Generate daily plan

        ↓

Store plan

        ↓

Monitor progress

        ↓

Continuously adjust
```

------------------------------------------------------------------------

## 27. Planning Engine vs LLM

Do not let the LLM handle everything.

### Application logic should handle:

-   Dates
-   Deadlines
-   Task status
-   Dependencies
-   Scheduling
-   Notifications
-   Persistence
-   Validation
-   Calculations

### LLM should handle:

-   Understanding natural language
-   Summarization
-   Reasoning
-   Explanation
-   Recommendation
-   Conversational interaction
-   Extracting meaning from meetings
-   Generating plans

This separation makes the system considerably more reliable.

------------------------------------------------------------------------

## 28. The "Consciousness" Effect

The application should never claim actual consciousness.

Instead, create the **experience of continuity**.

The user should feel:

> "This assistant knows what I'm working toward, remembers what happened
> yesterday, understands what's important today, and knows what I should
> probably do next."

That feeling comes from:

``` text
Persistent Memory
+
World Model
+
Time Awareness
+
Goals
+
Dependencies
+
History
+
Personal Preferences
+
Planning
+
Proactive Triggers
+
LLM Reasoning
```

Not from simply using a larger model.

------------------------------------------------------------------------

## 29. Personal World Model

The central data model can look like:

``` text
                 USER
                  │
        ┌─────────┼─────────┐
        ↓         ↓         ↓
      Goals    Projects   People
        │         │         │
        ↓         ↓         ↓
      Tasks ── Dependencies
        │
        ↓
    Deadlines
        │
        ↓
     Events
        │
        ↓
     Decisions
        │
        ↓
     Memories
```

This becomes the assistant's representation of the user's world.

------------------------------------------------------------------------

## 30. Example of True PA Behaviour

User:

> "I want to win this hackathon."

The assistant shouldn't simply answer:

> "Here are some tips."

Instead:

``` text
Goal created
     ↓
Hackathon deadline found
     ↓
Current project assessed
     ↓
Missing work identified
     ↓
Learning requirements identified
     ↓
Milestones created
     ↓
Daily plan created
     ↓
Progress monitored
     ↓
Plan dynamically adjusted
```

Then:

> **"You have 11 days left. The core AI pipeline is working, but the
> memory system isn't finished. I'd prioritize that today. Don't spend
> time polishing animations yet."**

That is the behaviour that makes it feel like an actual PA.

------------------------------------------------------------------------

## 31. MVP

Do not attempt the entire system during the hackathon.

Build one strong vertical slice.

### MVP flow

``` text
User creates goal
        ↓
"Prepare for hackathon"
        ↓
Assistant asks deadline
        ↓
Creates plan
        ↓
User records meeting
        ↓
Local transcription
        ↓
Extract commitments
        ↓
Update memory
        ↓
Deadline monitoring
        ↓
Morning briefing
        ↓
Proactive recommendation
```

This demonstrates the core idea end-to-end.

------------------------------------------------------------------------

## 32. Hackathon Demo

The strongest demonstration should be completely offline.

### Step 1

Create:

> "Hackathon on September 20."

### Step 2

Create several goals and tasks.

### Step 3

Record a short simulated meeting.

The assistant extracts:

``` text
Task → Finish prototype
Deadline → September 10
Dependency → Local model integration
```

### Step 4

Turn on airplane mode.

### Step 5

Ask:

> "What should I do now?"

The PA responds based entirely on local memory.

### Step 6

Ask:

> "What should I learn today?"

It generates a recommendation based on:

-   Current project
-   Skill gaps
-   Deadline
-   Remaining time

### Step 7

Miss a task.

Ask again.

The assistant adapts the plan.

------------------------------------------------------------------------

## 33. What Makes This Different

A normal AI assistant:

``` text
User → Question → AI → Answer
```

Your assistant:

``` text
User
 ↓
Life / Work Activity
 ↓
Persistent Personal World Model
 ↓
Continuous Reasoning
 ↓
Plan
 ↓
Monitor
 ↓
Adapt
 ↓
Proactively Communicate
```

The key product idea is:

> **The assistant doesn't just answer questions. It maintains an
> evolving understanding of what the user is trying to accomplish and
> continuously helps them get there.**

------------------------------------------------------------------------

## 34. Privacy Advantage

Because the system runs locally:

``` text
Meetings
Tasks
Notes
Goals
Personal conversations
Work information
Memories
AI inference
```

can remain on the phone.

This is particularly valuable for an executive-assistant use case where
sending every meeting and personal decision to a cloud AI service may be
undesirable.

------------------------------------------------------------------------

## 35. Long-Term Vision

The eventual system could become a true personal operating layer:

``` text
                  PERSONAL AI
                       │
       ┌───────────────┼────────────────┐
       ↓               ↓                ↓
   Work Life       Learning Life     Personal Life
       │               │                │
    Projects          Skills           Goals
    Meetings          Courses          Plans
    Tasks             Practice         Events
       │               │                │
       └───────────────┼────────────────┘
                       ↓
                Personal World Model
                       ↓
                 Local AI Agent
                       ↓
                 🤖 Personal PA
```

The long-term goal is not simply:

> **"AI that remembers my conversations."**

It is:

> **"AI that understands the trajectory of my life and work, remembers
> my commitments, recognizes what matters, and helps me decide what to
> do next."**
