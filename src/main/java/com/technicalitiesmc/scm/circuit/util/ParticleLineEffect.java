package com.technicalitiesmc.scm.circuit.util;

import com.mojang.math.Vector3d;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import static com.technicalitiesmc.scm.circuit.util.ParticleDirector.positionConvertor;
import static com.technicalitiesmc.scm.circuit.util.ParticleDirector.showParticle;

public class ParticleLineEffect implements Runnable {

    private Minecraft minecraft;
    private Vector3d pos1,pos2;
    private boolean on = false;
    private boolean alive = true;
    private int time = 1;
    private double dx,dy,dz;

    public ParticleLineEffect(Minecraft minecraft){
        this.minecraft = minecraft;
    }

    public void loadLocations(BlockPos pos1,BlockPos pos2,ComponentSlotPos csp1,ComponentSlotPos csp2,int time){
        this.pos1 = positionConvertor(pos1,csp1);
        this.pos2 = positionConvertor(pos2,csp2);

        dx = (this.pos2.x-this.pos1.x)/((double) time);
        dy = (this.pos2.y-this.pos1.y)/((double) time);
        dz = (this.pos2.z-this.pos1.z)/((double) time);

        Thread thread = new Thread(this);
        thread.start();
    }

    public void loadLocations(Vector3d pos1,Vector3d pos2,int time){
        this.pos1 = pos1;
        this.pos2 = pos2;

        dx = (this.pos2.x-this.pos1.x)/((double) time);
        dy = (this.pos2.y-this.pos1.y)/((double) time);
        dz = (this.pos2.z-this.pos1.z)/((double) time);

        Thread thread = new Thread(this);
        thread.start();
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

    public boolean isAlive(){
        return alive;
    }

    public void run(){
        while (alive){
            if(on) {
                for (int i = 0; i <= time; i++) {
                    showParticle(minecraft, pos1.x + i * dx, pos1.y + i * dy, pos1.z + i * dz);
                    try{
                        Thread.sleep(50);
                    }catch (Exception e){}
                }
            }
        }
    }
}
