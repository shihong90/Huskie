//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.apache.spark.sql.execution.customDatasource.streaming;

public abstract class Offset {
    public Offset() {
    }

    public abstract String json();

    public boolean equals(Object obj) {
        return obj instanceof org.apache.spark.sql.execution.streaming.Offset ? this.json().equals(((org.apache.spark.sql.execution.streaming.Offset)obj).json()) : false;
    }

    public int hashCode() {
        return this.json().hashCode();
    }

    public String toString() {
        return this.json();
    }
}