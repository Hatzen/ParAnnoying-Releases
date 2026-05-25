package de.hartz.software.parannoying.offline.model.domain.dialogs

/**
 * DummyUser which cannot be fully instantiated as we only now their onlineId.
 * Like it is the case with OnlineGroup members which are not known yet or got deleted.
 * NOTE: Not intended to be persisted.
 * // TODO: Maybe unify with UnknownUser. But actually these differ, online group unknown users will never be able to write a message..
 */
class DummyUser(
hash: String
): User("Unknown Dummy User", hash)