package io.udfs.api;

import io.udfs.api.cbor.*;
import io.ipfs.cid.*;
import io.ipfs.multihash.Multihash;
import io.ipfs.multiaddr.MultiAddress;
import io.udfs.api.cbor.CborObject;
//import one.ulord.upaas.ucwallet.client.UDFSClient;
import io.udfs.api.util.UdfsDevTools;
import org.junit.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static org.junit.Assert.assertTrue;

public class APITest {
    //http访问方式
    private final UDFS udfs = new UDFS("132.232.99.150",5001,false);
    //https访问方式
    //private final UDFS udfs = new UDFS("114.67.37.76",443,false);
    private final Random r = new Random(33550336); // perfect

    @Test
    public void dag() throws IOException {
        String original = "{\"data\":1234}";
        byte[] object = original.getBytes();
        MerkleNode put = udfs.dag.put("json", object);

        Cid expected = Cid.decode("zdpuAs3whHmb9T1NkHSLGF45ykcKrEBxSLiEx6YpLzmKbQLEB");

        Multihash result = put.hash;
        Assert.assertTrue("Correct cid returned", result.equals(expected));

        byte[] get = udfs.dag.get(expected);
        Assert.assertTrue("Raw data equal", original.equals(new String(get).trim()));
    }

    @Test
    public void dagCbor() throws IOException {
        Map<String, CborObject> tmp = new LinkedHashMap<>();
        String value = "G'day mate!";
        tmp.put("data", new CborObject.CborString(value));
        CborObject original = CborObject.CborMap.build(tmp);
        byte[] object = original.toByteArray();
        MerkleNode put = udfs.dag.put("cbor", object);

        Cid cid = (Cid) put.hash;

        byte[] get = udfs.dag.get(cid);
        Assert.assertTrue("Raw data equal", ((Map)JSONParser.parse(new String(get))).get("data").equals(value));

        Cid expected = Cid.decode("zdpuApemz4XMURSCkBr9W5y974MXkSbeDfLeZmiQTPpvkatFF");
        Assert.assertTrue("Correct cid returned", cid.equals(expected));
    }

    @Test
    public void keys() throws IOException {
        List<KeyInfo> existing = udfs.key.list();
        String name = "mykey" + System.nanoTime();
        KeyInfo gen = udfs.key.gen(name, Optional.of("rsa"), Optional.of("2048"));
        String newName = "bob" + System.nanoTime();
        Object rename = udfs.key.rename(name, newName);
        List<KeyInfo> rm = udfs.key.rm(newName);
        List<KeyInfo> remaining = udfs.key.list();
        Assert.assertTrue("removed key", remaining.equals(existing));
    }

    @Test
    public void ipldNode() {
        Function<Stream<Pair<String, CborObject>>, CborObject.CborMap> map =
                s -> CborObject.CborMap.build(s.collect(Collectors.toMap(p -> p.left, p -> p.right)));
        CborObject.CborMap a = map.apply(Stream.of(new Pair<>("b", new CborObject.CborLong(1))));

        CborObject.CborMap cbor = map.apply(Stream.of(new Pair<>("a", a), new Pair<>("c", new CborObject.CborLong(2))));

        IpldNode.CborIpldNode node = new IpldNode.CborIpldNode(cbor);
        List<String> tree = node.tree("", -1);
        Assert.assertTrue("Correct tree", tree.equals(Arrays.asList("/a/b", "/c")));
    }

    @Test
    public void singleFileTest() throws IOException {
        NamedStreamable.ByteArrayWrapper file = new NamedStreamable.ByteArrayWrapper("hello.txt", "G'day world! udfs rocks!".getBytes());
        fileTest(file);
    }

    @Test
    public void wrappedSingleFileTest() throws IOException {
        NamedStreamable.ByteArrayWrapper file = new NamedStreamable.ByteArrayWrapper("hello.txt", "G'day world! udfs rocks!".getBytes());
        List<MerkleNode> addParts = udfs.add(file, true);
        MerkleNode filePart = addParts.get(0);
        MerkleNode dirPart = addParts.get(1);
        byte[] catResult = udfs.cat(filePart.hash);
        byte[] getResult = udfs.get(filePart.hash);
        if (!Arrays.equals(catResult, file.getContents()))
            throw new IllegalStateException("Different contents!");
        List<Multihash> pinRm = udfs.pin.rm(dirPart.hash, true);
        if (!pinRm.get(0).equals(dirPart.hash))
            throw new IllegalStateException("Didn't remove file!");
        Object gc = udfs.repo.gc();
    }

