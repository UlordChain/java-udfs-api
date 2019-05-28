# java-udfs-api

> A Java implementation of the UDFS http api

## Table of Contents

- [Install](#install)
- [Usage](#usage)
- [Dependencies](#dependencies)

## Install

### Official releases

You can use this project by including `ipfs.jar` from one of the [releases](https://github.com/UlordChain/java-udfs-api/releases).

### Maven, Gradle, SBT

Package managers are supported through [JitPack](https://jitpack.io/#UlordChain/java-udfs-api/) which supports Maven, Gradle, SBT, etc.

for Maven, add the following sections to your pom.xml (replacing $LATEST_VERSION):
```
	<repositories>
    		<repository>
    		    <id>jitpack.io</id>
    		    <url>https://jitpack.io</url>
    		</repository>
    	</repositories>
    <dependency>
    	    <groupId>com.github.UlordChain</groupId>
    	    <artifactId>java-udfs-api</artifactId>
    	    <version>v1.1.1</version>
    </dependency>
```

## Usage

Create an UDFS instance with:
```Java http
http: UDFS udfs = new UDFS("127.0.0.1",5001,false);
```
```Java https
https: UDFS udfs = new UDFS("127.0.0.1",5001,true);
```


To add a file and bakeup other masternode use (the push method returns a list of merklenodes, in this case there is only one element):
```Java
 //要添加文件使用
    NamedStreamable.FileWrapper file = new NamedStreamable.FileWrapper(new File("F:/test/xxx.txt"));
    //添加文件到UDFS返回HASH值
    List<MerkleNode> addParts = udfs.add(file);
    //输出HASH值
    //List<MerkleNode> addParts = udfs.push(file); 如果存在master节点，需要做主动备份用这个方法
    System.out.println(addParts.get(0).hash);
```

To push a byte[] use:
```Java
Multihash filePointer = Multihash.fromBase58(hash);
        byte[] data = udfs.cat(filePointer);
        if(data != null){
            File file  = new File(filePathName);   
            if(file.exists()){   
               file.delete();   
            }   
            FileOutputStream fos = new FileOutputStream(file);   
            fos.write(data,0,data.length);   
            fos.flush();   
            fos.close();   
          } 
```

To get a file use:
```Java
Multihash filePointer = Multihash.fromBase58("Qme7KYZZTkARzkwE4x3vLKC4zB1jtNdw5HwuCxqABE7Kgc");
        byte[] fileContents = udfs.cat(filePointer);
        String str=new String(fileContents);
        System.out.println("查询的内容为："+str);
        List<Multihash> pinRm=udfs.pin.rmlocal(filePointer);
        System.out.println("删除结果:"+pinRm.get(0).toString());
```

## Dependencies

Current versions of dependencies are included in the `./lib` directory.

* [multibase](https://github.com/multiformats/java-multibase)
* [multiaddr](https://github.com/multiformats/java-multiaddr)
* [multihash](https://github.com/multiformats/java-multihash)
* [cid](https://github.com/ipld/java-cid)
