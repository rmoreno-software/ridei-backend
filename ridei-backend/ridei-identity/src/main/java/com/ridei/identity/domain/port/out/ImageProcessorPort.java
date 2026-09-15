package com.ridei.identity.domain.port.out;

public interface ImageProcessorPort {
    byte[] compressToFit(byte[] originalImage, long maxBytes);
}
