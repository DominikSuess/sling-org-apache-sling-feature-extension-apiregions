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
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.jar.JarFile;

import org.apache.sling.feature.Artifact;
import org.apache.sling.feature.Extension;
import org.apache.sling.feature.Feature;
import org.apache.sling.feature.builder.HandlerContext;
import org.apache.sling.feature.builder.PostProcessHandler;
import org.apache.sling.feature.extension.apiregions.api.ApiRegions;
import org.apache.sling.feature.io.IOUtils;
import org.osgi.framework.Constants;

public class BundleMappingHandler extends AbstractHandler implements PostProcessHandler {
    @Override
    public void postProcess(HandlerContext context, Feature feature, Extension extension) {
        if (!ApiRegions.EXTENSION_NAME.equals(extension.getName()))
            return;

        try {
            File idBSNFile = getFeatureDataFile(context, feature, "idbsnver.properties");
            Properties map = loadProperties(idBSNFile);

            for (Artifact b : feature.getBundles()) {
                URL f = context.getArtifactProvider().provide(b.getId());

                try (JarFile jf = IOUtils.getJarFileFromURL(f, true, null)) {
                    String bsn = jf.getManifest().getMainAttributes().getValue(Constants.BUNDLE_SYMBOLICNAME);
                    if (bsn == null)
                        continue;

                    String ver = jf.getManifest().getMainAttributes().getValue(Constants.BUNDLE_VERSION);
                    if (ver == null)
                        ver = "0.0.0";

                    map.put(b.getId().toMvnId(), bsn.trim() + "~" + ver.trim());
                }
            }

            storeProperties(map, idBSNFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
