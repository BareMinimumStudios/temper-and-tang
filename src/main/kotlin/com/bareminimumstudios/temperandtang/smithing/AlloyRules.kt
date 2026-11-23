package com.bareminimumstudios.temperandtang.smithing

data class AlloyBatch(val batches:Int,val additiveCost:Int,val outputCount:Int)
object AlloyRules {
    fun batch(primary:Int,additive:Int,primaryPer:Int,additivePer:Int,outputPer:Int,limit:Int=8):AlloyBatch? {
        if(primary !in 1..8 || additive !in 1..8 || primaryPer !in 1..8 || additivePer !in 1..8 || outputPer !in 1..8 || limit !in 1..8 || primary%primaryPer!=0)return null
        val batches=primary/primaryPer
        val cost=additivePer*batches;val output=outputPer*batches
        if(cost>additive || output !in 1..limit)return null
        return AlloyBatch(batches,cost,output)
    }
}
