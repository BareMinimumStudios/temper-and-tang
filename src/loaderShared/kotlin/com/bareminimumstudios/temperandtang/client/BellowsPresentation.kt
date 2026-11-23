package com.bareminimumstudios.temperandtang.client

import com.bareminimumstudios.temperandtang.TemperAndTang
import com.pockyl.diegesis.api.client.*
import net.minecraft.resources.ResourceLocation
import org.joml.Vector3f
import kotlin.math.*

/** A hinged lid and six flexible leather folds, rather than a scaled solid box. */
class BellowsPresentation {
    private val outline=listOf(-5f to -5f,5f to -5f,6f to -2f,4f to 4f,1.3f to 7f,-1.3f to 7f,-4f to 4f,-6f to -2f)
    private var time=0f
    private var opening=3f
    private var gust=0f
    private fun quad(b:MeshShape.Builder,a:Vector3f,c:Vector3f,d:Vector3f,e:Vector3f) {
        val n=Vector3f(c).sub(a).cross(Vector3f(d).sub(a)).normalize().negate()
        for((p,uv) in listOf(e to (0f to 1f),d to (1f to 1f),c to (1f to 0f),a to (0f to 0f)))b.corner(p.x,p.y,p.z,uv.first,uv.second,n.x,n.y,n.z)
    }
    private fun board(hinged:Boolean):MeshShape {
        val b=MeshShape.builder()
        val zOffset=if(hinged)7f else 0f
        for(i in outline.indices) {
            val a=outline[i];val c=outline[(i+1)%outline.size]
            val p=Vector3f(a.first,0f,a.second-zOffset);val q=Vector3f(c.first,0f,c.second-zOffset)
            quad(b,p,q,Vector3f(q).add(0f,.65f,0f),Vector3f(p).add(0f,.65f,0f))
            fun face(points:List<Vector3f>,ny:Float) {
                for(v in points)b.corner(v.x,v.y,v.z,(v.x+6f)/12f,(v.z+zOffset+5f)/12f,0f,ny,0f)
            }
            face(listOf(Vector3f(0f,.65f,-zOffset),Vector3f(q).add(0f,.65f,0f),Vector3f(p).add(0f,.65f,0f),Vector3f(p).add(0f,.65f,0f)),1f)
            face(listOf(Vector3f(0f,0f,-zOffset),p,q,q),-1f)
        }
        return b.build()
    }
    private fun leather(angle:Float):MeshShape {
        val b=MeshShape.builder()
        fun ring(i:Int,j:Int):Vector3f {
            val u=j/6f;val p=outline[i];val inset=if(j%2==0)1f else .88f
            val z=7f+(p.second-7f)*(1-u+u*cos(angle))
            val top=1.25f+(7f-p.second)*sin(angle)
            return Vector3f(p.first*inset,.65f+(top-.65f)*u,7f+(z-7f)*inset)
        }
        for(i in outline.indices)for(j in 0..5) {
            val next=(i+1)%outline.size
            quad(b,ring(i,j),ring(next,j),ring(next,j+1),ring(i,j+1))
        }
        return b.build()
    }
    private val lower=board(false)
    private val upper=board(true)
    fun frame(view:StationView,dt:Float) {
        time+=dt
        val heating=view.state().data().getString("status")=="heating"
        val target=if(heating)3.9f+1.9f*sin(time*5f) else 3f
        opening+=(target-opening)*(1-exp(-dt*15f))
        val angle=asin(((opening-1.25f)/12f).coerceIn(0f,.8f))
        val wood=ResourceLocation.withDefaultNamespace("block/spruce_planks")
        view.node("bellows_lower").ifPresent {it.setShape(lower,wood)}
        view.node("bellows_top").ifPresent {it.setShape(upper,wood);it.rotateTo(angle*180f/PI.toFloat(),0f,0f,0f)}
        view.node("bellows_leather").ifPresent {it.setShape(leather(angle),ResourceLocation.withDefaultNamespace("block/brown_terracotta"))}
        val compression=heating && cos(time*5f)<-.45f
        view.node("fire").ifPresent {it.setEmitRate(if(compression)18f else 6f)}
        gust+=dt
        if(compression && gust>.14f) {
            gust=0f
            view.spawnParticles(DiegesisParticles.DUST,Vector3f(13.7f,7.5f,8f),Vector3f(-9f,1f,0f),.25f,3,0x809BAABC.toInt())
            view.spawnParticles(TemperAndTang.id("forge_spark"),Vector3f(8f,6.5f,8f),Vector3f(0f,10f,0f),1.2f,2,0xFFFFB85C.toInt())
        }
    }
}
