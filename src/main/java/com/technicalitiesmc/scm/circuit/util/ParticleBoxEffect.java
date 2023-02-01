package com.technicalitiesmc.scm.circuit.util;

import com.mojang.math.Vector3d;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import static com.technicalitiesmc.scm.circuit.util.ParticleDirector.positionConvertor;

public class ParticleBoxEffect {

    Minecraft minecraft;
    Vector3d pos1,pos2;
    ParticleLineEffect[] particleLineEffects = null;
    private double dx,dy,dz;

    private int time;

    public ParticleBoxEffect(Minecraft minecraft){
        this.minecraft = minecraft;
    }

    public void loadLocations(BlockPos pos1, BlockPos pos2, ComponentSlotPos csp1, ComponentSlotPos csp2, int time){
        this.pos1 = positionConvertor(pos1,csp1);
        this.pos2 = positionConvertor(pos2,csp2);
        this.time = time;

        dx = (this.pos2.x-this.pos1.x)/((double) time);
        dy = (this.pos2.y-this.pos1.y)/((double) time);
        dz = (this.pos2.z-this.pos1.z)/((double) time);

        if(dy == 0){
            if(dx == 0 || dz == 0){
                particleLineEffects = new ParticleLineEffect[1];
                particleLineEffects[0] = new ParticleLineEffect(minecraft){{loadLocations(ParticleBoxEffect.this.pos1,ParticleBoxEffect.this.pos2,ParticleBoxEffect.this.time);}};
            }else {
                particleLineEffects = new ParticleLineEffect[4];
                particleLineEffects[0] = new ParticleLineEffect(minecraft){{loadLocations(ParticleBoxEffect.this.pos1,new Vector3d(ParticleBoxEffect.this.pos2.x,ParticleBoxEffect.this.pos1.y,ParticleBoxEffect.this.pos1.z),ParticleBoxEffect.this.time);}};
                particleLineEffects[1] = new ParticleLineEffect(minecraft){{loadLocations(ParticleBoxEffect.this.pos1,new Vector3d(ParticleBoxEffect.this.pos1.x,ParticleBoxEffect.this.pos1.y,ParticleBoxEffect.this.pos2.z),ParticleBoxEffect.this.time);}};
                particleLineEffects[2] = new ParticleLineEffect(minecraft){{loadLocations(new Vector3d(ParticleBoxEffect.this.pos1.x,ParticleBoxEffect.this.pos1.y,ParticleBoxEffect.this.pos2.z),new Vector3d(ParticleBoxEffect.this.pos2.x,ParticleBoxEffect.this.pos1.y,ParticleBoxEffect.this.pos2.z),ParticleBoxEffect.this.time);}};
                particleLineEffects[3] = new ParticleLineEffect(minecraft){{loadLocations(new Vector3d(ParticleBoxEffect.this.pos2.x,ParticleBoxEffect.this.pos1.y,ParticleBoxEffect.this.pos1.z),new Vector3d(ParticleBoxEffect.this.pos2.x,ParticleBoxEffect.this.pos1.y,ParticleBoxEffect.this.pos2.z),ParticleBoxEffect.this.time);}};

            }
        }else {
            particleLineEffects = new ParticleLineEffect[12];
            particleLineEffects[0] = new ParticleLineEffect(minecraft){{loadLocations(ParticleBoxEffect.this.pos1,new Vector3d(ParticleBoxEffect.this.pos2.x,ParticleBoxEffect.this.pos1.y,ParticleBoxEffect.this.pos1.z),ParticleBoxEffect.this.time);}};
            particleLineEffects[1] = new ParticleLineEffect(minecraft){{loadLocations(ParticleBoxEffect.this.pos1,new Vector3d(ParticleBoxEffect.this.pos1.x,ParticleBoxEffect.this.pos1.y,ParticleBoxEffect.this.pos2.z),ParticleBoxEffect.this.time);}};
            particleLineEffects[2] = new ParticleLineEffect(minecraft){{loadLocations(new Vector3d(ParticleBoxEffect.this.pos1.x,ParticleBoxEffect.this.pos1.y,ParticleBoxEffect.this.pos2.z),new Vector3d(ParticleBoxEffect.this.pos2.x,ParticleBoxEffect.this.pos1.y,ParticleBoxEffect.this.pos2.z),ParticleBoxEffect.this.time);}};
            particleLineEffects[3] = new ParticleLineEffect(minecraft){{loadLocations(new Vector3d(ParticleBoxEffect.this.pos2.x,ParticleBoxEffect.this.pos1.y,ParticleBoxEffect.this.pos1.z),new Vector3d(ParticleBoxEffect.this.pos2.x,ParticleBoxEffect.this.pos1.y,ParticleBoxEffect.this.pos2.z),ParticleBoxEffect.this.time);}};
            particleLineEffects[4] = new ParticleLineEffect(minecraft){{loadLocations(ParticleBoxEffect.this.pos2,new Vector3d(ParticleBoxEffect.this.pos2.x,ParticleBoxEffect.this.pos2.y,ParticleBoxEffect.this.pos1.z),ParticleBoxEffect.this.time);}};
            particleLineEffects[5] = new ParticleLineEffect(minecraft){{loadLocations(ParticleBoxEffect.this.pos2,new Vector3d(ParticleBoxEffect.this.pos1.x,ParticleBoxEffect.this.pos2.y,ParticleBoxEffect.this.pos2.z),ParticleBoxEffect.this.time);}};
            particleLineEffects[6] = new ParticleLineEffect(minecraft){{loadLocations(new Vector3d(ParticleBoxEffect.this.pos1.x,ParticleBoxEffect.this.pos2.y,ParticleBoxEffect.this.pos2.z),new Vector3d(ParticleBoxEffect.this.pos1.x,ParticleBoxEffect.this.pos2.y,ParticleBoxEffect.this.pos1.z),ParticleBoxEffect.this.time);}};
            particleLineEffects[7] = new ParticleLineEffect(minecraft){{loadLocations(new Vector3d(ParticleBoxEffect.this.pos2.x,ParticleBoxEffect.this.pos2.y,ParticleBoxEffect.this.pos1.z),new Vector3d(ParticleBoxEffect.this.pos1.x,ParticleBoxEffect.this.pos2.y,ParticleBoxEffect.this.pos1.z),ParticleBoxEffect.this.time);}};
            particleLineEffects[8] = new ParticleLineEffect(minecraft){{loadLocations(ParticleBoxEffect.this.pos2,new Vector3d(ParticleBoxEffect.this.pos2.x,ParticleBoxEffect.this.pos1.y,ParticleBoxEffect.this.pos2.z),ParticleBoxEffect.this.time);}};
            particleLineEffects[9] = new ParticleLineEffect(minecraft){{loadLocations(ParticleBoxEffect.this.pos1,new Vector3d(ParticleBoxEffect.this.pos1.x,ParticleBoxEffect.this.pos2.y,ParticleBoxEffect.this.pos1.z),ParticleBoxEffect.this.time);}};
            particleLineEffects[10] = new ParticleLineEffect(minecraft){{loadLocations(new Vector3d(ParticleBoxEffect.this.pos1.x,ParticleBoxEffect.this.pos1.y,ParticleBoxEffect.this.pos2.z),new Vector3d(ParticleBoxEffect.this.pos1.x,ParticleBoxEffect.this.pos2.y,ParticleBoxEffect.this.pos2.z),ParticleBoxEffect.this.time);}};
            particleLineEffects[11] = new ParticleLineEffect(minecraft){{loadLocations(new Vector3d(ParticleBoxEffect.this.pos2.x,ParticleBoxEffect.this.pos1.y,ParticleBoxEffect.this.pos1.z),new Vector3d(ParticleBoxEffect.this.pos2.x,ParticleBoxEffect.this.pos2.y,ParticleBoxEffect.this.pos1.z),ParticleBoxEffect.this.time);}};
        }
    }

    public void on(){
        if(particleLineEffects != null){
            for(ParticleLineEffect ple:particleLineEffects){
                ple.on();
            }
        }
    }

    public void off(){
        if(particleLineEffects != null){
            for(ParticleLineEffect ple:particleLineEffects){
                ple.off();
            }
        }
    }

    public void stop(){
        if(particleLineEffects != null){
            for(ParticleLineEffect ple:particleLineEffects){
                ple.stop();
            }
        }
    }
}
