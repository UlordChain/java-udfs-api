package io.udfs.api;

import io.ipfs.cid.*;
import io.ipfs.multihash.Multihash;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.stream.*;

public class MerkleNode {

    public final Multihash hash;
    public final Optional<String> name;
    public final Optional<Integer> size;
    public final Optional<String> largeSize;
    public final Optional<Integer> type;
    public final List<MerkleNode> links;
    public final Optional<byte[]> data;
    public final List<String> backup;
    public static String backdirhash="";
    public static String backfilehash="";

    public MerkleNode(String hash,
                      Optional<String> name,
                      Optional<Integer> size,
                      Optional<String> largeSize,
                      Optional<Integer> type,
                      List<MerkleNode> links,
                      Optional<byte[]> data,
                      List<String> backup) {
        this.name = name;
        this.hash = Cid.decode(hash);
        this.size = size;
        this.largeSize = largeSize;
        this.type = type;
        this.links = links;
        this.data = data;
        this.backup=backup;
    }

    public MerkleNode(String hash) {
        this(hash, Optional.empty());
    }

    public MerkleNode(String hash, Optional<String> name) {
        this(hash, name, Optional.empty(), Optional.empty(), Optional.empty(), Arrays.asList(), Optional.empty(),null);
    }

    @Override
    public boolean equals(Object b) {
        if (!(b instanceof MerkleNode))
            return false;
        MerkleNode other = (MerkleNode) b;
        return hash.equals(other.hash); // ignore name hash says it all
    }

    @Override
    public int hashCode() {
        return hash.hashCode();
    }

    public static MerkleNode fromJSON(Object rawjson) {
        if (rawjson instanceof String)
            return new MerkleNode((String)rawjson);
        Map json = (Map)rawjson;
        if ("error".equals(json.get("Type")))
            throw new IllegalStateException("Remote UDFS error: " + json.get("Message"));
        String hash = (String)json.get("Hash");
        if (hash == null)
            hash = (String)json.get("Key");
        if (hash == null && json.containsKey("Cid"))
            hash = (String) (((Map) json.get("Cid")).get("/"));
        if(null!=hash){
            backdirhash=hash;
        }
        Optional<String> name = json.containsKey("Name") ?
                Optional.of((String) json.get("Name")):
                Optional.empty();
        String fileName=(String) json.get("Name");
        if(StringUtils.isNotEmpty(fileName)){
            backfilehash=hash;
        }
        Object rawSize = json.get("Size");
        Optional<Integer> size = rawSize instanceof Integer ?
                Optional.of((Integer) rawSize) :
                Optional.empty();
        Optional<String> largeSize = rawSize instanceof String ?
                Optional.of((String) json.get("Size")) :
                Optional.empty();
        Optional<Integer> type = json.containsKey("Type") ?
                Optional.of((Integer) json.get("Type")) :
                Optional.empty();
        List<Object> linksRaw = (List<Object>) json.get("Links");
        List<MerkleNode> links = linksRaw == null ?
                Collections.emptyList() :
                linksRaw.stream().map(x -> MerkleNode.fromJSON(x)).collect(Collectors.toList());
        Optional<byte[]> data = json.containsKey("Data") ? Optional.of(((String)json.get("Data")).getBytes()): Optional.empty();
        List<String> backups=new ArrayList<String>();
        JSONObject job = JSONObject.fromObject(json.get("Extend"));
        if(null!=job&&job.size()!=0){
            JSONArray list = job.getJSONArray("Success");
            if(null!=list){
                hash=backdirhash;//备份信息将存放的节点的hash做为hash值
                name=Optional.of(backfilehash);//备份信息将文件的hash值作为name值
                Iterator<Object> it = list.iterator();
                while (it.hasNext()) {
                    JSONObject ob = (JSONObject) it.next();
                    String id = ob.getString("ID");
                    backups.add(id);
                }
            }
        }

        return new MerkleNode(hash, name, size, largeSize, type, links, data,backups);
    }

    public Object toJSON() {
        Map<String, Object> res = new TreeMap<>();
        res.put("Hash", hash);
        res.put("Links", links.stream().map(x -> x.hash).collect(Collectors.toList()));
        data.ifPresent(bytes -> res.put("Data", bytes));
        name.ifPresent(s -> res.put("Name", s));
        if (size.isPresent()) {
            res.put("Size", size.get());
        } else {
            largeSize.ifPresent(s -> res.put("Size", s));
        }
        type.ifPresent(integer -> res.put("Type", integer));
        return res;
    }

    public String toJSONString() {
        return JSONParser.toString(toJSON());
    }
}
