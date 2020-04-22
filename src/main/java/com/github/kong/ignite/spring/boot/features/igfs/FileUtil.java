package com.github.kong.ignite.spring.boot.features.igfs;

import com.github.kong.ignite.spring.boot.context.IgniteApplicationContextHolder;
import org.apache.commons.io.FileUtils;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteFileSystem;
import org.apache.ignite.igfs.IgfsPath;

import java.io.File;
import java.io.IOException;

/**
 * 整合igfs文件系统
 */
public class FileUtil {

    private static Ignite ignite = (Ignite) IgniteApplicationContextHolder.getBean("igniteClient");

    private static IgniteFileSystem fs = ignite.fileSystem("igfs");

    /**
     * 从igfs中检出文件
     * @param tempPath 检出的本地路径
     * @param filePath igfs的文件路径
     */
    public static void findFile(String tempPath,String filePath) {
        IgfsPath igfsPath = new IgfsPath(filePath);
        byte[] data = IgfsUtil.read(fs,igfsPath);
        File file = new File(tempPath + filePath);
        try {
            //如果缓存中无文件，那返回原来的;否则就覆盖文件
            if(data.length > 0){
                //删除旧文件
                FileUtils.deleteQuietly(file);
                FileUtils.writeByteArrayToFile(file,data);
                //将文件移出内存
                deleteFile(filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从igfs中检出文件
     * @param filePath igfs的文件路径
     */
    public static File findFile(String filePath) {
        IgfsPath igfsPath = new IgfsPath(filePath);
        byte[] data = IgfsUtil.read(fs,igfsPath);
        File file = new File(FileUtils.getTempDirectoryPath() + filePath);
        try {
            //如果缓存中无文件，那返回原来的;否则就覆盖文件
            if(data.length > 0){
                //删除旧文件
                FileUtils.deleteQuietly(file);
                FileUtils.writeByteArrayToFile(file,data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    /**
     * igfs创建文件
     * @param filePath
     * @param data
     */
    public static void createFile(String filePath, byte[] data) {
        IgfsPath igfsPath = new IgfsPath(filePath);
        IgfsUtil.create(fs,igfsPath,data);
    }

    /**
     * igfs追加文件
      * @param filePath
     * @param data
     */
    public static void append(String filePath, byte[] data) {
        IgfsPath igfsPath = new IgfsPath(filePath);
        IgfsUtil.append(fs,igfsPath,data);
    }

    /**
     * igfs删除文件
     * @param filePath
     */
    private static void deleteFile(String filePath) {
        IgfsPath igfsPath = new IgfsPath(filePath);
        IgfsUtil.delete(fs,igfsPath);
    }

    /**
     * igfs创建文件夹
     * @param dirPath
     */
    public static void mkdirs(String dirPath){
        IgfsPath igfsPath = new IgfsPath(dirPath);
        IgfsUtil.mkdirs(fs,igfsPath);
    }
}
