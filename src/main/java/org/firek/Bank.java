package org.firek;

import static spark.Spark.get;

public class Bank {
    public static void main(String[] args) {
        get("/hello", (req, res) -> "Hello World");
    }
}
