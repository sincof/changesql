package com.sin.service;

import java.io.*;

public class ProgramStatus {
    public static String path = "program_status";
    public static File file = new File(path);
    public static int status = 0;

    public ProgramStatus() {
    }

    // 程序开始时候运行
    public static int getProgramStatus() {
        if (!file.exists()) {
            try {
                while (!file.exists() && !file.createNewFile()) {
                }
                file.setReadable(true);
                file.setWritable(true);
            } catch (IOException ioe) {
                System.out.println("There is error in creating the file");
                ioe.printStackTrace();
            }
            try (FileWriter fileWriter = new FileWriter(file)) {
                status = -1;
                fileWriter.write('1');
                System.out.println("-1 status");
            } catch (IOException ioException) {
                System.out.println("File does not exist");
                ioException.printStackTrace();
            }
            return -1;
        } else {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                int n = Integer.parseInt(br.readLine());
                FileWriter fileWriter = new FileWriter(file);
                switch (n) {
                    case 1:
                        fileWriter.write('3');
                        status = 1;
                        fileWriter.flush();
                        System.out.println("3 status");
                        break;
                    case 3:
                        fileWriter.write('3');
                        status = 3;
                        fileWriter.flush();
                        System.out.println("3 status");
                        break;
                    case 2:
                        fileWriter.write('4');
                        status = 2;
                        fileWriter.flush();
                        System.out.println("4 status");
                        break;
                    case 4:
                        fileWriter.write('4');
                        status = 4;
                        fileWriter.flush();
                        System.out.println("4 status");
                        break;
                    default:
                        return 5;
                }
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return status;
    }

    // 完成创建数据库和表格开始运行
    public static int finishWR() {
        try (FileWriter fileWriter = new FileWriter(file)) {
            switch (status) {
                case -1:
                    fileWriter.write('2');
                    status = 2;
                    fileWriter.flush();
                    System.out.println("2 status");
                    break;
                case 1, 2, 3, 4:
                    fileWriter.write('4');
                    status = 4;
                    fileWriter.flush();
                    System.out.println("4 status");
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return status;
    }
}


/**
 * CODE: 没办法，要以一开始杀两次，只能硬搞
 * 完成创建数据库和表格开始运行
 * <p>
 * 1. 第一次运行
 * 没有文件：首先判断目录下面有没有1个文件，如果没有的话，先创建一个文件，并且写入1，并且开始执行database和table的创建工作，如果完成创建工作的话，就将1替换成2，然后一直阻塞
 * 2. 第二次运行
 * 1：没有完成database和table的创建工作，一进入程序，读取到1，就写入3，则继续完成database和table的创建工作，如果完成了创建工作就将3替换成4，然后一直阻塞
 * 2：一进入程序直接写入4，完成了database和table的创建工作，但是是处于第二次kill的情况，直接阻塞
 * 3. 第三次运行
 * 3：开始database和table的创建工作，然后插入数据
 * 4：直接开始插入数据
 */