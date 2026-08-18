# Semantic caching in Scala 3

This deterministic, in-memory example stores LLM responses in a weighted graph.
It searches from recent nodes through a bounded number of strong neighbors and
reuses a response only when cosine similarity reaches the cache-hit threshold.
Embedding and LLM services are injected, so the example and tests need no
network access, credentials, or external infrastructure.

## Test

```bash
sbt test
```

## Run the demonstration

```bash
sbt run
```
