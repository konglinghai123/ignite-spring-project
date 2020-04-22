package com.github.kong.ignite.spring.boot.features.igfs;

import com.google.common.primitives.Bytes;
import org.apache.ignite.IgniteException;
import org.apache.ignite.IgniteFileSystem;
import org.apache.ignite.igfs.IgfsException;
import org.apache.ignite.igfs.IgfsInputStream;
import org.apache.ignite.igfs.IgfsPath;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

public class IgfsUtil {

    private static final Logger logger = LoggerFactory.getLogger(IgfsUtil.class);

    private static Object lock = new Object();

    /**
     * 删除文件
     * @param fs
     * @param path
     */
    public static void delete(IgniteFileSystem fs, IgfsPath path) {

        if (fs.exists(path)) {
            boolean isFile = fs.info(path).isFile();

            try {
                fs.delete(path, true);
            }
            catch (IgfsException e) {
                logger.error(">>> Failed to delete " + (isFile ? "file" : "directory") + " [path=" + path +
                    ", msg=" + e.getMessage() + ']');
            }
        }
        else {
            logger.info(">>> Won't delete file or directory (doesn't exist): " + path);
        }
    }

    /**
     * 创建文件夹
     *
     * @param fs IGFS.
     * @param path Directory path.
     * @throws IgniteException In case of error.
     */
    public static void mkdirs(IgniteFileSystem fs, IgfsPath path){

        try {
            fs.mkdirs(path);

            logger.info(">>> Created directory: " + path);
        }
        catch (IgfsException e) {
            logger.error(">>> Failed to create a directory [path=" + path + ", msg=" + e.getMessage() + ']');
        }
    }

    /**
     * 创建文件
     *
     * @param fs IGFS.
     * @param path File path.
     * @param data Data.
     * @throws IgniteException If file can't be created.
     * @throws IOException If data can't be written.
     */
    public static void create(IgniteFileSystem fs, IgfsPath path, @Nullable byte[] data) {

        try (OutputStream out = fs.create(path, true)) {
            logger.info(">>> Created file: " + path);
            if (data != null) {
                out.write(data,0,data.length);
                out.close();
                logger.info(">>> Wrote data to file: " + path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 追加文件内容
     * @param fs
     * @param path
     * @param appendData
     */
    public static void append(IgniteFileSystem fs, IgfsPath path, byte[] appendData) {

        if(!fs.exists(path)){
            fs.create(path,true);
        }

        if(!fs.info(path).isFile()){
            logger.error(">>> can't append data to path " + path);
            return;
        }

        synchronized (lock){

            int pos = (int)fs.info(path).length();
            byte[] data = new byte[pos + appendData.length];
            IgfsInputStream is = fs.open(path);
            try {
                is.read(data);
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            data = Bytes.concat(data,appendData);
            create(fs, path, data);

        }
    }

    /**
     * 读取文件
     *
     * @param fs IgniteFs.
     * @param path File path.
     * @throws IgniteException If file can't be opened.
     * @throws IOException If data can't be read.
     */
    public static byte[] read(IgniteFileSystem fs, IgfsPath path) {

        if(!fs.info(path).isFile()){
            logger.error(">>> there is not a file :" + path);
            return null;
        }

        byte[] data = new byte[(int)fs.info(path).length()];

        try (IgfsInputStream in = fs.open(path)) {
            in.read(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }


}
