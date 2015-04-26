package com.newer.filemanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by dell on 2015/3/24.
 */
public class FileUtil {
    /**
     * 创建文件夹
     * @param file
     * @param name
     * @return
     * @throws IOException
     */
    public static int index = 0;
    public static boolean createFolder(File file,String name,List<File> data) throws IOException{

        File f = new File(file.getAbsolutePath()+"/"+name);
        if(data.contains(f)){
            f = new File(file.getAbsolutePath()+"/"+name+"("+(index++)+")");
        }
        data.add(f);
        return f.mkdir();
    }
    /**
     * 复制文件夹
     * @param src
     * @param dest
     * @throws IOException
     */
    public static void copyFolder(File src,File dest) throws IOException{
        dest.mkdir();
        File[] files = src.listFiles();
        for(File file : files){
            File sDest = new File(dest.getPath()+"/"+file.getName());
            if(file.isDirectory()){
                copyFolders(file,sDest);
            }
            if(file.isFile()){
                copyFile(file,sDest);
            }
        }
    }
    /**
     * 复制文件 流
     * @param src
     * @param sDest
     * @throws IOException
     */
    public static void copyFile(File src, File sDest) throws IOException {

        FileInputStream inputStream = new FileInputStream(src);
        FileOutputStream outputStream = new FileOutputStream(sDest);
        byte[] buf = new byte[1024*10];
        int size ;
        while(-1 != (size = inputStream.read(buf))){
            outputStream.write(buf,0,size);
        }
        inputStream.close();
        outputStream.close();


    }
    /**
     * 复制多个文件夹 与copyFolder互相调用
     * @param sSrc
     * @param sDest
     * @throws IOException
     */
    private static void copyFolders(File sSrc, File sDest) throws IOException {
        sDest.mkdir();
        File[] files = sSrc.listFiles();
        for(File f : files){
            if(f.isFile()){
                copyFile(f, new File(sDest.getPath()+"/"+f.getName()));
            }if(f.isDirectory()){
                copyFolder(f, new File(sDest.getPath()+"/"+f.getName()) );
            }
        }

    }

    /**
     * 删除文件夹
     * @param src
     */
    public static void deleteFolder(File src){
        File[] files = src.listFiles();
        if(files != null){
            for(File f : files){
                if(f.isDirectory()){
                    deleteFolder(f);
                    if(!f.isFile()){
                        f.delete();
                    }
                }
                if(f.isFile()){
                    f.delete();
                }
            }
            src.delete();
        }else{
            src.delete();
        }
    }
    /**
     * 删除文件
     */

    /**
     * 重命名
     */

    public static File rename(File file,String newName){
        String name = file.getParentFile().getPath() + "/"+newName;
        File newFile = new File(name);
        file.renameTo(newFile);
        return newFile;
    }

    /**
     * 格式化文件大小
     * @param size
     * @return
     */
    public static String getDataSize(long size) {
        DecimalFormat formater = new DecimalFormat("####.00");
        if (size < 1024) {
            return size + "bytes";
        } else if (size < 1024 * 1024) {
            float kbsize = size / 1024f;
            return formater.format(kbsize) + "KB";
        } else if (size < 1024 * 1024 * 1024) {
            float mbsize = size / 1024f / 1024f;
            return formater.format(mbsize) + "MB";
        } else if (size < 1024 * 1024 * 1024 * 1024) {
            float gbsize = size / 1024f / 1024f / 1024f;
            return formater.format(gbsize) + "GB";
        } else {
            return "size: error";
        }
        }
    }