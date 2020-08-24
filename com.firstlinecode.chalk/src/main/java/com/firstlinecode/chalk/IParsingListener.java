package com.firstlinecode.chalk;

public interface IParsingListener extends Chained<IParsingListener> {
    String beforeParsing(String message);
    Object afterParsing(Object parseObject);
}
