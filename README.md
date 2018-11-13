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
	    <version>Tag</version>
	</dependency>
```

## Usage

Create an UDFS instance with:
```Java
UDFS udfs = new UDFS("127.0.0.1",5001);
```

Then run commands like:
```Java
udfs.refs.local();
```

To add a file and bakeup other masternode use (the push method returns a list of merklenodes, in this case there is only one element):
```Java
NamedStreamable.FileWrapper file = new NamedStreamable.FileWrapper(new File("udfs.txt"));
MerkleNode addResult = udfs.push(file).get(0);
```

To push a byte[] use:
```Java
NamedStreamable.ByteArrayWrapper file = new NamedStreamable.ByteArrayWrapper("udfs.txt", "hello world".getBytes());
MerkleNode addResult = udfs.push(file).get(0);
```

To get a file use:
```Java
Multihash filePointer = Multihash.fromBase58("hash值");
byte[] fileContents = udfs.cat(filePointer);
```

## Dependencies

Current versions of dependencies are included in the `./lib` directory.

* [multibase](https://github.com/multiformats/java-multibase)
* [multiaddr](https://github.com/multiformats/java-multiaddr)
* [multihash](https://github.com/multiformats/java-multihash)
* [cid](https://github.com/ipld/java-cid)
