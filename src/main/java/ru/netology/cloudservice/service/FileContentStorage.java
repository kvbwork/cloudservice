package ru.netology.cloudservice.service;

import java.io.IOException;
import java.io.InputStream;

public interface FileContentStorage {

    boolean contains(String uid);

    InputStream get(String uid) throws IOException;

    long put(String uid, InputStream inputStream) throws IOException;

    void remove(String uid) throws IOException;

}
