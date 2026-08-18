# Kotlin Comparison

This report compares:

- [Skill-assisted Kotlin project](https://github.com/rohinp/semantic-caching/tree/main/semantic-caching-kotlin)
- [Comparison Kotlin project](https://github.com/rohinp/pragmatic-developer-skills/tree/main/example/semantic-caching-kotlin)
- [Prompt used for the skill-assisted project](prompts/kotlin.md)

## Observed Results

The skill-assisted project passed nine tests and the comparison project passed
eight during the original review.

| Area | Comparison project | Skill-assisted project | Assessment |
| --- | --- | --- | --- |
| Production size | 216 lines in one file | 179 core lines plus a 29-line demo | Similar total size; skill-assisted concerns are separated. |
| Test dependencies | `kotlin.test` and Kotest property testing | `kotlin.test` only | Skill-assisted project followed the dependency constraint. |
| Compiler settings | Warnings as errors | Warnings as errors | Equivalent compiler evidence. |
| Embedding model | Public list alias validated during cosine calls | Validated immutable domain object | Skill-assisted construction is stronger. |
| Extreme finite values | Invalid result rejected by a similarity constructor | Accepted inputs can produce `NaN` | Skill-assisted implementation has a remaining numeric defect. |
| Threshold contracts | Reversed rejected; equality permitted | Equal, reversed, out-of-range, and `NaN` rejected | Skill-assisted project matches strict separation. |
| Graph invariants | Runtime checker plus generated cases | Snapshot assertions over one graph | Comparison explores more states. |
| Atomic failure | Node count and production invariant report | Complete snapshot, existing edges, and LLM calls | Skill-assisted test is stronger. |
| Traversal bound | Three nodes, bound four | Five nodes, bound three, with followed-neighbor checks | Only skill-assisted test is non-vacuous. |
| Exhaustive sequences | 121 through length four | 364 through length five | Skill-assisted finite model is larger. |
| Exhaustive assertions | Total call count | Ordered identities, count, and node count | Skill-assisted assertions are stronger. |
| Representative defects | No retained evidence | Traversal, premature mutation/LLM, and reverse-edge defects seeded | Skill-assisted tests demonstrated targeted sensitivity. |
| Runnable example | No application task | Configured deterministic `run` task | Skill-assisted project meets the requirement. |

## Main Findings

### Traversal-bound test is meaningful

The skill-assisted fixture creates five nodes against a bound of three, confirms
that a recent start has more reachable neighbors than may be followed, and
requires at least one neighbor visit. Removing neighbor truncation caused the
test to fail during generation. The comparison fixture has only three nodes
against a bound of four, so visiting every node still passes.

### Atomicity evidence is stronger

The skill-assisted test begins with existing edges, snapshots the complete graph,
submits an incompatible embedding, and checks graph and LLM history. A temporary
premature-mutation defect was rejected. The comparison assertion covers node
count and a production-owned invariant report, not all observable state.

### Raw graph invariant coverage has a blind spot

The skill-assisted snapshot contains canonical edges and raw adjacency, but its
test iterates canonical edges first. A self-loop or isolated one-way edge stored
only from a higher ID to a lower ID can be omitted from that projection. The test
should iterate every raw adjacency entry directly, as the Python test now does.

The generation-time global reverse-edge mutation was detected, but it does not
cover every localized asymmetric state.

### Extreme finite values can produce `NaN`

The immutable embedding rejects non-finite components but accepts
`Double.MAX_VALUE`. Squared magnitudes overflow and the cosine expression can
become `Infinity / Infinity`; coercion does not turn `NaN` into a valid bounded
similarity. Tests use ordinary finite values and do not expose this. A scaled
norm calculation or documented safe component range is required.

### Generated exploration remains broader in the comparison project

Kotest Property samples compatible vectors and graph inputs. The skill-assisted
prompt prohibited adding that dependency without approval, so its checks use
focused examples. This is a deliberate dependency-versus-coverage tradeoff, not
an unconditional improvement.

## How the Prompt Affected the Result

The following outcomes map directly to explicit prompt wording:

- strict threshold separation;
- a traversal fixture larger than the bound;
- complete graph and LLM atomicity snapshots;
- exactly 364 identity-sensitive sequences;
- representative defects for traversal and partial mutation;
- no additional property-testing dependency; and
- a configured runnable application.

The prompt did not mention stable arithmetic for extreme finite components, and
that case remained defective. Prompt effects are therefore clearer than a broad
claim that skill invocation guarantees correctness.

## Skill Evidence

The retained session showed that the run read both skills and required
references, classified vector, threshold, traversal, atomicity, invariant, and
sequence risks, chose proportionate techniques, ran the full suite and demo,
temporarily seeded and restored three defects, and used bounded verification
language. This demonstrates workflow compliance, not causal proof.

## Run

Skill-assisted project:

```bash
cd semantic-caching-kotlin
./gradlew test --rerun-tasks
./gradlew run
```

Comparison setup and source are at the
[absolute GitHub location](https://github.com/rohinp/pragmatic-developer-skills/tree/main/example/semantic-caching-kotlin).

## Conclusion

The skill-assisted Kotlin project is stronger on construction contracts,
atomicity, traversal evidence, sequence checking, and mutation-tested assertions.
The comparison project is stronger in broad generated exploration. The
skill-assisted numeric defect and raw-adjacency test blind spot remain important
limitations.
