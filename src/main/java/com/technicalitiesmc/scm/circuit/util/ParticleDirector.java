package com.technicalitiesmc.scm.circuit.util;

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

    public void run(){
        while (alive) {
            if (on) {
                minecraft.level.addParticle(new DustParticleOptions(new Vector3f(92.0f, 247.0f, 100.0f), 1.0f), ((double) blockPos.getX()) + ((double) csp.pos().x() + 1.5) / 9, ((double) blockPos.getY()) + ((double) csp.pos().y() + 3) / 9, ((double) blockPos.getZ()) + ((double) csp.pos().z() + 1.5) / 9, 0, 0, 0);
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
