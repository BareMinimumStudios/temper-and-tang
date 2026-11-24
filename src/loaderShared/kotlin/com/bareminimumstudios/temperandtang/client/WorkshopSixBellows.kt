package com.bareminimumstudios.temperandtang.client

import com.pockyl.diegesis.api.client.MeshShape
import com.pockyl.diegesis.api.client.StationView
import net.minecraft.resources.ResourceLocation
import kotlin.math.*

/** Split Workshop Six bellows: fixed stand/nozzle, hinged boards, flexible textured leather. */
object WorkshopSixBellows {
    // Subpixel leather poses are shared between stations and reused across pump cycles.
    private val meshes=arrayOfNulls<MeshShape>(257)
    private val texture=ResourceLocation.fromNamespaceAndPath("temperandtang","block/workshop_six/blacksmith_bellows")
    fun frame(view:StationView,opening:Float) {
        val angle=(8f+(opening-2f)*4.2f).coerceIn(8f,24f)
        view.node("workshop_six_bellows_top").ifPresent {it.rotateTo(0f,0f,angle,0f)}
        val key=((angle-8f)*16f).roundToInt().coerceIn(0,256)
        meshes[key]?.let {shape->view.node("workshop_six_bellows_leather").ifPresent {it.setShape(shape,texture)};return}
        val leatherAngle=8f+key/16f
        val height=7.2f*(.18f+.82f*sin(leatherAngle*PI.toFloat()/180f)/sin(PI.toFloat()/8f))
        val x0=-3.619856f;val x1=9.980144f;val y0=-2.29876f;val y1=y0+height;val z0=-4.4f;val z1=4.4f
        val mesh=MeshShape.builder()
        fun face(points:Array<FloatArray>,uv:FloatArray,nx:Float,ny:Float,nz:Float) {
            val coords=arrayOf(floatArrayOf(uv[0],uv[1]),floatArrayOf(uv[0],uv[3]),floatArrayOf(uv[2],uv[3]),floatArrayOf(uv[2],uv[1]))
            for(i in points.indices){val p=points[i];mesh.corner(p[0],p[1],p[2],coords[i][0]/16f,coords[i][1]/16f,nx,ny,nz)}
        }
        face(arrayOf(floatArrayOf(x1,y1,z0),floatArrayOf(x1,y0,z0),floatArrayOf(x0,y0,z0),floatArrayOf(x0,y1,z0)),floatArrayOf(4.25f,3.25f,6.375f,4.375f),0f,0f,-1f)
        face(arrayOf(floatArrayOf(x0,y1,z1),floatArrayOf(x0,y0,z1),floatArrayOf(x1,y0,z1),floatArrayOf(x1,y1,z1)),floatArrayOf(6.375f,3.25f,4.25f,4.375f),0f,0f,1f)
        face(arrayOf(floatArrayOf(x1,y1,z1),floatArrayOf(x1,y0,z1),floatArrayOf(x1,y0,z0),floatArrayOf(x1,y1,z0)),floatArrayOf(0f,4.625f,1.375f,5.75f),1f,0f,0f)
        face(arrayOf(floatArrayOf(x0,y1,z0),floatArrayOf(x0,y0,z0),floatArrayOf(x0,y0,z1),floatArrayOf(x0,y1,z1)),floatArrayOf(1.375f,4.625f,2.75f,5.75f),-1f,0f,0f)
        face(arrayOf(floatArrayOf(x0,y1,z0),floatArrayOf(x0,y1,z1),floatArrayOf(x1,y1,z1),floatArrayOf(x1,y1,z0)),floatArrayOf(2.125f,4.625f,0f,3.25f),0f,1f,0f)
        face(arrayOf(floatArrayOf(x0,y0,z1),floatArrayOf(x0,y0,z0),floatArrayOf(x1,y0,z0),floatArrayOf(x1,y0,z1)),floatArrayOf(4.25f,3.25f,2.125f,4.625f),0f,-1f,0f)
        val shape=mesh.build();meshes[key]=shape
        view.node("workshop_six_bellows_leather").ifPresent {it.setShape(shape,texture)}
    }
}
