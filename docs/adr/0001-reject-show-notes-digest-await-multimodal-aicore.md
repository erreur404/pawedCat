# ADR 0001: Reject Show Notes-Based AI Digest in Favor of Future Multimodal On-Device Processing

## Status

Accepted (Feature Rejected / Deferred)

## Context

We evaluated adding an automated "Daily/Nightly Podcast Digest" feature using on-device AI (such as Gemini Nano via Android AICore) scheduled during overnight device charging (`setRequiresCharging(true)`).

Current on-device AICore models (Gemini Nano on Tensor G3 / Pixel 8) operate strictly with text-only input and a compact token context window. Consequently, generating a digest today would require feeding raw RSS show notes and episode descriptions into the model, rather than the actual recorded audio.

## Decision

We reject generating podcast digests from RSS show notes and episode descriptions.

### Rationale:
1. **Description is not the content**: Show notes are marketing copy, sponsor placements, or boilerplate written by distributors. Authors frequently write clickbait, promotional blurbs, or vague descriptions that do not accurately represent what was actually discussed in the episode.
2. **False expectations & wasted user time**: Summarizing inaccurate or deceptive metadata produces misleading digests, defeating the purpose of saving the user's listening time.
3. **Spoiler prevention**: For narrative/storytelling podcasts, show notes can inadvertently spoil cliffhangers or plot points.

## Consequences

- We will not implement an LLM digest feature based on RSS show note text.
- We defer AI-powered episode digestion until on-device Android AICore hardware/models support direct multimodal raw audio stream ingestion (speech-to-concepts) efficiently on-device without heavy cloud dependencies or extreme battery degradation.
