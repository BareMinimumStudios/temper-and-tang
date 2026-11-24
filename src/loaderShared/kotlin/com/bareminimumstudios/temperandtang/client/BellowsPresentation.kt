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
    private var drive=0f
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
    private fun ring(i:Int,j:Int,angle:Float):Vector3f {
        val u=j/6f;val p=outline[i];val inset=if(j%2==0)1f else .88f
        val z=7f+(p.second-7f)*(1-u+u*cos(angle))
        val top=1.25f+(7f-p.second)*sin(angle)
        return Vector3f(p.first*inset,.65f+(top-.65f)*u,7f+(z-7f)*inset)
    }
    private fun leather(angle:Float):MeshShape {
        val b=MeshShape.builder()
        for(i in outline.indices)for(j in 0..5) {
            val next=(i+1)%outline.size
            quad(b,ring(i,j,angle),ring(next,j,angle),ring(next,j+1,angle),ring(i,j+1,angle))
        }
        return b.build()
    }
    private fun welts(angle:Float):MeshShape {
        val b=MeshShape.builder()
        fun edge(i:Int,j:Int):Vector3f {
            val p=ring(i,j,angle)
            // A tiny outward offset keeps the sewn edge clear of the leather surface.
            return Vector3f(p.x*1.012f,p.y,7f+(p.z-7f)*1.012f)
        }
        for(j in listOf(0,2,4,6))for(i in outline.indices) {
            val p=edge(i,j);val q=edge((i+1)%outline.size,j)
            quad(b,Vector3f(p).add(0f,-.065f,0f),Vector3f(q).add(0f,-.065f,0f),Vector3f(q).add(0f,.065f,0f),Vector3f(p).add(0f,.065f,0f))
        }
        return b.build()
    }
    private val lower=board(false)
    private val upper=board(true)
    fun frame(view:StationView,dt:Float) {
        view.node("hearth_body").ifPresent {it.setVisible(view.isBlockHidden)}
        time+=dt
        val heating=view.state().data().getString("status")=="heating"
        // These furnished fireboxes have their own animated fire, logs and interior atlas.
        // Do not add loose flames or metal sparks in front of their doors.
        if(view.node("furnace_body").isPresent) {
            view.node("furnace_body").ifPresent {it.setVisible(view.isBlockHidden)}
            return
        }
        drive+=((if(heating)1f else 0f)-drive)*(1-exp(-dt*7f))
        val target=3f+drive*(.9f+1.9f*sin(time*5f))
        val previousOpening=opening
        opening+=(target-opening)*(1-exp(-dt*15f))
        val angle=asin(((opening-1.25f)/12f).coerceIn(0f,.8f))
        if(view.node("workshop_six_bellows_base").isPresent)WorkshopSixBellows.frame(view,opening)
        val wood=ResourceLocation.withDefaultNamespace("block/spruce_planks")
        view.node("bellows_lower").ifPresent {it.setShape(lower,wood)}
        view.node("bellows_top").ifPresent {it.setShape(upper,wood);it.rotateTo(angle*180f/PI.toFloat(),0f,0f,0f)}
        view.node("bellows_leather").ifPresent {it.setShape(leather(angle),ResourceLocation.withDefaultNamespace("block/brown_terracotta"))}
        view.node("bellows_welts").ifPresent {it.setShape(welts(angle),ResourceLocation.withDefaultNamespace("block/dark_oak_planks"))}
        val compression=(previousOpening-opening)/dt.coerceAtLeast(.0001f)
        val pressure=if(heating)((compression/9.5f-.45f)/.55f).coerceIn(0f,1f) else 0f
        view.node("fire").ifPresent {it.setEmitRate(6f+20f*pressure)}
        val r=(216+39*pressure).toInt();val g=(183+72*pressure).toInt();val b=(158+97*pressure).toInt()
        view.node("embers").ifPresent {it.setTint((0xFF000000L or (r.toLong() shl 16) or (g.toLong() shl 8) or b.toLong()).toInt())}
        // Keep distinct compression bursts, but retain sub-frame time instead of resetting the clock per puff.
        gust=if(pressure>0f)gust+dt.coerceIn(0f,.1f) else .065f
        var emitted=0
        while(pressure>0f && gust>=.065f && emitted++<3) {
            gust-=.065f
            val age=gust.coerceIn(0f,.1f)
            val mouth=SceneCoordinates.point(view,"bellows_nozzle_tip",0f,0f,0f)
            val coal=SceneCoordinates.point(view,"hearth_scene",8.5f,7.25f,8f)
            val air=Vector3f(coal).sub(mouth).normalize().mul(15f)
            view.spawnParticles(TemperAndTang.id("bellows_air"),Vector3f(mouth).fma(age,air),air,.10f,3,0)
            SceneCoordinates.burst(view,"hearth_scene",DiegesisParticles.FLAME,Vector3f(8.5f,7.25f,8f),Vector3f(0f,5f,0f),.45f,2,0)
            SceneCoordinates.burst(view,"hearth_scene",TemperAndTang.id("forge_spark"),Vector3f(8f,7.25f,8f),Vector3f(0f,7f,0f),.5f,1,0xFFFFB85C.toInt())
        }
    }
}
