package com.bareminimumstudios.temperandtang.smithing
import kotlin.test.*
import kotlin.math.*
class WorkshopMotionTest {
    @Test fun `the hammer striking face lands on both anvil surfaces`() {
        for(surface in listOf(13.4f,14.4f,16.4f)) {
            val p=WorkshopMotion.hammerFace(surface,WorkshopMotion.hammerAngle(WorkshopMotion.IMPACT))
            assertEquals(8f,p.x,.001f);assertEquals(surface,p.y,.001f);assertEquals(9f,p.z,.001f)
            assertTrue(WorkshopMotion.hammerFace(surface,WorkshopMotion.hammerAngle(0f)).y>surface+3)
        }
    }
    @Test fun `the tong jaws stay with the workpiece throughout dipping`() {
        for(i in 27..92) {
            val u=i/100f;val g=WorkshopMotion.tongGrip(u);val p=WorkshopMotion.quenchPiece(u);val a=g.angle*PI.toFloat()/180
            assertEquals(p.x,g.point.x,.001f)
            assertEquals(p.y+.15f,g.point.y-5.4f*sin(a),.001f)
            assertEquals(p.z,g.point.z+5.4f*cos(a),.001f)
        }
        assertTrue(WorkshopMotion.quenchPiece(.65f).y<9.8f)
        assertEquals(12.35f,WorkshopMotion.quenchPiece(1f).y)
    }
}