    @Test
    public void pushSingleFileTest() throws IOException {
        NamedStreamable.ByteArrayWrapper file = new NamedStreamable.ByteArrayWrapper("udfstest.txt", "welcome! udfs rocks!".getBytes());
        List<MerkleNode> addParts = udfs.push(file, true);//push方法已经remove代理节点的文件，所以不用删除代理节点
        MerkleNode filePart = addParts.get(0);
        MerkleNode dirPart = addParts.get(1);
        MerkleNode backupPart = addParts.get(2);
        byte[] catResult = udfs.cat(filePart.hash);
        byte[] getResult = udfs.get(filePart.hash);
        if (!Arrays.equals(catResult, file.getContents()))
            throw new IllegalStateException("Different contents!");
        //List<Multihash> pinRm = udfs.pin.rm(dirPart.hash, true);
        System.out.println(filePart.hash);
       /* if (!pinRm.get(0).equals(dirPart.hash))
            throw new IllegalStateException("Didn't remove file!");*/
        //Object gc = udfs.repo.gc();
    }

    @Test
    public void pushFileTest() throws IOException {
        //要添加文件使用
     /*   NamedStreamable.FileWrapper file = new NamedStreamable.FileWrapper(new File("F:/test/20181116.txt"));
        //添加文件到IPFS返回HASH值
        List<MerkleNode> addParts = udfs.add(file);
        System.out.println("内容为:"+addParts.get(0).toJSONString());
        String hash="QmPQJ6CTMxxGintCKKeQ38gxE8P9nnUdq5nM8nifLT1aQh";
        //Multihash hash=new Multihash("QmPQJ6CTMxxGintCKKeQ38gxE8P9nnUdq5nM8nifLT1aQh");
        List<Multihash> pinRm=udfs.pin.rm(Multihash.fromBase58("QmPQJ6CTMxxGintCKKeQ38gxE8P9nnUdq5nM8nifLT1aQh"));
        System.out.println("删除结果:"+pinRm.get(0).toString());*/

        Multihash filePointer = Multihash.fromBase58("Qme7KYZZTkARzkwE4x3vLKC4zB1jtNdw5HwuCxqABE7Kgc");
        byte[] fileContents = udfs.cat(filePointer);
        String str=new String(fileContents);
        System.out.println("查询的内容为："+str);
        List<Multihash> pinRm=udfs.pin.rmlocal(filePointer);
        System.out.println("删除结果:"+pinRm.get(0).toString());
        //输出HASH值
        //List<MerkleNode> addParts = udfs.push(file);
        /* System.out.println("哈希值:"+addParts.get(0).hash);
        System.out.println("名称:"+addParts.get(0).name);
        System.out.println("文件大小"+addParts.get(0).largeSize);
        System.out.println("备份节点信息"+addParts.get(0).backup);*/
    }

    public void download(String filePathName,String hash) throws IOException {
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
    }

    @Test
    public void dirTest() throws IOException {
        NamedStreamable.DirWrapper dir = new NamedStreamable.DirWrapper("root", Arrays.asList());
        MerkleNode addResult = udfs.add(dir).get(0);
        List<MerkleNode> ls = udfs.ls(addResult.hash);
        Assert.assertTrue(ls.size() > 0);
    }

