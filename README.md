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

environment configuration:
```
testnet environment:
url=http://test.api.udfs.one:15001
```
```
formal environment:
url=http://api.udfs.one:15001
```


To add a file and bakeup other masternode use (the push method returns a list of merklenodes, in this case there is only one element):
```Java
String filePath = "C:\\Users\\Allen\\Desktop\\certificate\\1.png";
        String url="http://test.api.udfs.one:15001/api/v0/add";
        String type="add";
        UdfsDevTools tools = new UdfsDevTools(1000, "", "", filePath, "");
        File file = new File(filePath);
        String Result = UdfsDevTools.FilePost(url, file,tools.getToken(type));
        System.out.println("Result:"+Result);
```

To push a byte[] use:
```Java
String filePath = "C:\\Users\\Allen\\Desktop\\certificate\\1.png";
        String url="http://test.api.udfs.one:15001/api/v0/push";
        String type="add";
        UdfsDevTools tools = new UdfsDevTools(1000, "", "", filePath, "");
        File file = new File(filePath);
        String Result = UdfsDevTools.FilePost(url, file,tools.getToken(type));
        System.out.println("Result:"+Result);
```

To get a file use:
```Java
String hash="";
        String url="http://test.api.udfs.one:15001/api/v0/cat/"+hash;
        String type="get";
        UdfsDevTools tools = new UdfsDevTools(100, "", "", "");
        System.out.println(tools.getToken(type));
        UdfsDevTools.download(url, "微信图片_20181113205946.jpg","F:\\分布式存储\\",tools.getToken(type));
```

## Dependencies

Current versions of dependencies are included in the `./lib` directory.

* [multibase](https://github.com/multiformats/java-multibase)
* [multiaddr](https://github.com/multiformats/java-multiaddr)
* [multihash](https://github.com/multiformats/java-multihash)
* [cid](https://github.com/ipld/java-cid)
