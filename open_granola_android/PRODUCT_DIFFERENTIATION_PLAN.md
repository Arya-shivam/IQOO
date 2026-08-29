# pa Product Differentiation Plan

## Product thesis

The strongest differentiator for `pa` is not private meeting summaries or local AI. Those capabilities are useful, but they are increasingly expected.

`pa` should become the first private **intent-to-reality assistant**:

> It remembers what you said matters, observes where your attention actually went, and helps close the gap—entirely on your device.

## The market gap

| Product category | What it remembers | Primary outcome |
| --- | --- | --- |
| Granola | Meetings and notes | Better meeting recall |
| Limitless | Spoken conversations | Searchable life memory |
| Gemini and Apple Intelligence | Data across their ecosystems | General assistance |
| `pa` | Commitments plus actual device behavior | Follow-through |

Granola already supports querying across meetings and extracting actions, so meeting memory alone is not unique. See the [Granola documentation](https://docs.granola.ai/help-center/getting-more-from-your-notes/chatting-with-your-meetings).

Limitless positions itself as an all-day conversation memory. See the [Limitless documentation](https://help.limitless.ai/en/articles/9124757-pendant-faq).

Apple and Google are both building assistants around personal context across apps. See [Apple Intelligence](https://developer.apple.com/apple-intelligence/) and [Gemini Personal Intelligence](https://support.google.com/gemini/answer/16598406).

Therefore, `pa` should avoid competing as another summarizer, memory store, or generic chatbot.

## The differentiating concept: Intent–Reality Loop

`pa` connects four things competitors generally treat separately:

1. What the user committed to in meetings.
2. What the user planned to accomplish.
3. What notifications demanded the user's attention.
4. Where phone usage shows the user's attention actually went.

For example:

> You committed to sending the prototype after yesterday's meeting. It is still incomplete, and communication apps consumed 94 minutes this morning. Would you like a 30-minute focus plan?

That is substantially more valuable than:

> Here is yesterday's meeting summary.

The product promise becomes:

> `pa` knows what you promised, what distracted you, and what deserves attention next.

## Signature experiences

### 1. Commitment ledger

Automatically extract promises, deadlines, decisions, and dependencies from meeting summaries.

Every commitment should include:

- Source meeting
- Exact supporting transcript
- Owner
- Deadline
- Confidence
- Current status

This creates trust and makes hallucinations inspectable.

### 2. Attention drift detection

Compare active plans against app usage and notification patterns.

Examples:

- "Your priority was interview preparation, but it received no focused time today."
- "Messaging interruptions peaked after 2 PM."
- "Three meetings created seven tasks, but your available time only supports four."

This experience should be reflective, never judgmental.

### 3. Daily private briefing

When the user opens `pa`, show something actionable rather than a generic positive quote:

> Good morning, Arya. Two commitments are due today. Your first meeting starts at 11:00, and yesterday your best focus window was 9:20–10:40.

Every claim should be derived locally and linked to visible sources.

### 4. End-of-day closure

Produce a short private review containing:

- What moved forward
- What remained unfinished
- What consumed attention
- Which commitments changed
- What tomorrow should protect

Over time, `pa` becomes a personal decision journal instead of a chat history.

### 5. Intervention intelligence

The real moat is learning when to help.

`pa` should learn locally:

- When the user normally focuses
- Which notifications matter
- Which apps correlate with unfinished plans
- Which nudges are useful or ignored
- How much work the user realistically completes

The assistant should sometimes decide not to interrupt.

## Defensibility

The durable asset should be a local temporal knowledge graph:

```text
Meeting → Commitment → Plan → Device activity → Outcome → Learned preference
```

Each item should remain:

- Source-linked
- Editable
- Deletable
- Confidence-scored
- Stored locally
- Excluded from context when disabled

The advantage is not having more raw data. It is understanding the relationship between intention, attention, and outcome.

## Initial audience

Start with meeting-heavy people who struggle with fragmented attention:

- Founders
- Product managers
- Consultants
- Students handling projects and classes
- People with executive-function or attention-management challenges

Do not initially market `pa` as AI for everyone.

## Product roadmap

### Phase 1: Close the loop

- Extract commitments from meetings
- Connect commitments to plans
- Detect overdue and conflicting commitments
- Generate morning and evening briefings
- Show evidence for every insight

### Phase 2: Attention intelligence

- Compare plans with app usage
- Classify notifications by relevance
- Learn focus windows
- Detect interruption patterns
- Let users rate whether an insight was useful

### Phase 3: Safe actions

With user confirmation, allow `pa` to:

- Create calendar focus blocks
- Draft follow-up messages
- Silence low-value notifications temporarily
- Convert commitments into reminders
- Prepare the next meeting from previous decisions

### Phase 4: Personal operating layer

- Proactive but controlled assistance
- Local multimodal memory
- Multiple-device synchronization using end-to-end encryption
- User-selectable local models
- An exportable personal knowledge graph

## What not to compete on

Avoid making these the central pitch:

- "We summarize meetings."
- "We use an LLM."
- "You can chat with your notes."
- "Your data is private."
- "We have CPU, GPU, and NPU selection."

These are capabilities, not the product's identity.

## Positioning statement

> **`pa` is a private personal execution system that connects what you intended to do with how you actually spent your attention.**
