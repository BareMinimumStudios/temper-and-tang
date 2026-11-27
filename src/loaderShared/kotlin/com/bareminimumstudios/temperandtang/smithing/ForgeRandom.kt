package com.bareminimumstudios.temperandtang.smithing

import java.security.SecureRandom

/** Outcome rolls are server-owned and independent of the world seed or client-provided timing scores. */
object ForgeRandom {
    private val random=SecureRandom()
    fun nextInt(bound:Int)=random.nextInt(bound)
    fun nextDouble()=random.nextDouble()
}
