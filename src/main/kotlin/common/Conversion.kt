package de.rki.jfn.common

import com.fasterxml.jackson.databind.node.IntNode

/** Converts an [Int] to an [IntNode] */
internal fun Int.toIntNode() = IntNode.valueOf(this)