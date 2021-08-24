package com.github.zimolab.jow.compiler.generator

import java.util.*

data class TypeArgument(
    val simpleName: String,
    val nullable: Boolean,
)

data class TypeArgumentTreeNode(
    var typeArgument: TypeArgument,
    var parent: TypeArgumentTreeNode? = null
) {
    var children = LinkedList<TypeArgumentTreeNode>()
    fun isRoot() = (parent == null)
    fun isLeaf() = (children.size == 0)
    fun getLevel(): Int {
        if (isRoot())
            return 0
        return parent!!.getLevel() + 1
    }

    fun addChild(typeArgument: TypeArgument): TypeArgumentTreeNode {
        val me = TypeArgumentTreeNode(typeArgument, this)
        this.children.add(me)
        return me
    }

    override fun toString(): String {
        return "${if (typeArgument.nullable) "Nullable" else ""}${typeArgument.simpleName}"
    }
}