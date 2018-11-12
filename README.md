# java-udfs-api

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
