package com.bareminimumstudios.temperandtang.smithing

import kotlin.math.*

/** Pixel-space tool geometry shared by presentation and contact regression tests. */
object WorkshopMotion {
    const val IMPACT = 0.145f
    const val SWING = 0.36f
    const val HAMMER_SCALE = 0.4f
    const val HEAD_Z = 9f
    const val TONG_JAW_Z = 4.494708f
    const val TONG_JAW_Y = -.36f
    const val FACE_Y = -3f
    data class Point(val x:Float,val y:Float,val z:Float)
    data class Grip(val point:Point,val angle:Float)
    fun ease(t:Float):Float {val u=t.coerceIn(0f,1f);return u*u*u*(u*(u*6f-15f)+10f)}
    private fun mix(a:Float,b:Float,u:Float)=a+(b-a)*ease(u)
    fun hammerAngle(seconds:Float):Float=when {
        seconds<0.055f->mix(-42f,-63f,seconds/0.055f)
        seconds<IMPACT->{val u=((seconds-0.055f)/(IMPACT-0.055f)).coerceIn(0f,1f);-63f*(1-u*u)}
        seconds<0.21f->mix(0f,-18f,(seconds-IMPACT)/(0.21f-IMPACT))
        else->mix(-18f,-42f,(seconds-0.21f)/(SWING-0.21f))
    }
    fun hammerGrip(surface:Float)=Point(8f,surface-FACE_Y*HAMMER_SCALE,9f-HEAD_Z*HAMMER_SCALE)
    fun sideHammerGrip(surface:Float,side:Int=1)=Point(8f-side*HEAD_Z*HAMMER_SCALE,surface-FACE_Y*HAMMER_SCALE,9f)
    fun sideHammerFace(surface:Float,angle:Float,side:Int=1):Point {
        val a=angle*PI.toFloat()/180f;val g=sideHammerGrip(surface,side)
        return Point(g.x+side*HAMMER_SCALE*(FACE_Y*sin(a)+HEAD_Z*cos(a)),g.y+HAMMER_SCALE*(FACE_Y*cos(a)-HEAD_Z*sin(a)),g.z)
    }
    fun hammerFace(surface:Float,angle:Float):Point {
        val a=angle*PI.toFloat()/180;val g=hammerGrip(surface)
        return Point(g.x,g.y+HAMMER_SCALE*(FACE_Y*cos(a)-HEAD_Z*sin(a)),g.z+HAMMER_SCALE*(FACE_Y*sin(a)+HEAD_Z*cos(a)))
    }
    fun quenchPiece(u:Float):Point=when {
        u<0.26f->Point(12f,16.35f,8f)
        u<0.40f->{val t=(u-.26f)/.14f;Point(mix(12f,8f,t),mix(16.35f,17.5f,t),8f)}
        u<0.56f->Point(8f,mix(17.5f,11f,(u-.40f)/.16f),8f)
        u<0.75f->{val t=(u-.56f)/.19f;Point(8f,11f,8f+sin(t*PI.toFloat()*2)*sin(t*PI.toFloat()).pow(2)*.25f)}
        u<0.93f->{val t=(u-.75f)/.18f;Point(mix(8f,12f,t),mix(11f,16.35f,t),8f)}
        else->Point(12f,16.35f,8f)
    }
    fun tongGrip(u:Float):Grip {
        val rest=Point(2.15f,16.2f,2.6f)
        if(u<=0f || u>=1f)return Grip(rest,0f)
        val p=quenchPiece(u)
        val angle=when {u<.40f->0f;u<.56f->mix(0f,40f,(u-.40f)/.16f);u<.75f->40f;else->mix(40f,0f,(u-.75f)/.18f)}
        val a=angle*PI.toFloat()/180
        val held=Point(p.x,p.y-TONG_JAW_Y*cos(a)+TONG_JAW_Z*sin(a),p.z-TONG_JAW_Y*sin(a)-TONG_JAW_Z*cos(a))
        return when {
            u<.20f->{val t=ease(u/.20f);Grip(Point(rest.x+(held.x-rest.x)*t,rest.y+(held.y-rest.y)*t+sin(t*PI.toFloat())*2.5f,rest.z+(held.z-rest.z)*t),0f)}
            u>.93f->{val t=ease((u-.93f)/.07f);Grip(Point(held.x+(rest.x-held.x)*t,held.y+(rest.y-held.y)*t+sin(t*PI.toFloat())*1.2f,held.z+(rest.z-held.z)*t),angle)}
            else->Grip(held,angle)
        }
    }
}
