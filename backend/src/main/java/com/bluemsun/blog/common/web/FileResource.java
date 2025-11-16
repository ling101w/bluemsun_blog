package com.bluemsun.blog.common.web;

import java.io.InputStream;

public record FileResource(InputStream stream, String fileName, String contentType, long length) {
}


