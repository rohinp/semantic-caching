from semantic_cache import (
    DeterministicEmbeddingService,
    RecordingLlmService,
    SemanticCache,
)


def main() -> None:
    embeddings = DeterministicEmbeddingService(
        {
            "How do I reset my password?": (1.0, 0.1, 0.0),
            "I forgot my password; how can I change it?": (0.99, 0.11, 0.0),
        }
    )
    llm = RecordingLlmService()
    cache = SemanticCache(
        embeddings,
        llm,
        edge_threshold=0.70,
        hit_threshold=0.95,
        search_starts=2,
        neighbors_per_start=2,
    )

    first = cache.get("How do I reset my password?")
    second = cache.get("I forgot my password; how can I change it?")
    print(f"First request (miss): {first}")
    print(f"Related request (semantic hit): {second}")
    print(f"LLM calls: {len(llm.calls)}")


if __name__ == "__main__":
    main()
