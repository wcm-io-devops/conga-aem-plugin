/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2022 wcm.io
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
package io.wcm.devops.conga.plugins.aem.maven;

/**
 * How to embed packages in the "all" package.
 */
public enum EmbedPackageMode {

  /**
   * Includes content packages via /apps folder, to be picked up by OSGi installer.
   * This is the recommended way and mandatory for AEMaaCS.
   */
  EMBED,

  /**
   * Includes content packages via /etc/packages folder, to be picked up by Package Manager.
   * This is an alternative mode for AEM 6.5 and below if you encounter issues with OSGi installer
   * (like <a href="https://github.com/Netcentric/accesscontroltool/issues/451">this</a>).
   */
  SUB_PACKAGE

}
