package com.signsathi.data.model

/**
 * The visual + interaction state of a single lesson node on the path.
 */
sealed class NodeState {
    /** Not yet reachable — shown greyed out with a lock icon */
    object Locked : NodeState()

    /** The next lesson the user should attempt — shown in orange with sparkle icon */
    object Active : NodeState()

    /**
     * The specific node the user is currently at (most recently unlocked).
     * Rendered raised/elevated with a "Continue" tooltip bubble above it.
     */
    object ActiveContinue : NodeState()

    /** Already finished — shown in green with a checkmark */
    object Completed : NodeState()
}

/**
 * A single lesson node on the path tree.
 *
 * @param id          Unique identifier, used as the nav argument when launching the lesson
 * @param title       Short display name shown in the tooltip / lesson header
 * @param state       Current visual + interaction state
 * @param xpReward    XP awarded on first completion
 */
data class LessonNode(
    val id: String,
    val title: String,
    val state: NodeState,
    val xpReward: Int = 10
)

/**
 * A group of lesson nodes under a shared theme (e.g. "Unit 1 — Welcome!").
 *
 * @param id          Unique identifier for this unit
 * @param title       Unit name shown in the header card (e.g. "Unit 1")
 * @param description Short subtitle shown in the header card (e.g. "Welcome!")
 * @param nodes       Ordered list of lesson nodes belonging to this unit
 */
data class LessonUnit(
    val id: String,
    val title: String,
    val description: String,
    val nodes: List<LessonNode>
)

/**
 * Snapshot of the current user's learning progress, displayed in the top stats bar.
 *
 * @param xp            Total XP earned across all lessons
 * @param streakDays    Current consecutive-day streak
 * @param heartsLeft    Remaining hearts (lives) — max 5
 */
data class UserProgress(
    val xp: Int = 0,
    val streakDays: Int = 0,
    val heartsLeft: Int = 5
)