package de.hartz.software.parannoying.offline.businesslogic.token

import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.offline.model.domain.messages.AbstractMessage

// Extension function for hex conversion
// fun String.toHex(): String = joinToString("") { "%02x".format(it) }

// Merkle Tree Node
data class MerkleNode(
    val hash: String,
    val left: MerkleNode? = null,
    val right: MerkleNode? = null
)

// Merkle Tree Class
class MerkleTree(val securityInterfaceHolder: SecurityInterfaceHolder, messages: List<AbstractMessage>, private val orderInvariant: Boolean = false) {
    private val leafMap = mutableMapOf<String, MerkleNode>()
    lateinit var root: MerkleNode
        private set

    init {
        require(messages.isNotEmpty()) { "Messages must not be empty" }
        val sorted = if (orderInvariant) messages.sortedBy { it.persistenceId } else messages
        val leaves = sorted.map { msg ->
            val hash = msg.metaData.previousToken
            val node = MerkleNode(hash)
            // TODO: hash is not unique like this..
            leafMap[hash] = node
            node
        }
        root = buildTree(leaves)
    }

    private fun buildTree(nodes: List<MerkleNode>): MerkleNode {
        if (nodes.size == 1) return nodes[0]

        val nextLevel = nodes.chunked(2).map { pair ->
            val left = pair[0]
            val right = if (pair.size == 2) pair[1] else left
            val combinedHash = hash(left.hash + right.hash)
            MerkleNode(combinedHash, left, right)
        }
        return buildTree(nextLevel)
    }

    // fun getRootHashHex(): String = root.hash.toHex()
    // fun getRootHashBase64(): String = Base64.getEncoder().encodeToString(root.hash)

    fun getProofFor(message: String): List<String>? {
        val leaf = leafMap[message] ?: return null
        return buildProofPath(leaf)
    }

    fun verifyInclusion(
        message: String,
        proof: List<String>,
        expectedRoot: String
    ): Boolean {
        var hash = hash(message.toString())
        for (siblingHash in proof) {
            hash = if (hash.contentEquals(siblingHash)) hash else hash(hash + siblingHash)
        }
        return hash.contentEquals(expectedRoot)
    }

    private fun buildProofPath(leaf: MerkleNode): List<String> {
        val proof = mutableListOf<String>()

        fun recurse(current: MerkleNode?, target: MerkleNode): Boolean {
            if (current == null) return false
            if (current.left == null && current.right == null) return current === target

            val inLeft = recurse(current.left, target)
            val inRight = recurse(current.right, target)

            if (inLeft && current.right != null) proof.add(current.right.hash)
            if (inRight && current.left != null) proof.add(current.left.hash)

            return inLeft || inRight
        }

        recurse(root, leaf)
        return proof
    }

    private fun hash(data: String): String {
        return securityInterfaceHolder.hashHelper.hash(data)
    // return MessageDigest.getInstance("SHA-256").digest(data)
    }
}

/*
// Sample Usage
fun main() {
    // TODO: Proper test
    // "one", "two", "three", "four", "five"
    val messages = listOf<AbstractMessage>()
    val tree = MerkleTree(SecurityInterfaceHolder(), messages)

    // println("Merkle Root (Hex): ${tree.getRootHashHex()}")
    // println("Merkle Root (Base64): ${tree.getRootHashBase64()}")

    val targetMsg = "three"
    val proof = tree.getProofFor(targetMsg)
    if (proof != null) {
        val result = tree.verifyInclusion(targetMsg, proof, tree.root.hash)
        println("Inclusion verified for '$targetMsg': $result")
    }
}
 */