package io.udfs.api.cbor;

public interface Cborable {

    CborObject toCbor();

    default byte[] serialize() {
        return toCbor().toByteArray();
    }
}
