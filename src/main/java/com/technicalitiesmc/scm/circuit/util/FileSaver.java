package com.technicalitiesmc.scm.circuit.util;

import com.technicalitiesmc.lib.circuit.component.ComponentState;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.lwjgl.system.CallbackI;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static com.technicalitiesmc.scm.circuit.CircuitHelper.FOLDER_NAME;
import static com.technicalitiesmc.scm.circuit.util.BlueprintDataPacket.fileHead;
import static com.technicalitiesmc.scm.circuit.util.BlueprintDataPacket.fileVersion;

public class FileSaver {
    byte[] itemBytes = null;
    byte[] posBytes = null;
    ArrayList<BlueprintDataPacket> datas = null;

    public FileSaver(byte[] itemBytes, byte[] posBytes,ArrayList<BlueprintDataPacket> datas) {
        this.itemBytes = itemBytes;
        this.posBytes = posBytes;
        this.datas = datas;
    }

    public ArrayList<BlueprintDataPacket> getDatas() {
        return datas;
    }

    public boolean save(String name, String introduction, String author) {

        if(itemBytes == null || posBytes == null || datas == null) return false;

        try {
            //生成文件和输出流
            File output = new File(FOLDER_NAME + "\\" + name + ".blueprint");
            FileOutputStream os = new FileOutputStream(output);
            BufferedOutputStream bos = new BufferedOutputStream(os);

            //输出字节
            BlueprintDataPacket.writeFileHead(bos, name, introduction, author);//文件头
            bos.write(itemBytes);//物品
            bos.write(posBytes);//位置

            //创建一个FriendlyByteBuf
            ByteBuf bf = Unpooled.buffer(64);
            FriendlyByteBuf buffer = new FriendlyByteBuf(bf);

            //写入元件数据(每个64字节)
            for (BlueprintDataPacket bdp2 : datas) {
                for (ComponentState cs : bdp2.getComponentList()) {
                    cs.serialize(buffer);
                    bos.write(buffer.accessByteBufWithCorrectSize());
                    buffer.clear();
                }
            }

            bos.flush();
            bos.close();
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public long getSize(String blueprintName, String blueprintIntroduction, String blueprintAuthor){

        if(itemBytes != null && posBytes != null && datas != null){
            long result = 0l;
            result += fileHead.length;
            result += fileVersion.length;

            result += itemBytes.length;
            result += posBytes.length;

            result += blueprintName.getBytes(StandardCharsets.UTF_8).length;
            result += blueprintIntroduction.getBytes(StandardCharsets.UTF_8).length;
            result += blueprintAuthor.getBytes(StandardCharsets.UTF_8).length;

            for (BlueprintDataPacket bdp2 : datas) {
                for (ComponentState cs : bdp2.getComponentList()) {
                    result += 64l;
                }
            }
            return result;
        }
        return -1;
    }
}
