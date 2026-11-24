package com.bareminimumstudios.temperandtang.smithing
import kotlin.test.*
import kotlin.math.*
class WorkshopMotionTest {
    @Test fun `the hammer striking face lands on both anvil surfaces`() {
        for(surface in listOf(13.4f,14.4f,16.4f,14.51f,17.26f)) {
            val p=WorkshopMotion.hammerFace(surface,WorkshopMotion.hammerAngle(WorkshopMotion.IMPACT))
            assertEquals(8f,p.x,.001f);assertEquals(surface,p.y,.001f);assertEquals(9f,p.z,.001f)
            assertTrue(WorkshopMotion.hammerFace(surface,WorkshopMotion.hammerAngle(0f)).y>surface+2)
        }
    }
    @Test fun `both side swings keep the handle aside and land on the same workpiece`() {
        for(surface in listOf(13.4f,14.4f,16.4f,14.51f,17.26f))for(side in listOf(-1,1)) {
            val grip=WorkshopMotion.sideHammerGrip(surface,side)
            assertTrue((8f-grip.x)*side>3f)
            val face=WorkshopMotion.sideHammerFace(surface,WorkshopMotion.hammerAngle(WorkshopMotion.IMPACT),side)
            assertEquals(8f,face.x,.001f);assertEquals(surface,face.y,.001f);assertEquals(9f,face.z,.001f)
            assertTrue(WorkshopMotion.sideHammerFace(surface,WorkshopMotion.hammerAngle(0f),side).y>surface+2f)
        }
    }
    @Test fun `the tong jaws stay with the workpiece throughout dipping`() {
        for(i in 27..92) {
            val u=i/100f;val g=WorkshopMotion.tongGrip(u);val p=WorkshopMotion.quenchPiece(u);val a=g.angle*PI.toFloat()/180
            assertEquals(p.x,g.point.x,.001f)
            assertEquals(p.y,g.point.y+WorkshopMotion.TONG_JAW_Y*cos(a)-WorkshopMotion.TONG_JAW_Z*sin(a),.001f)
            assertEquals(p.z,g.point.z+WorkshopMotion.TONG_JAW_Y*sin(a)+WorkshopMotion.TONG_JAW_Z*cos(a),.001f)
        }
        assertTrue(WorkshopMotion.quenchPiece(.65f).y<14.4f)
        assertEquals(16.35f,WorkshopMotion.quenchPiece(1f).y)
    }
}