    @Test
    public void directoryTest() throws IOException {
        Random rnd = new Random();
        String dirName = "folder" + rnd.nextInt(100);
        Path tmpDir = Files.createTempDirectory(dirName);

        String fileName = "afile" + rnd.nextInt(100);
        Path file = tmpDir.resolve(fileName);
        FileOutputStream fout = new FileOutputStream(file.toFile());
        byte[] fileContents = "udfs rocks!".getBytes();
        fout.write(fileContents);
        fout.flush();
        fout.close();

        String subdirName = "subdir";
        tmpDir.resolve(subdirName).toFile().mkdir();

        String subfileName = "subdirfile" + rnd.nextInt(100);
        Path subdirfile = tmpDir.resolve(subdirName + "/" + subfileName);
        FileOutputStream fout2 = new FileOutputStream(subdirfile.toFile());
        byte[] file2Contents = "udfs still rocks!".getBytes();
        fout2.write(file2Contents);
        fout2.flush();
        fout2.close();

        List<MerkleNode> addParts = udfs.add(new NamedStreamable.FileWrapper(tmpDir.toFile()));
        MerkleNode addResult = addParts.get(addParts.size() - 1);
        List<MerkleNode> lsResult = udfs.ls(addResult.hash);
        if (lsResult.size() != 1)
            throw new IllegalStateException("Incorrect number of objects in ls!");
        if (!lsResult.get(0).equals(addResult))
            throw new IllegalStateException("Object not returned in ls!");
        byte[] catResult = udfs.cat(addResult.hash, "/" + fileName);
        if (!Arrays.equals(catResult, fileContents))
            throw new IllegalStateException("Different contents!");

        byte[] catResult2 = udfs.cat(addResult.hash, "/" + subdirName + "/" + subfileName);
        if (!Arrays.equals(catResult2, file2Contents))
            throw new IllegalStateException("Different contents!");
    }

//    @Test
    public void largeFileTest() throws IOException {
        byte[] largerData = new byte[100*1024*1024];
        new Random(1).nextBytes(largerData);
        NamedStreamable.ByteArrayWrapper largeFile = new NamedStreamable.ByteArrayWrapper("nontrivial.txt", largerData);
        fileTest(largeFile);
    }

//    @Test
    public void hugeFileStreamTest() throws IOException {
        byte[] hugeData = new byte[1000*1024*1024];
        new Random(1).nextBytes(hugeData);
        NamedStreamable.ByteArrayWrapper largeFile = new NamedStreamable.ByteArrayWrapper("massive.txt", hugeData);
        MerkleNode addResult = udfs.add(largeFile).get(0);
        InputStream in = udfs.catStream(addResult.hash);

        byte[] res = new byte[hugeData.length];
        int offset = 0;
        byte[] buf = new byte[4096];
        int r;
        while ((r = in.read(buf)) >= 0) {
            try {
                System.arraycopy(buf, 0, res, offset, r);
                offset += r;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if (!Arrays.equals(res, hugeData))
            throw new IllegalStateException("Different contents!");
    }

    @Test
    public void hostFileTest() throws IOException {
        Path tempFile = Files.createTempFile("udfs", "tmp");
        BufferedWriter w = new BufferedWriter(new FileWriter(tempFile.toFile()));
        w.append("Some data");
        w.flush();
        w.close();
        NamedStreamable hostFile = new NamedStreamable.FileWrapper(tempFile.toFile());
        fileTest(hostFile);
    }

    @Test
    public void hashOnly() throws IOException {
        byte[] data = randomBytes(4096);
        NamedStreamable file = new NamedStreamable.ByteArrayWrapper(data);
        MerkleNode addResult = udfs.add(file, false, true).get(0);
        List<Multihash> local = udfs.refs.local();
        if (local.contains(addResult.hash))
            throw new IllegalStateException("Object shouldn't be present!");
    }

    public void fileTest(NamedStreamable file)  throws IOException{
        MerkleNode addResult = udfs.add(file).get(0);
        byte[] catResult = udfs.cat(addResult.hash);
        byte[] getResult = udfs.get(addResult.hash);
        if (!Arrays.equals(catResult, file.getContents()))
            throw new IllegalStateException("Different contents!");
        List<Multihash> pinRm = udfs.pin.rm(addResult.hash, true);
        if (!pinRm.get(0).equals(addResult.hash))
            throw new IllegalStateException("Didn't remove file!");
        Object gc = udfs.repo.gc();
    }

    @Test
    public void pinTest() throws IOException {
        MerkleNode file = udfs.add(new NamedStreamable.ByteArrayWrapper("some data".getBytes())).get(0);
        Multihash hash = file.hash;
        Map<Multihash, Object> ls1 = udfs.pin.ls(UDFS.PinType.all);
        boolean pinned = ls1.containsKey(hash);
        List<Multihash> rm = udfs.pin.rm(hash);
        // second rm should not throw a http 500, but return an empty list
//            List<Multihash> rm2 = udfs.pin.rm(hash);
        List<Multihash> add2 = udfs.pin.add(hash);
        // adding something already pinned should succeed
        List<Multihash> add3 = udfs.pin.add(hash);
        Map<Multihash, Object> ls = udfs.pin.ls(UDFS.PinType.recursive);
        udfs.repo.gc();
        // object should still be present after gc
        Map<Multihash, Object> ls2 = udfs.pin.ls(UDFS.PinType.recursive);
        boolean stillPinned = ls2.containsKey(hash);
        Assert.assertTrue("Pinning works", pinned && stillPinned);
    }

    @Test
    public void pinUpdate() throws IOException {
        MerkleNode child1 = udfs.add(new NamedStreamable.ByteArrayWrapper("some data".getBytes())).get(0);
        Multihash hashChild1 = child1.hash;
        System.out.println("child1: " + hashChild1);

        CborObject.CborMerkleLink root1 = new CborObject.CborMerkleLink(hashChild1);
        MerkleNode root1Res = udfs.block.put(Collections.singletonList(root1.toByteArray()), Optional.of("cbor")).get(0);
        System.out.println("root1: " + root1Res.hash);
        udfs.pin.add(root1Res.hash);

        CborObject.CborList root2 = new CborObject.CborList(Arrays.asList(new CborObject.CborMerkleLink(hashChild1), new CborObject.CborLong(42)));
        MerkleNode root2Res = udfs.block.put(Collections.singletonList(root2.toByteArray()), Optional.of("cbor")).get(0);
        List<MultiAddress> update = udfs.pin.update(root1Res.hash, root2Res.hash, true);

        Map<Multihash, Object> ls = udfs.pin.ls(UDFS.PinType.all);
        boolean childPresent = ls.containsKey(hashChild1);
        if (!childPresent)
            throw new IllegalStateException("Child not present!");

        udfs.repo.gc();
        Map<Multihash, Object> ls2 = udfs.pin.ls(UDFS.PinType.all);
        boolean childPresentAfterGC = ls2.containsKey(hashChild1);
        if (!childPresentAfterGC)
            throw new IllegalStateException("Child not present!");
    }

    @Test
    public void rawLeafNodePinUpdate() throws IOException {
        MerkleNode child1 = udfs.block.put("some data".getBytes(), Optional.of("raw"));
        Multihash hashChild1 = child1.hash;
        System.out.println("child1: " + hashChild1);

        CborObject.CborMerkleLink root1 = new CborObject.CborMerkleLink(hashChild1);
        MerkleNode root1Res = udfs.block.put(Collections.singletonList(root1.toByteArray()), Optional.of("cbor")).get(0);
        System.out.println("root1: " + root1Res.hash);
        udfs.pin.add(root1Res.hash);

        MerkleNode child2 = udfs.block.put("G'day new tree".getBytes(), Optional.of("raw"));
        Multihash hashChild2 = child2.hash;

        CborObject.CborList root2 = new CborObject.CborList(Arrays.asList(
                new CborObject.CborMerkleLink(hashChild1),
                new CborObject.CborMerkleLink(hashChild2),
                new CborObject.CborLong(42))
        );
        MerkleNode root2Res = udfs.block.put(Collections.singletonList(root2.toByteArray()), Optional.of("cbor")).get(0);
        List<MultiAddress> update = udfs.pin.update(root1Res.hash, root2Res.hash, false);
    }

    @Test
    public void indirectPinTest() throws IOException {
        Multihash EMPTY = udfs.object._new(Optional.empty()).hash;
        MerkleNode data = udfs.object.patch(EMPTY, "set-data", Optional.of("childdata".getBytes()), Optional.empty(), Optional.empty());
        Multihash child = data.hash;

        MerkleNode tmp1 = udfs.object.patch(EMPTY, "set-data", Optional.of("parent1_data".getBytes()), Optional.empty(), Optional.empty());
        Multihash parent1 = udfs.object.patch(tmp1.hash, "add-link", Optional.empty(), Optional.of(child.toString()), Optional.of(child)).hash;
        udfs.pin.add(parent1);

        MerkleNode tmp2 = udfs.object.patch(EMPTY, "set-data", Optional.of("parent2_data".getBytes()), Optional.empty(), Optional.empty());
        Multihash parent2 = udfs.object.patch(tmp2.hash, "add-link", Optional.empty(), Optional.of(child.toString()), Optional.of(child)).hash;
        udfs.pin.add(parent2);
        udfs.pin.rm(parent1, true);

        Map<Multihash, Object> ls = udfs.pin.ls(UDFS.PinType.all);
        boolean childPresent = ls.containsKey(child);
        if (!childPresent)
            throw new IllegalStateException("Child not present!");

        udfs.repo.gc();
        Map<Multihash, Object> ls2 = udfs.pin.ls(UDFS.PinType.all);
        boolean childPresentAfterGC = ls2.containsKey(child);
        if (!childPresentAfterGC)
            throw new IllegalStateException("Child not present!");
}

    @Test
    public void objectPatch() throws IOException {
        MerkleNode obj = udfs.object._new(Optional.empty());
        Multihash base = obj.hash;
        // link tests
        String linkName = "alink";
        MerkleNode addLink = udfs.object.patch(base, "add-link", Optional.empty(), Optional.of(linkName), Optional.of(base));
        MerkleNode withLink = udfs.object.get(addLink.hash);
        if (withLink.links.size() != 1 || !withLink.links.get(0).hash.equals(base) || !withLink.links.get(0).name.get().equals(linkName))
            throw new RuntimeException("Added link not correct!");
        MerkleNode rmLink = udfs.object.patch(addLink.hash, "rm-link", Optional.empty(), Optional.of(linkName), Optional.empty());
        if (!rmLink.hash.equals(base))
            throw new RuntimeException("Adding not inverse of removing link!");

        // data tests
//            byte[] data = "some random textual data".getBytes();
        byte[] data = new byte[1024];
        new Random().nextBytes(data);
        MerkleNode patched = udfs.object.patch(base, "set-data", Optional.of(data), Optional.empty(), Optional.empty());
        byte[] patchedResult = udfs.object.data(patched.hash);
        if (!Arrays.equals(patchedResult, data))
            throw new RuntimeException("object.patch: returned data != stored data!");

        MerkleNode twicePatched = udfs.object.patch(patched.hash, "append-data", Optional.of(data), Optional.empty(), Optional.empty());
        byte[] twicePatchedResult = udfs.object.data(twicePatched.hash);
        byte[] twice = new byte[2*data.length];
        for (int i=0; i < 2; i++)
            System.arraycopy(data, 0, twice, i*data.length, data.length);
        if (!Arrays.equals(twicePatchedResult, twice))
            throw new RuntimeException("object.patch: returned data after append != stored data!");

    }

    @Test
    public void refsTest() throws IOException {
        List<Multihash> local = udfs.refs.local();
        for (Multihash ref: local) {
            Object refs = udfs.refs(ref, false);
        }
    }

    @Test
    public void objectTest() throws IOException {
        MerkleNode _new = udfs.object._new(Optional.empty());
        Multihash pointer = Multihash.fromBase58("QmPZ9gcCEpqKTo6aq61g2nXGUhM4iCL3ewB6LDXZCtioEB");
        MerkleNode object = udfs.object.get(pointer);
        List<MerkleNode> newPointer = udfs.object.put(Arrays.asList(object.toJSONString().getBytes()));
        List<MerkleNode> newPointer2 = udfs.object.put("json", Arrays.asList(object.toJSONString().getBytes()));
        MerkleNode links = udfs.object.links(pointer);
        byte[] data = udfs.object.data(pointer);
        Map stat = udfs.object.stat(pointer);
    }

    @Test
    public void blockTest() throws IOException {
        MerkleNode pointer = new MerkleNode("QmPZ9gcCEpqKTo6aq61g2nXGUhM4iCL3ewB6LDXZCtioEB");
        Map stat = udfs.block.stat(pointer.hash);
        byte[] object = udfs.block.get(pointer.hash);
        List<MerkleNode> newPointer = udfs.block.put(Arrays.asList("Some random data...".getBytes()));
    }

    @Test
    public void bulkBlockTest() throws IOException {
        CborObject cbor = new CborObject.CborString("G'day udfs!");
        byte[] raw = cbor.toByteArray();
        List<MerkleNode> bulkPut = udfs.block.put(Arrays.asList(raw, raw, raw, raw, raw), Optional.of("cbor"));
        List<Multihash> hashes = bulkPut.stream().map(m -> m.hash).collect(Collectors.toList());
        byte[] result = udfs.block.get(hashes.get(0));
        System.out.println();
    }

    @Test
    public void publish() throws Exception {
        // JSON document
        String json = "{\"name\":\"blogpost\",\"documents\":[]}";

        // Add a DAG node to udfs
        MerkleNode merkleNode = udfs.dag.put("json", json.getBytes());
        Assert.assertEquals("expected to be zdpuAknRh1Kro2r2xBDKiXyTiwA3Nu5XcmvjRPA1VNjH41NF7" , "zdpuAknRh1Kro2r2xBDKiXyTiwA3Nu5XcmvjRPA1VNjH41NF7", merkleNode.hash.toString());

        // Get a DAG node
        byte[] res = udfs.dag.get((Cid) merkleNode.hash);
        Assert.assertEquals("Should be equals", JSONParser.parse(json), JSONParser.parse(new String(res)));

        // Publish to IPNS
        Map result = udfs.name.publish(merkleNode.hash);

        // Resolve from IPNS
        String resolved = udfs.name.resolve(Multihash.fromBase58((String) result.get("Name")));
        Assert.assertEquals("Should be equals", resolved, "/udfs/" + merkleNode.hash.toString());
    }

    @Test
    public void pubsubSynchronous() throws Exception {
        String topic = "topic" + System.nanoTime();
        List<Map<String, Object>> res = Collections.synchronizedList(new ArrayList<>());
        new Thread(() -> {
            try {
                udfs.pubsub.sub(topic, res::add, t -> t.printStackTrace());
            } catch (IOException e) {
                throw new RuntimeException(e);}
        }).start();

        long start = System.currentTimeMillis();
        for (int i=1; i < 100; ) {
            long t1 = System.currentTimeMillis();
            udfs.pubsub.pub(topic, "Hello!");
            if (res.size() >= i) {
                long t2 = System.currentTimeMillis();
                System.out.println("pub => sub took " + (t2 - t1));
                i++;
            }
        }
        long duration = System.currentTimeMillis() - start;
        Assert.assertTrue("Fast synchronous pub-sub", duration < 1000);
    }

    @Test
    public void pubsub() throws Exception {
        String topic = "topic" + System.nanoTime();
        Stream<Map<String, Object>> sub = udfs.pubsub.sub(topic);
        String data = "Hello!";
        Object pub = udfs.pubsub.pub(topic, data);
        Object pub2 = udfs.pubsub.pub(topic, "G'day");
        List<Map> results = sub.limit(2).collect(Collectors.toList());
        Assert.assertTrue( ! results.get(0).equals(Collections.emptyMap()));
    }

    private static String toEscapedHex(byte[] in) throws IOException {
        StringBuilder res = new StringBuilder();
        for (byte b : in) {
            res.append("\\x");
            res.append(String.format("%02x", b & 0xFF));
        }
        return res.toString();
    }

    /**
     *  Test that merkle links in values of a cbor map are followed during recursive pins
     */
    @Test
    public void merkleLinkInMap() throws IOException {
        Random r = new Random();
        CborObject.CborByteArray target = new CborObject.CborByteArray(("g'day udfs!").getBytes());
        byte[] rawTarget = target.toByteArray();
        MerkleNode targetRes = udfs.block.put(Arrays.asList(rawTarget), Optional.of("cbor")).get(0);

        CborObject.CborMerkleLink link = new CborObject.CborMerkleLink(targetRes.hash);
        Map<String, CborObject> m = new TreeMap<>();
        m.put("alink", link);
        m.put("arr", new CborObject.CborList(Collections.emptyList()));
        CborObject.CborMap source = CborObject.CborMap.build(m);
        byte[] rawSource = source.toByteArray();
        MerkleNode sourceRes = udfs.block.put(Arrays.asList(rawSource), Optional.of("cbor")).get(0);

        CborObject.fromByteArray(rawSource);

        List<Multihash> add = udfs.pin.add(sourceRes.hash);
        udfs.repo.gc();
        udfs.repo.gc();

        List<Multihash> refs = udfs.refs(sourceRes.hash, true);
        Assert.assertTrue("refs returns links", refs.contains(targetRes.hash));

        byte[] bytes = udfs.block.get(targetRes.hash);
        Assert.assertTrue("same contents after GC", Arrays.equals(bytes, rawTarget));
        // These commands can be used to reproduce this on the command line
        String reproCommand1 = "printf \"" + toEscapedHex(rawTarget) + "\" | udfs block put --format=cbor";
        String reproCommand2 = "printf \"" + toEscapedHex(rawSource) + "\" | udfs block put --format=cbor";
        System.out.println();
    }

    @Test
    public void recursiveRefs() throws IOException {
        CborObject.CborByteArray leaf1 = new CborObject.CborByteArray(("G'day udfs!").getBytes());
        byte[] rawLeaf1 = leaf1.toByteArray();
        MerkleNode leaf1Res = udfs.block.put(Arrays.asList(rawLeaf1), Optional.of("cbor")).get(0);

        CborObject.CborMerkleLink link = new CborObject.CborMerkleLink(leaf1Res.hash);
        Map<String, CborObject> m = new TreeMap<>();
        m.put("link1", link);
        CborObject.CborMap source = CborObject.CborMap.build(m);
        MerkleNode sourceRes = udfs.block.put(Arrays.asList(source.toByteArray()), Optional.of("cbor")).get(0);

        CborObject.CborByteArray leaf2 = new CborObject.CborByteArray(("G'day again, udfs!").getBytes());
        byte[] rawLeaf2 = leaf2.toByteArray();
        MerkleNode leaf2Res = udfs.block.put(Arrays.asList(rawLeaf2), Optional.of("cbor")).get(0);

        Map<String, CborObject> m2 = new TreeMap<>();
        m2.put("link1", new CborObject.CborMerkleLink(sourceRes.hash));
        m2.put("link2", new CborObject.CborMerkleLink(leaf2Res.hash));
        CborObject.CborMap source2 = CborObject.CborMap.build(m2);
        MerkleNode rootRes = udfs.block.put(Arrays.asList(source2.toByteArray()), Optional.of("cbor")).get(0);

        List<Multihash> refs = udfs.refs(rootRes.hash, false);
        boolean correct = refs.contains(sourceRes.hash) && refs.contains(leaf2Res.hash) && refs.size() == 2;
        Assert.assertTrue("refs returns links", correct);

        List<Multihash> refsRecurse = udfs.refs(rootRes.hash, true);
        boolean correctRecurse = refs.contains(sourceRes.hash)
                && refs.contains(leaf1Res.hash)
                && refs.contains(leaf2Res.hash)
                && refs.size() == 3;
        Assert.assertTrue("refs returns links", correct);
    }

    /**
     *  Test that merkle links as a root object are followed during recursive pins
     */
    @Test
    public void rootMerkleLink() throws IOException {
        Random r = new Random();
        CborObject.CborByteArray target = new CborObject.CborByteArray(("g'day udfs!" + r.nextInt()).getBytes());
        byte[] rawTarget = target.toByteArray();
        MerkleNode block1 = udfs.block.put(Arrays.asList(rawTarget), Optional.of("cbor")).get(0);
        Multihash block1Hash = block1.hash;
        byte[] retrievedObj1 = udfs.block.get(block1Hash);
        Assert.assertTrue("get inverse of put", Arrays.equals(retrievedObj1, rawTarget));

        CborObject.CborMerkleLink cbor2 = new CborObject.CborMerkleLink(block1.hash);
        byte[] obj2 = cbor2.toByteArray();
        MerkleNode block2 = udfs.block.put(Arrays.asList(obj2), Optional.of("cbor")).get(0);
        byte[] retrievedObj2 = udfs.block.get(block2.hash);
        Assert.assertTrue("get inverse of put", Arrays.equals(retrievedObj2, obj2));

        List<Multihash> add = udfs.pin.add(block2.hash);
        udfs.repo.gc();
        udfs.repo.gc();

        byte[] bytes = udfs.block.get(block1.hash);
        Assert.assertTrue("same contents after GC", Arrays.equals(bytes, rawTarget));
        // These commands can be used to reproduce this on the command line
        String reproCommand1 = "printf \"" + toEscapedHex(rawTarget) + "\" | udfs block put --format=cbor";
        String reproCommand2 = "printf \"" + toEscapedHex(obj2) + "\" | udfs block put --format=cbor";
        System.out.println();
    }

    /**
     *  Test that a cbor null is allowed as an object root
     */
    @Test
    public void rootNull() throws IOException {
        CborObject.CborNull cbor = new CborObject.CborNull();
        byte[] obj = cbor.toByteArray();
        MerkleNode block = udfs.block.put(Arrays.asList(obj), Optional.of("cbor")).get(0);
        byte[] retrievedObj = udfs.block.get(block.hash);
        Assert.assertTrue("get inverse of put", Arrays.equals(retrievedObj, obj));

        List<Multihash> add = udfs.pin.add(block.hash);
        udfs.repo.gc();
        udfs.repo.gc();

        // These commands can be used to reproduce this on the command line
        String reproCommand1 = "printf \"" + toEscapedHex(obj) + "\" | udfs block put --format=cbor";
        System.out.println();
    }

    /**
     *  Test that merkle links in a cbor list are followed during recursive pins
     */
    @Test
    public void merkleLinkInList() throws IOException {
        Random r = new Random();
        CborObject.CborByteArray target = new CborObject.CborByteArray(("g'day udfs!" + r.nextInt()).getBytes());
        byte[] rawTarget = target.toByteArray();
        MerkleNode targetRes = udfs.block.put(Arrays.asList(rawTarget), Optional.of("cbor")).get(0);

        CborObject.CborMerkleLink link = new CborObject.CborMerkleLink(targetRes.hash);
        CborObject.CborList source = new CborObject.CborList(Arrays.asList(link));
        byte[] rawSource = source.toByteArray();
        MerkleNode sourceRes = udfs.block.put(Arrays.asList(rawSource), Optional.of("cbor")).get(0);

        List<Multihash> add = udfs.pin.add(sourceRes.hash);
        udfs.repo.gc();
        udfs.repo.gc();

        byte[] bytes = udfs.block.get(targetRes.hash);
        Assert.assertTrue("same contents after GC", Arrays.equals(bytes, rawTarget));
        // These commands can be used to reproduce this on the command line
        String reproCommand1 = "printf \"" + toEscapedHex(rawTarget) + "\" | udfs block put --format=cbor";
        String reproCommand2 = "printf \"" + toEscapedHex(rawSource) + "\" | udfs block put --format=cbor";
    }

    @Test
    public void fileContentsTest() throws IOException {
        udfs.repo.gc();
        List<Multihash> local = udfs.refs.local();
        for (Multihash hash: local) {
            try {
                Map ls = udfs.file.ls(hash);
                return;
            } catch (Exception e) {} // non unixfs files will throw an exception here
        }
    }

    @Test
    @Ignore("name test may hang forever")
    public void nameTest() throws IOException {
        MerkleNode pointer = new MerkleNode("QmPZ9gcCEpqKTo6aq61g2nXGUhM4iCL3ewB6LDXZCtioEB");
        Map pub = udfs.name.publish(pointer.hash);
        String name = "key" + System.nanoTime();
        Object gen = udfs.key.gen(name, Optional.of("rsa"), Optional.of("2048"));
        Map mykey = udfs.name.publish(pointer.hash, Optional.of(name));
        String resolved = udfs.name.resolve(Multihash.fromBase58((String) pub.get("Name")));
    }

    @Test
    @Ignore("[#103] DNS test cannot resolve name")
    public void dnsTest() throws IOException {
        String domain = "udfs.io";
        String dns = udfs.dns(domain);
    }

    public void mountTest() throws IOException {
        Map mount = udfs.mount(null, null);
    }

    @Test
    @Ignore("dht test may hang forever")
    public void dhtTest() throws IOException {
        Multihash pointer = Multihash.fromBase58("QmPZ9gcCEpqKTo6aq61g2nXGUhM4iCL3ewB6LDXZCtioEB");
        Map get = udfs.dht.get(pointer);
        Map put = udfs.dht.put("somekey", "somevalue");
        Map findprovs = udfs.dht.findprovs(pointer);
        List<Peer> peers = udfs.swarm.peers();
        Map query = udfs.dht.query(peers.get(0).id);
        Map find = udfs.dht.findpeer(peers.get(0).id);
    }

    @Test
    public void localId() throws Exception {
        Map id = udfs.id();
        System.out.println();
    }

    @Test
    public void statsTest() throws IOException {
        Map stats = udfs.stats.bw();
    }

    public void resolveTest() throws IOException {
        Multihash hash = Multihash.fromBase58("QmatmE9msSfkKxoffpHwNLNKgwZG8eT9Bud6YoPab52vpy");
        Map res = udfs.resolve("ipns", hash, false);
    }

    @Test
    public void swarmTest() throws IOException {
        Map<Multihash, List<MultiAddress>> addrs = udfs.swarm.addrs();
        if (addrs.size() > 0) {
            boolean contacted = addrs.entrySet().stream()
                    .anyMatch(e -> {
                        Multihash target = e.getKey();
                        List<MultiAddress> nodeAddrs = e.getValue();
                        boolean contactable = nodeAddrs.stream()
                                .anyMatch(addr -> {
                                    try {
                                        MultiAddress peer = new MultiAddress(addr.toString() + "/udfs/" + target.toBase58());
                                        Map connect = udfs.swarm.connect(peer);
                                        Map disconnect = udfs.swarm.disconnect(peer);
                                        return true;
                                    } catch (Exception ex) {
                                        return false;
                                    }
                                });
                        try {
                            Map id = udfs.id(target);
                            Map ping = udfs.ping(target);
                            return contactable;
                        } catch (Exception ex) {
                            // not all nodes have to be contactable
                            return false;
                        }
                    });
            if (!contacted)
                throw new IllegalStateException("Couldn't contact any node!");
        }
        List<Peer> peers = udfs.swarm.peers();
        System.out.println(peers);
    }

    @Test
    public void bootstrapTest() throws IOException {
        List<MultiAddress> bootstrap = udfs.bootstrap.list();
        System.out.println(bootstrap);
        List<MultiAddress> rm = udfs.bootstrap.rm(bootstrap.get(0), false);
        List<MultiAddress> add = udfs.bootstrap.add(bootstrap.get(0));
        System.out.println();
    }

    @Test
    public void diagTest() throws IOException {
        Map config = udfs.config.show();
        String val = udfs.config.get("Datastore.GCPeriod");
        Map setResult = udfs.config.set("Datastore.GCPeriod", val);
        udfs.config.replace(new NamedStreamable.ByteArrayWrapper(JSONParser.toString(config).getBytes()));
//            Object log = udfs.log();
        String sys = udfs.diag.sys();
        String cmds = udfs.diag.cmds();
    }

    @Test
    public void toolsTest() throws IOException {
        String version = udfs.version();
        int major = Integer.parseInt(version.split("\\.")[0]);
        int minor = Integer.parseInt(version.split("\\.")[1]);
        assertTrue(major >= 0 && minor >= 4);     // Requires at least 0.4.0
        Map commands = udfs.commands();
    }
    /*@Test
    public void testUdfs(){
        UDFSClient client=new UDFSClient("/ip4/111.231.218.88/tcp/5001");
        String result=client.publishResource("1.txt","hello world zhongguo".getBytes());
        System.out.println(result);

    }*/

    // this api is disabled until deployment over udfs is enabled
    public void updateTest() throws IOException {
        Object check = udfs.update.check();
        Object update = udfs.update();
    }

    private byte[] randomBytes(int len) {
        byte[] res = new byte[len];
        r.nextBytes(res);
        return res;
    }
}
