package com.technicalitiesmc.scm.init;

import java.io.File;

import static com.technicalitiesmc.scm.circuit.CircuitHelper.FOLDER_NAME;

public class SCMBlueprints {
    //检查文件夹是否存在。否则生成一个新的
    public static void checkFolder(){
        File schematics = new File(FOLDER_NAME);
        if(!schematics.exists()){
            schematics.mkdir();
        }
    }
}
