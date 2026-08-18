# Prompts Used

These pages preserve the prompts used to generate the skill-assisted examples:

- [Python](python.md)
- [Scala 3](scala.md)
- [Kotlin](kotlin.md)

The absolute local filesystem paths inside the code blocks are retained because
they were part of the prompts that were run. For repository navigation, use the
[skill-assisted repository](https://github.com/rohinp/semantic-caching) and the
[comparison repository](https://github.com/rohinp/pragmatic-developer-skills/tree/main/example).

The prompts are not identical. The Scala prompt was the first version. Kotlin
added strict thresholds, non-vacuous traversal, fuller snapshots, and mutation
checks. Python retained those improvements and added stable extreme-value
floating-point requirements. The comparison reports treat these prompt changes
as a confounding variable rather than attributing every difference to skills.
