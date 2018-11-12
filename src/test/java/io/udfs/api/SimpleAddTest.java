package io.udfs.api;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import io.ipfs.multiaddr.MultiAddress;

/**
 *
 * UDFS daemon --enable-pubsub-experiment &
 *
 * UDFS pin rm `UDFS pin ls -qt recursive`
 *
 * UDFS --api=/ip4/127.0.0.1/tcp/5001 add -r src/test/resources/html
 *
 */
public class SimpleAddTest {

    static final Map<String, String> cids = new LinkedHashMap<>();
    static {
        cids.put("index.html", "QmVts3YjmhsCSqMv8Thk1CCy1nnpCbqEFjbkjS7PEzthZE");
        cids.put("html", "QmUQvDumYa8najL94EnGhmGobyMyNzAmCSpfAxYnYcQHZD");
    }

    UDFS UDFS = new UDFS(new MultiAddress("/ip4/127.0.0.1/tcp/5001"));

    @Test
    public void testSingle() throws Exception {
        Path path = Paths.get("src/test/resources/html/index.html");
        NamedStreamable file = new NamedStreamable.FileWrapper(path.toFile());
        List<MerkleNode> tree = UDFS.add(file);

        Assert.assertEquals(1, tree.size());
        Assert.assertEquals("index.html", tree.get(0).name.get());
        Assert.assertEquals(cids.get("index.html"), tree.get(0).hash.toBase58());
    }

    @Test
    public void testSingleWrapped() throws Exception {

        Path path = Paths.get("src/test/resources/html/index.html");
        NamedStreamable file = new NamedStreamable.FileWrapper(path.toFile());
        List<MerkleNode> tree = UDFS.add(file, true);

        Assert.assertEquals(2, tree.size());
        Assert.assertEquals("index.html", tree.get(0).name.get());
        Assert.assertEquals(cids.get("index.html"), tree.get(0).hash.toBase58());
    }

    @Test
    public void testSingleOnlyHash() throws Exception {

        Path path = Paths.get("src/test/resources/html/index.html");
        NamedStreamable file = new NamedStreamable.FileWrapper(path.toFile());
        List<MerkleNode> tree = UDFS.add(file, false, true);

        Assert.assertEquals(1, tree.size());
        Assert.assertEquals("index.html", tree.get(0).name.get());
        Assert.assertEquals(cids.get("index.html"), tree.get(0).hash.toBase58());
    }

    @Test
    public void testRecursive() throws Exception {

        Path path = Paths.get("src/test/resources/html");
        NamedStreamable file = new NamedStreamable.FileWrapper(path.toFile());
        List<MerkleNode> tree = UDFS.add(file);

        Assert.assertEquals(8, tree.size());
        Assert.assertEquals("html", tree.get(7).name.get());
        Assert.assertEquals(cids.get("html"), tree.get(7).hash.toBase58());
    }

    @Test
    public void testRecursiveOnlyHash() throws Exception {

        Path path = Paths.get("src/test/resources/html");
        NamedStreamable file = new NamedStreamable.FileWrapper(path.toFile());
        List<MerkleNode> tree = UDFS.add(file, false, true);

        Assert.assertEquals(8, tree.size());
        Assert.assertEquals("html", tree.get(7).name.get());
        Assert.assertEquals(cids.get("html"), tree.get(7).hash.toBase58());
    }
}
