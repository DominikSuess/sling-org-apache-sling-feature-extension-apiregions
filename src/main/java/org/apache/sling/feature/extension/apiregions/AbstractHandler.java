/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sling.feature.extension.apiregions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Properties;

import org.apache.sling.feature.Feature;
import org.apache.sling.feature.builder.HandlerContext;

class AbstractHandler {
    static final String GLOBAL_NAME = "global";

    static final String NAME_KEY = "name";
    static final String EXPORTS_KEY = "exports";

    static final String FILE_PREFIX = "apiregions.";
    static final String FILE_STORAGE_DIR_KEY = "fileStorage";

    static final String PROPERTIES_RESOURCE_PREFIX = "sling.feature.apiregions.resource.";

    protected static File getFeatureDataFile(HandlerContext context, Feature target, String fileName) throws IOException {
        String featureName = target.getId().toMvnId().replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
        File f = AbstractHandler.getDataFile(context, featureName, fileName);
        f.getParentFile().mkdirs();
        return f;
    }

    protected static File getDataFile(HandlerContext context, String directory, String name) throws IOException {
        String stg = context.getConfiguration().get(FILE_STORAGE_DIR_KEY);
        File f;
        if (stg != null) {
            File dir;
            if (directory != null) {
                dir = new File(stg, directory);
                dir.mkdirs();
            } else {
                dir = new File(stg);
            }
            f = new File(dir, name);
        } else {
            // If we store in the temp space we don't use the directory
            Path p = Files.createTempFile(FILE_PREFIX, name);
            f = p.toFile();
            f.deleteOnExit();
        }

        // The feature launcher runtime picks the data file up from this system property
        System.setProperty(PROPERTIES_RESOURCE_PREFIX + name, f.getCanonicalPath());
        return f;
    }

    protected static File getDataFile(HandlerContext context, String name) throws IOException {
        return getDataFile(context, null, name);
    }

    protected Properties loadProperties(File file) throws IOException, FileNotFoundException {
        Properties map = new Properties();
        if (file.exists()) {
            try (InputStream is = new FileInputStream(file)) {
                map.load(is);
            }
        }
        return map;
    }

    protected void storeProperties(Properties properties, File file) throws IOException, FileNotFoundException {
        try (OutputStream os = new FileOutputStream(file)) {
            properties.store(os, "Generated at " + new Date());
        }
    }
}
