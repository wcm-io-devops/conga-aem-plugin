/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2018 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.devops.conga.plugins.aem.handlebars.helper;

import java.io.IOException;

import org.apache.jackrabbit.oak.commons.UUIDUtils;
import org.apache.jackrabbit.oak.spi.security.ConfigurationParameters;
import org.apache.jackrabbit.oak.spi.security.user.UserConstants;

import com.github.jknack.handlebars.Options;

import io.wcm.devops.conga.generator.spi.handlebars.HelperPlugin;
import io.wcm.devops.conga.generator.spi.handlebars.context.HelperContext;

/**
 * Handlebars helper that builds a password hash for a given password string of an Oak JCR user.
 */
public final class OakAuthorizableUuidHelper implements HelperPlugin<Object> {

  /**
   * Plugin/Helper name
   */
  public static final String NAME = "oakAuthorizableUuid";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Object apply(Object context, Options options, HelperContext pluginContext) throws IOException {
    if (context == null) {
      return null;
    }

    ConfigurationParameters config = ConfigurationParameters.of(options.hash);
    boolean usercaseMappedProfile = config.getConfigValue(UserConstants.PARAM_ENABLE_RFC7613_USERCASE_MAPPED_PROFILE,
        UserConstants.DEFAULT_ENABLE_RFC7613_USERCASE_MAPPED_PROFILE);

    String authorizableId = context.toString();
    return getContentID(authorizableId, usercaseMappedProfile);
  }

  /*
   * Method copied from https://github.com/apache/jackrabbit-oak/blob/trunk/oak-core/src/main/java/org/apache/jackrabbit/oak/security/user/AuthorizableBaseProvider.java
   */
  private String getContentID(String authorizableId, boolean usercaseMappedProfile) {
    String s = authorizableId.toLowerCase();
    if (usercaseMappedProfile) {
      s = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFKC);
    }
    return UUIDUtils.generateUUID(s);
  }

}
