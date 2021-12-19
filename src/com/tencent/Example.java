package com.tencent;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

//  example of parameter parse, the final binary should be able to accept specified parameters as requested
//
//  usage example:
//      ./run --data_path /tmp/data --dst_ip 127.0.0.1 --dst_port 3306 --dst_user root --dst_password 123456789
//
//  you can test this example by:
//   mkdir -p target && javac -sourcepath ./src/ ./src/com/tencent/Example.java  -d ./target/
//   cd target && java -classpath . com.tencent.Example  --data_path /tmp/data --dst_ip 127.0.0.1 --dst_port 3306 --dst_user root
//      --dst_password 123456789
public class Example {
    @Parameter(names = {"--data_path"}, description = "dir path of source data")
    public String DataPath = "";

    @Parameter(names = {"--dst_ip"}, description = "ip of dst database address")
    public String DstIP = "";

    @Parameter(names = {"--dst_port"}, description = "port of dst database address")
    public Integer DstPort = 0;

    @Parameter(names = {"--dst_user"}, description = "user name of dst database")
    public String DstUser = "";

    @Parameter(names = {"--dst_password"}, description = "password of dst database")
    public String DstPassword = "";

    public static void main(String[] args) {
        Example example = new Example();

        //parse cmd
        JCommander jc = JCommander.newBuilder().addObject(example).build();
        jc.parse(args);

        //jc.usage();  //print command parameter usage

        example.run(); //print command parameters
    }


    public void run() {
        System.out.printf("data path:%s\n",DataPath);
        System.out.printf("dst ip:%s\n",DstIP);
        System.out.printf("dst port:%d\n",DstPort);
        System.out.printf("dst user:%s\n",DstUser);
        System.out.printf("dst password:%s\n",DstPassword);
    }
}
