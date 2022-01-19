package de.rki.jfn.common

import com.fasterxml.jackson.databind.node.BooleanNode

/** Converts a [Boolean] to a [BooleanNode] */
internal fun Boolean.toBooleanNode() = BooleanNode.valueOf(this)
