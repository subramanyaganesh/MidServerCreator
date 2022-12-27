package com.midServer.SetMid.Model;

import net.lingala.zip4j.exception.ZipException;

import java.io.IOException;
import java.sql.SQLException;

@FunctionalInterface
public interface ExpFunction<T,R> {
    R apply(T t) throws IOException, SQLException;
}
