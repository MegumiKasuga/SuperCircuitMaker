package com.technicalitiesmc.scm.circuit.util;

import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;

public class ParticleDirector implements Runnable{

    private Minecraft minecraft;
    private BlockPos blockPos;
    private ComponentSlotPos csp;
    private boolean on = false;

    private boolean alive = true;

    public ParticleDirector(Minecraft minecraft){this.minecraft = minecraft;}

    public void loadLocation(BlockPos blockPos,ComponentSlotPos csp){
        this.blockPos = blockPos;
        this.csp = csp;
        Thread thread = new Thread(this);
        thread.start();
    }

    public static void showParticle(Minecraft minecraft, BlockPos pos,ComponentSlotPos componentSlotPos){
        minecraft.level.addParticle(new DustParticleOptions(new Vector3f(92.0f, 247.0f, 100.0f), 1.0f), ((double) pos.getX()) + ((double) componentSlotPos.pos().x() + 1.5) / 9, ((double) pos.getY()) + ((double) componentSlotPos.pos().y() + 3) / 9, ((double) pos.getZ()) + ((double) componentSlotPos.pos().z() + 1.5) / 9, 0, 0, 0);
    }

    public static void showParticle(Minecraft minecraft, double x,double y,double z){
        minecraft.level.addParticle(new DustParticleOptions(new Vector3f(92.0f, 247.0f, 100.0f), 1.0f), x, y, z, 0, 0, 0);
    }

    public static Vector3d positionConvertor(BlockPos pos,ComponentSlotPos componentSlotPos){
        return new Vector3d(((double) pos.getX()) + ((double) componentSlotPos.pos().x() + 1.5) / 9,((double) pos.getY()) + ((double) componentSlotPos.pos().y() + 3) / 9,((double) pos.getZ()) + ((double) componentSlotPos.pos().z() + 1.5) / 9);
    }

    public void run(){
        while (alive) {
            if (on) {
                showParticle(minecraft,blockPos,csp);
            }
            try {
                Thread.sleep(50);
            } catch (Exception e) {
            }
        }
    }

    public void on(){
        on = true;
    }

    public void off(){
        on = false;
    }

    public boolean isOn(){
        return on;
    }

    public void stop(){
        alive = false;
    }
}
