package com.bareminimumstudios.temperandtang.client

import com.pockyl.diegesis.api.client.StationView
import net.minecraft.resources.ResourceLocation
import org.joml.Vector3f

/** Particle positions use the same composed transform as the model they leave. */
internal object SceneCoordinates {
    fun point(view:StationView,node:String,x:Float,y:Float,z:Float):Vector3f {
        val p=Vector3f(x,y,z);view.node(node).ifPresent {it.sceneMatrix().transformPosition(p)};return p
    }
    fun burst(view:StationView,node:String,style:ResourceLocation,at:Vector3f,velocity:Vector3f,spread:Float,count:Int,color:Int) {
        val p=Vector3f(at);val v=Vector3f(velocity);var scale=1f
        view.node(node).ifPresent {n->val m=n.sceneMatrix();m.transformPosition(p);m.transformDirection(v);scale=m.getScale(Vector3f()).x}
        view.spawnParticles(style,p,v,spread*scale,count,color)
    }
}
