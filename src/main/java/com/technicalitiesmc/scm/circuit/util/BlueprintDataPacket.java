package com.technicalitiesmc.scm.circuit.util;

import com.technicalitiesmc.lib.circuit.component.ComponentSlot;
import com.technicalitiesmc.lib.circuit.component.ComponentState;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import static com.technicalitiesmc.scm.circuit.CircuitHelper.FILE_HEADER;
import static com.technicalitiesmc.scm.circuit.CircuitHelper.FILE_VERSION;


public class BlueprintDataPacket {
    private ArrayList<byte[]> posList = new ArrayList<byte[]>();
    private ArrayList<ComponentState> componentList = new ArrayList<ComponentState>();

    public static HashMap<String,Integer> IDTABLE = new HashMap<>(){
        {
            put("torch_bottom",0x00);
            put("randomizer",0x01);
            put("delay",0x02);
            put("pulsar",0x03);
            put("note",0x04);
            put("lamp",0x05);
            put("lever",0x06);
            put("button",0x07);
            put("platform",0x08);
            put("adder",0x09);
            put("subtractor",0x0a);
            put("multiplier",0x0b);
            put("divider",0x0c);
            put("constant",0x0d);
            put("redstone_wire",0x0f);
            put("vertical_wire",0x10);
            put("bundled_wire",0x11);
            put("vertical_bundled_wire",0x12);
            //put();
        }
    };

    public static final byte[] fileHead = FILE_HEADER.getBytes(StandardCharsets.UTF_8);//文件头
    public static final byte[] fileVersion = FILE_VERSION.getBytes(StandardCharsets.UTF_8);//文件版本

    HashMap<String,Integer> items = new HashMap<String,Integer>();

    public BlueprintDataPacket(){}

    public ArrayList<byte[]> getPosList() {
        return posList;
    }

    public ArrayList<ComponentState> getComponentList() {
        return componentList;
    }

    public void setComponentList(ArrayList<ComponentState> componentList) {
        this.componentList = componentList;
    }

    public void setPosList(ArrayList<byte[]> posList) {
        this.posList = posList;
    }

    public void addPos(byte[] pos){
        if(pos != null && pos.length == 3){
            posList.add(pos);
        }
    }

    public void addComponent(ComponentState cs){
        if(cs != null){
            componentList.add(cs);
        }
    }

    public boolean isEmpty(){
        return (posList.isEmpty() || componentList.isEmpty());
    }

    public void offset(int x,int z){
        for(byte[] pos:posList){
            pos[0] = (byte)(pos[0]+x);
            pos[2] = (byte)(pos[2]+z);
        }
    }

    public HashMap<String,Integer> getItems(){
        for(ComponentState cs:componentList){
            String name = cs.getComponentType().toString().replaceAll("ComponentType\\[supercircuitmaker:","").replaceAll("]","");
            //以红石火把底部计入item数量
            if(!name.equals("torch_top")) {
                if (items.containsKey(name)) {
                    items.put(name, items.get(name) + 1);
                    continue;
                }
                items.put(name, 1);
            }
        }
        return items;
    }

    public static HashMap<String , Integer> itemMapMerge(HashMap<String , Integer> a,HashMap<String,Integer> b){
        for(String x:b.keySet()){
            if(a.containsKey(x)){
                a.put(x,b.get(x)+a.get(x));
            }else {
                a.put(x,b.get(x));
            }
        }
        return a;
    }

    //b的数据会跟在a后面
    public static ArrayList<byte[]> posListMerge(ArrayList<byte[]> a,ArrayList<byte[]> b){
        for(byte[] data : b){
            a.add(data);
        }
        return a;
    }

    public static byte[] getPosSerialize(ArrayList<byte[]> position){
        if(!position.isEmpty()){
            int length = 3*position.size()+2;
            byte[] result = new byte[length];

            //写入长度
            byte[] lengthBytes = twoBytesFormatting(length);
            result[0] = lengthBytes[0];
            result[1] = lengthBytes[1];

            //装填进所有位置数据
            for(int i = 0;i<position.size();i++){
                result[3*i+2] = position.get(i)[0];//x
                result[3*i+3] = position.get(i)[1];//y
                result[3*i+4] = position.get(i)[2];//z
            }
            return result;
        }
        return null;
    }

    public static byte[] getItemsSerialize(HashMap<String,Integer> itemMap){
        if(!itemMap.isEmpty()){
            String[] item = new String[itemMap.size()];

            int ix = 0;
            for(String str : itemMap.keySet()){
                item[ix] = str;
                ix++;
            }

            int length = 3*item.length+2;
            byte[] result = new byte[length];

            //写入长度
            byte[] lengthBytes = twoBytesFormatting(length);
            result[0] = lengthBytes[0];
            result[1] = lengthBytes[1];

            //写入数据(物品ID和数量)，三字节为一组，第一字节为ID，第二、三字节为数量
            for(int i = 0;i<item.length;i++){
                result[3*i+2] = (byte) IDTABLE.get(item[i]).intValue();
                byte[] dataBytes = twoBytesFormatting(itemMap.get(item[i]));
                result[3*i+3] = dataBytes[0];
                result[3*i+4] = dataBytes[1];
            }
            return result;
        }
        return null;
    }

    public static byte[] twoBytesFormatting(int data){
        if(data>65535 || data<0) return null;
        return new byte[]{(byte)(data%256),(byte)(data-256*(data%256))};
    }

    public static void writeFileHead(BufferedOutputStream bos,String blueprintName,String blueprintIntroduction,String blueprintAuthor) throws IOException {
        //对各字符串这个进行UTF_8编码
        byte[] name = blueprintName.getBytes(StandardCharsets.UTF_8);//蓝图名
        byte[] introduction = blueprintIntroduction.getBytes(StandardCharsets.UTF_8);//蓝图介绍
        byte[] author = blueprintAuthor.getBytes(StandardCharsets.UTF_8);//蓝图作者

        //文件头
        bos.write(fileHead);
        //文件版本
        bos.write(twoBytesFormatting(fileVersion.length));
        bos.write(fileVersion);
        //蓝图名字
        bos.write(twoBytesFormatting(name.length));
        bos.write(name);
        //蓝图介绍
        bos.write(twoBytesFormatting(introduction.length));
        bos.write(introduction);
        //蓝图作者
        bos.write(twoBytesFormatting(author.length));
        bos.write(author);

        bos.flush();
    }
}